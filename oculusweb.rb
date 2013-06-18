require 'rubygems' if RUBY_VERSION < "1.9"
require 'sinatra/base'
require 'sinatra/config_file'
require 'haml'
require 'thin'
require 'json'
require 'msgpack'
require 'base64'
require 'sanitize'

require File.join(File.dirname(__FILE__), 'helpers/elasticsearch')
require File.join(File.dirname(__FILE__), 'helpers/redis')
require File.join(File.dirname(__FILE__), 'helpers/dygraph')
require File.join(File.dirname(__FILE__), 'helpers/fingerprint')

$sockets = []

class Oculusweb < Sinatra::Base

   # Comment out the following 3 lines if you want more verbose logging
   # like stacktraces and the dev mode sinatra error page
   #set :show_exceptions, false
   #set :logging, false
   #set :dump_errors, false

   register Sinatra::ConfigFile
   config_file './config/config.yml'

   helpers do

     def protected!
       unless authorized?
         response['WWW-Authenticate'] = %(Basic realm="Restricted Area")
         throw(:halt, [401, "Not authorized\n"])
       end
     end

     def authorized?
       @auth ||=  Rack::Auth::Basic::Request.new(request.env)
       @auth.provided? && @auth.basic? && @auth.credentials && @auth.credentials == ['admin', 'admin']
     end

   end

  before do
    @helper_status = Hash.new


    # Initialise Redis Helper
    begin
      @redis_helper = Oculus::Helpers::RedisHelper.new(settings.redis["host"], settings.redis["port"])
      @helper_status["Redis"] = "OK!"
    rescue HelperError => e
      @helper_status["Redis"] = "ERROR: #{e.message}"
    end

    # Initialise Elasticsearch Helper
    begin
      @active_es_server = @redis_helper.get("oculus.active_es_server") || settings.elasticsearch["servers"].first
      @elasticsearch_helper = Oculus::Helpers::ElasticsearchHelper.new(@active_es_server, settings.elasticsearch["index"], "metric", settings.elasticsearch["timeout"])
      @phrase_slop = params["p_slop"] ? params["p_slop"].to_i : settings.elasticsearch["phrase_slop"].to_i
      @elasticsearch_helper.phrase_slop=@phrase_slop
      @dtw_radius = params["dtw_radius"] ? params["dtw_radius"].to_i : settings.elasticsearch["scorers"]["dtw"]["radius"].to_i
      @elasticsearch_helper.dtw_radius=@dtw_radius
      @elasticsearch_helper.dtw_scale_points = settings.elasticsearch["scorers"]["dtw"]["scale_points"]
      @elasticsearch_helper.euclidian_scale_points = settings.elasticsearch["scorers"]["euclidian"]["scale_points"]
      @timeout = settings.elasticsearch["timeout"].to_i
      @helper_status["Elasticsearch"] = "OK!"
    rescue HelperError => e
      @helper_status["ElasticSearch"] = "ERROR: #{e.message}"
    end


    # Initialise Collections Helper
    begin
      @active_es_server = @redis_helper.get("oculus.active_es_server") || settings.elasticsearch["servers"].first
      @collections_helper = Oculus::Helpers::ElasticsearchHelper.new(@active_es_server, "collections", "collection", settings.elasticsearch["timeout"])
      @phrase_slop = params["p_slop"] ? params["p_slop"].to_i : settings.elasticsearch["phrase_slop"].to_i
      @dtw_radius = params["dtw_radius"] ? params["dtw_radius"].to_i : settings.elasticsearch["scorers"]["dtw"]["radius"].to_i
      @collections_helper.dtw_scale_points = settings.elasticsearch["scorers"]["dtw"]["scale_points"]
      @collections_helper.euclidian_scale_points = settings.elasticsearch["scorers"]["euclidian"]["scale_points"]
      @timeout = settings.elasticsearch["timeout"].to_i
      @helper_status["Collections"] = "OK!"
    rescue HelperError => e
      @helper_status["Collections"] = "ERROR: #{e.message}"
    end

    # Initialise Dygraph Helper
    begin
      @dygraph_helper = Oculus::Helpers::DygraphHelper.new
      @helper_status["Dygraph"] = "OK!"
    rescue HelperError => e
      @helper_status["Dygraph"] = "ERROR: #{e.message}"
    end

    # Initialise Fingerprint Helper
    begin
      @fingerprint_helper = Oculus::Helpers::FingerprintHelper.new
      @helper_status["Fingerprint"] = "OK!"
    rescue HelperError => e
      @helper_status["Fingerprint"] = "ERROR: #{e.message}"
    end

    # Initialise Skyline Helper
    begin
      @skyline_helper = Oculus::Helpers::RedisHelper.new(settings.skyline["host"], settings.skyline["port"])
      @metric_prefix = settings.skyline["metric_prefix"] || "metrics"
      @helper_status["Skyline"] = "OK!"
    rescue HelperError => e
      @helper_status["Skyline"] = "ERROR: #{e.message}"
    end

    @search_types = ["Euclidian", "FastDTW"]
    @last_import_time = @redis_helper.get("oculus.last_import_end") || "Not Found"
    @last_import_duration = @redis_helper.get("oculus.last_import_duration") || "Not Found"
  end

   configure do
     enable :sessions
   end
  # Base route
  get '/' do
    haml :index
  end

   get '/help' do
     haml :help_wrapper
   end

  get '/status' do
    haml :status
  end

  get '/supergraph' do
    haml :supergraph
  end

   get '/collections' do
     @collections = @collections_helper.get_collections
     @freetexts = {}
     @collections.each do |c|
       collection_name = c.collection_name
       @freetexts[collection_name] = @collections_helper.get_collection_freetext(collection_name).first.free_text
     end
     haml :collections
   end

   get '/collection/:collection' do
     @edit_mode = params[:edit]
     @collection = params[:collection]
     @name_clash = params[:nameclash]
     @collection_description = params["collection_description"]
     @collection_metrics = {}
     @collections_helper.get_collection(@collection).each do |c|
       datapoints = []
       u = MessagePack::Unpacker.new
       u.feed(Base64.decode64(c.msgpack_data))
       u.each do |obj|
         datapoints <<  obj
       end
       @collection_metrics[c.metric_name] = {
           :datapoints =>  @dygraph_helper.redis_datapoints_to_dygraph_format(datapoints)
       }
     end
     @collection_freetext = @collections_helper.get_collection_freetext(@collection).first.free_text
     haml :collection
   end

   get '/editcollection' do
     referer = request.env["HTTP_REFERER"] || ""
     unless referer.include?("/collection") and referer.include?("?edit=true")
       halt 403
     end
     old_collection_name = params[:old_collection_name]
     collection_name = params[:collection_name]
     collection_description = params[:collection_description]
     unless !old_collection_name.nil? and !collection_name.nil? and !collection_description.nil?
       halt 500
     end

     collection_names = @collections_helper.get_collections.map{|c|c.collection_name}.uniq
     if collection_names.include?(collection_name) and old_collection_name!= collection_name
       redirect "/collection/#{old_collection_name}?edit=true&nameclash=#{collection_name}&collection_description=#{URI.escape(params["collection_description"])}"
     end

     settings.elasticsearch["servers"].each do |s|
       collections_helper = Oculus::Helpers::ElasticsearchHelper.new(s, "collections", "collection", 30)
       collections_helper.update_collection_metrics(old_collection_name, collection_name)
       collections_helper.update_collection_freetext(old_collection_name,collection_name,Sanitize.clean(collection_description, Sanitize::Config::RELAXED))
     end
     sleep 1
     redirect "/collection/#{collection_name}"
   end

   get '/addtocollection' do
     if session[:tempcollection].nil?
       session[:tempcollection] = [params["metric_name"]]
      else
       session[:tempcollection] << params["metric_name"] unless session[:tempcollection].include?(params["metric_name"])
     end
   end

   get '/tempcollection' do
     @temp_collection = session[:tempcollection]
     haml :tempcollection, :layout => false
   end

   get '/clearcollection' do
     session[:tempcollection] = nil
   end

   get '/savecollection' do
     @referer = request.env["HTTP_REFERER"] || ""
     redis_results = Hash.new
     @metric_data = Hash.new
     @temp_collection = session[:tempcollection]
     @name_clash = params[:nameclash]
     @collection_description = params["collection_description"]
     formatted_names = @temp_collection.map{|n| "#{@metric_prefix}.#{n}"}
     redis_datapoints = @skyline_helper.mget(formatted_names)
     formatted_names.each_with_index do |r,index|
       redis_results[r] = []
       u = MessagePack::Unpacker.new
       u.feed(redis_datapoints[index])
       u.each do |obj|
         redis_results[r] <<  obj
       end
       @metric_data[r] = {
           :id => r.gsub("#{@metric_prefix}.",""),
           :datapoints => @dygraph_helper.redis_datapoints_to_dygraph_format(redis_results[r]),
           :fingerprint => @elasticsearch_helper.get_fingerprint(r),
           :values => @elasticsearch_helper.get_values(r),
           :msgpack_data => redis_datapoints[index]
       }
     end

     if params["collection_name"] && params["collection_description"]
       collection_names = @collections_helper.get_collections.map{|c|c.collection_name}.uniq
       if collection_names.include?(params["collection_name"])
         redirect "/savecollection?nameclash=#{params["collection_name"]}&collection_description=#{URI.escape(params["collection_description"])}"
       end
       settings.elasticsearch["servers"].each do |s|
           collections_helper = Oculus::Helpers::ElasticsearchHelper.new(s, "collections", "collection", 30)
           @metric_data.each do |name,values|
             collections_helper.index_collection(params["collection_name"],values[:id],values[:values],values[:fingerprint],Base64::encode64(values[:msgpack_data]))
           end
           collections_helper.index_collection_freetext(params["collection_name"],Sanitize.clean(params["collection_description"], Sanitize::Config::RELAXED))
       end
       session[:tempcollection] = nil
       sleep 1
       redirect "/collection/#{params["collection_name"]}"
     end
     haml :savecollection
   end

   get '/removefromcollection' do
     session[:tempcollection] = session[:tempcollection].select{|c| c != params["metric_name"]}
   end

   get '/deletecollection' do
     referer = request.env["HTTP_REFERER"] || ""
     unless  referer.include?("/collection")
       halt 403
     end
     settings.elasticsearch["servers"].each do |s|
       collections_helper = Oculus::Helpers::ElasticsearchHelper.new(s, "collections", "collection", 30)
       collections_helper.delete_collection(params["name"])
     end
   end

   get '/deletemetric' do
     referer = request.env["HTTP_REFERER"] || ""
     unless referer.include?("/collection") and referer.include?("?edit=true")
       halt 403
     end
     collection_name = params[:collection_name]
     metric_name = params[:metric_name]
     unless !collection_name.nil? and !metric_name.nil?
       halt 500
     end
     settings.elasticsearch["servers"].each do |s|
       collections_helper = Oculus::Helpers::ElasticsearchHelper.new(s, "collections", "collection", 30)
       collections_helper.delete_metric(collection_name,metric_name)
     end

     redirect "/collection/#{collection_name}"
   end

  get '/search' do
    @temp_collection = session[:tempcollection]
    @search_type = params["search_type"].downcase || "FastDTW"
    @page = params["page"] == "" ? 1 : params["page"]
    @filters = params["filters"].nil? ? [] : params["filters"].split(".")
    @phrase_slop = params["p_slop"] ? params["p_slop"].to_i : settings.elasticsearch["phrase_slop"].to_i
    @elasticsearch_helper.phrase_slop=@phrase_slop
    @dtw_radius = params["dtw_radius"] ? params["dtw_radius"].to_i : settings.elasticsearch["scorers"]["dtw"]["radius"].to_i
    @elasticsearch_helper.dtw_radius=@dtw_radius
    @size = 20
    @explain = settings.results_explain.to_i
    if params[:draw_values] != "" and !params[:draw_values].nil?
      @query = "Drawn Query"
      @formatted_query = "Drawn Query"
      raw_values = params[:draw_values].split(",").map{|v|v.to_f}
      @fingerprint = @fingerprint_helper.translate_metric_array(raw_values)
      @values = raw_values.join(" ")
    else
      @query = params["query"]
      @formatted_query = "#{@metric_prefix}.#{@query}"
      @fingerprint = @elasticsearch_helper.get_fingerprint(@formatted_query)
      @values = @elasticsearch_helper.get_values(@formatted_query)
    end
    data_fields = {"name" => @formatted_query, "fingerprint" => @fingerprint, "values" => @values}

    #Search collections
    @collections_results = @collections_helper.search("#{@search_type}",data_fields,[],{:from => (@page.to_i-1) * @size, :size => @size, :explain => @explain})
    @collections_results_count = @collections_results.total_entries
    @collections_took = @collections_results.response["took"]

    #Search metrics
    results = @elasticsearch_helper.search("#{@search_type}",data_fields,@filters,{:from => (@page.to_i-1) * @size, :size => @size, :explain => @explain})

    redis_results = Hash.new
    if @formatted_query == "Drawn Query"
      redis_names = results.map{|r|r.id}
    else
      redis_names = results.map{|r|r.id} << @formatted_query
    end
    redis_datapoints = @skyline_helper.mget(redis_names)
    redis_names.each_with_index do |r,index|
      redis_results[r] = []
      u = MessagePack::Unpacker.new
      u.feed(redis_datapoints[index])
      u.each do |obj|
        redis_results[r] <<  obj
      end
    end

    if params[:draw_values] == ""
      @datapoints = @dygraph_helper.redis_datapoints_to_dygraph_format(redis_results[@formatted_query])
    else
      @datapoints = nil
    end


    @results_count = results.total_entries
    @took = results.response["took"]

    @page_count = (@results_count / @size) + 1

    @dygraphs = results.map{|r| {:id => r.id.gsub("#{@metric_prefix}.",""), :fingerprint => r.fingerprint, :datapoints => @dygraph_helper.redis_datapoints_to_dygraph_format(redis_results[r.id]), :score => r._score, :explanation => r._explanation.nil? ? nil : JSON.pretty_generate(r._explanation) }}
    haml :search
  end

  get '/admin' do
    protected!
    @action = params["action"]
    @referer = request.env["HTTP_REFERER"] || ""
    if !@referer.include?("/admin") and !@action.nil?
      redirect '/admin'
    end
    case @action
      when "rc"
        settings.elasticsearch["servers"].each do |s|
          collections_helper = Oculus::Helpers::ElasticsearchHelper.new(s, "collections", "collection", settings.elasticsearch["timeout"])
          collections_helper.reinitialize_collections
        end
        @status = ["Collections Initialized!","success"]
    end
    haml :admin
  end

  not_found do
    haml :'404'
  end

  error do
      @error = "#{request.env['sinatra.error'].to_s}"
      haml :'500'
  end
end
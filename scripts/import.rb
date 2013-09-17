#!/usr/bin/env ruby

require "resque"
require "redis"
require "app_conf"
require File.join(File.dirname(__FILE__), '../helpers/graphite')
require File.join(File.dirname(__FILE__), '../helpers/elasticsearch')
require File.join(File.dirname(__FILE__), '../workers/redis_worker')

start_time = Time.now

config = AppConf.new
config.load(File.join(File.dirname(__FILE__), '../config/config.yml'))

#Get active search server and select next search server
oculus_redis = Redis.new(:host => config[:redis][:host], :port => config[:redis][:port])
current_es_server = oculus_redis.get("oculus.active_es_server")

if config[:elasticsearch][:servers].length == 0
  puts "ERROR: You need at least 1 ElasticSearch server in your config. Found 0."
  exit 1
elsif config[:elasticsearch][:servers].length == 1
  search_server = config[:elasticsearch][:servers].first
else
  search_server = config[:elasticsearch][:servers].select{|m| m != current_es_server}.sample
end

search_index = config[:elasticsearch][:index]
metric_prefix = config[:skyline][:metric_prefix] || "metrics"

puts "Active ES Server: #{current_es_server}"
puts "Next ES Server: #{search_server}"

#Recreate indexes
puts "Recreating indexes"

puts "#{search_server}: #{search_index}"
@elasticsearch_helper = Oculus::Helpers::ElasticsearchHelper.new(search_server,search_index,"metric",15)
@elasticsearch_helper.recreate_index
@elasticsearch_helper.update_mapping

#Create redis jobs

puts "Creating redis jobs..."
skyline_redis = Redis.new(:host => config[:skyline][:host], :port => config[:skyline][:port])
Resque.redis = "#{config[:redis][:host]}:#{config[:redis][:port]}"
slice_size = 500
puts "Getting unique metric names"
unique_metrics = skyline_redis.smembers("#{metric_prefix}.unique_metrics")
puts "Found #{unique_metrics.size} metric names"

unique_metrics.each_slice(slice_size).to_a.each do |t|
  Resque.enqueue(Oculus::Workers::RedisWorker, t, search_server, search_index, config[:skyline][:host], config[:skyline][:port])
end

#Monitor jobs until done
until Resque.size("process_redis_metrics") == 0
  sleep 5
  puts "#{Resque.working.length} workers working"
  puts "#{Resque.size("process_redis_metrics")} process_redis_metrics jobs left to run"
end
end_time = Time.now

puts "Setting active search server to #{search_server}"
#Set active server
oculus_redis.set("oculus.active_es_server",search_server)

duration = end_time - start_time

oculus_redis.set("oculus.last_import_end",end_time)
oculus_redis.set("oculus.last_import_duration",duration)

puts "Oculus import finished in #{duration} seconds"




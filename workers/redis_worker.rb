#!/usr/bin/env ruby

require "redis"
require "rubberband"
require "msgpack"
require File.join(File.dirname(__FILE__), '../helpers/fingerprint')
require File.join(File.dirname(__FILE__), '../helpers/elasticsearch')

module Oculus
  module Workers
    class RedisWorker
      @queue = "process_redis_metrics"
      def self.perform(metrics,es_url,index,skyline_host,skyline_port)
        @skyline_host = skyline_host
        @skyline_port = skyline_port
        @es_url = es_url
        @index = index
        bulk_index(metrics)
      end

      def self.bulk_index(metrics,oneoff=false)
        @redis = Redis.new(:host => @skyline_host, :port => @skyline_port)
        @elasticsearch_helper = Oculus::Helpers::ElasticsearchHelper.new(@es_url,@index,"metric",15)
        @fingerprint_helper = Oculus::Helpers::FingerprintHelper.new
        redis_metric_data = @redis.mget(metrics)
        metric_data = []

        redis_metric_data.each_with_index do |m,index|
          timeseries = []
          unless m.nil?
            begin
              u = MessagePack::Unpacker.new
              u.feed(m)
              u.each do |obj|
                timeseries << obj
              end
              data_values = timeseries.map{ |m| m ? m.last.to_f : nil }.compact
              fingerprint = @fingerprint_helper.translate_metric_array(data_values)
              metric_data << {:fingerprint => fingerprint, :values => data_values.join(" "), :id => metrics[index]} unless data_values.empty?
            rescue MessagePack::MalformedFormatError
              puts "I broke on #{metrics[index]}"
            rescue NoMethodError
              puts "I broke on #{metrics[index]}"
            end
          end
        end

        response = @elasticsearch_helper.bulk_index(metric_data)
        failed_ids = response["items"].select{|i| i["index"]["ok"] != true}
        unless failed_ids.empty?
          if !oneoff
            puts "#{Time.now}: #{metric_data.length} items were bulk indexed, #{failed_ids.length} items failed, reindexing."
            bulk_index(failed_ids,true)
          else
            puts "#{Time.now}: #{metric_data.length} items were bulk indexed, #{failed_ids.length} items failed for a second time. Not reindexing."
          end
        end
      end
    end
  end
end
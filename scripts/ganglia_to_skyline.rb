#!/usr/bin/env ruby

require "resque"
require "redis"
require "app_conf"
require File.join(File.dirname(__FILE__), '../helpers/graphite')
require File.join(File.dirname(__FILE__), '../helpers/elasticsearch')
require File.join(File.dirname(__FILE__), '../workers/graphite_worker')
require File.join(File.dirname(__FILE__), '../workers/redis_worker')

config = AppConf.new
config.load(File.join(File.dirname(__FILE__), '../config/config.yml'))

oculus_redis = Redis.new(:host => config[:redis][:host], :port => config[:redis][:port])
Resque.redis = "#{config[:redis][:host]}:#{config[:redis][:port]}"
graphite_server = "#{config[:graphite][:host]}"

#Start infinite import loop
loop do
  start_time = Time.now
  start_time_epoch_adjusted = start_time.to_i - 60

  puts "New import run started at #{start_time}"

  last_import_epoch = oculus_redis.get("oculus.last_ganglia_import_start").to_i

  if last_import_epoch == 0
    last_import_epoch = start_time_epoch_adjusted - 60
  end

  puts "Fetching metrics for the last #{start_time_epoch_adjusted - last_import_epoch} seconds"

  graphite_helper = Oculus::Helpers::GraphiteHelper.new(graphite_server)

  tree = graphite_helper.get_graphite_namespace_tree("ganglia.*")
  tree.each do |t|
    graphite_helper.get_graphite_namespace_tree("#{t["id"]}.*").each do |s|
      Resque.enqueue(Oculus::Workers::GraphiteWorker, graphite_server,"#{s["id"]}.*.sum",last_import_epoch,start_time_epoch_adjusted,config[:skyline][:host], config[:skyline][:listener_port])
    end
  end

  sleep 5

  until Resque.size("process_graphite_namespace").to_i == 0
    sleep 5
  end
  end_time = Time.now

  duration = end_time - start_time

  oculus_redis.set("oculus.last_ganglia_import_start",start_time_epoch_adjusted)

  puts "Oculus ganglia import finished in #{duration} seconds"
end



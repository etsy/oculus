require 'resque'
require 'socket'
require 'msgpack'
require File.join(File.dirname(__FILE__), '../helpers/graphite')

module Oculus
  module Workers
    class GraphiteWorker
      @queue = "process_graphite_namespace"

      def self.perform(graphite_server,namespace,last_start_time,start_time,skyline_host,skyline_port)
        graphite_helper = Oculus::Helpers::GraphiteHelper.new(graphite_server)

        #Grab and import data between last import time and current import start time
        unless namespace.include?("__SummaryInfo__")
          json = graphite_helper.get_graphite_json(namespace,last_start_time,start_time)
          json.each do |m|
            name = "#{m["target"]}"
            datapoints = m["datapoints"]
            s = UDPSocket.new

            datapoints.each do |d|
              message = ["#{name}",[d[1].to_f,d[0].to_f]].to_msgpack
              s.send(message, 0, skyline_host, skyline_port)
            end
          end
        end
      rescue Resque::TermException
        puts "Aieeeee, I are ded..."
      end
    end
  end
end

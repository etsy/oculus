require 'net/http'
require 'json'

module Oculus
  module Helpers
    class GraphiteHelper

      def initialize(hostname)
        @host=hostname
      end


      def get_graphite_json(namespace,time_from,time_to)
        begin
          params = URI.escape("target=#{namespace}&from=#{time_from}&until=#{time_to}")
          uri = URI.parse("#{@host}/render/?#{params}&rawData=1&format=json")
          http = Net::HTTP.new(uri.host, uri.port)
          http.read_timeout = 180
          JSON.parse(http.request(Net::HTTP::Get.new(uri.request_uri)).body)
        rescue SocketError => msg
          puts "SocketError: #{msg}"
          exit 3
        end
      end

      def get_graphite_namespace_tree(namespace)
        begin
          params = URI.escape("query=#{namespace}")
          uri = URI.parse("#{@host}/metrics/find/?#{params}&format=treejson")
          http = Net::HTTP.new(uri.host, uri.port)
          http.read_timeout = 180
          JSON.parse(http.request(Net::HTTP::Get.new(uri.request_uri)).body)
        rescue SocketError => msg
          puts "SocketError: #{msg}"
          exit 3
        end
      end
    end
  end
end
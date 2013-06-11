require 'redis'
module Oculus
  module Helpers
    class RedisHelper
      attr_accessor :host, :port

      def initialize(host, port)
        @host = host
        @port = port
        @client = Redis.new(:host => host, :port => port)
      end
      def mget(metrics)
        @client.mget(metrics)
      end

      def get(key)
        @client.get(key)
      end

      def set(key,value)
        @client.set(key,value)
      end
    end
  end
end
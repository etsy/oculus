module Oculus
  module Helpers
    class DygraphHelper
      def datapoints_to_dygraph_format(datapoints)
        datapoints.map{|d| [Time.at(d[1]).strftime("%Y/%m/%d %H:%M:%S"),d[0]]}
      end
      def redis_datapoints_to_dygraph_format(datapoints)
        datapoints.map{|d| [Time.at(d[0]).strftime("%Y/%m/%d %H:%M:%S"),d[1]]}
      end
    end
  end
end
module Oculus
  module Helpers
    class FingerprintHelper
      #This method expects an array of arrays of [value, timestamp]
      def translate_metric_array(data_values)
        fingerprint = []
        min_val = data_values.min
        max_val = data_values.max
        data_values.each do |v|
          fingerprint << scale_x(v,min_val,max_val) unless v.nil?
        end
        sda(fingerprint)
      end

      def scale_x(x,min_val,max_val)
        if x == 0 && min_val == 0 && max_val == 0
          0
        else
          scale = ((25/(max_val-min_val)) * (x-min_val))
          scale.nan? ? 0 : scale.ceil
        end
      end

      def e_print(fingerprint)
        e_print = []
        ref_point = [0,0,0]

        fingerprint.each_slice(3).to_a.each do |f|
          e_print << Math.sqrt(f.zip(ref_point).map { |x| (x[1] - x[0])**2 }.reduce(:+)).to_i.to_s
        end
        e_print
      end

      def sda(fingerprint)
        infinity = 1.0/0
        negative_infinity = -1.0/0
        alphabet = [
                      {:name => "sdec",
                       :lbound  => 4,
                       :ubound => infinity,
                      },
                      {:name => "dec",
                       :lbound  => 1,
                       :ubound => 4,
                      },
                      {:name => "s",
                       :lbound  => -1,
                       :ubound => 1,
                      },
                      {:name => "inc",
                       :lbound  => -4,
                       :ubound => -1,
                      },
                      {:name => "sinc",
                       :lbound  => negative_infinity,
                       :ubound => -4,
                      }
                    ]

        pairs_diffs = (0...fingerprint.length-1).map{|i| fingerprint[i,2]}.map{|p|p[0] - p[1]}
        sda_fingerprint = []

        pairs_diffs.each do |d|
          alphabet.each do |a|
            if (a[:lbound]..a[:ubound]) === d
              sda_fingerprint << a[:name]
              break
            end
          end
        end
        (sda_fingerprint - ["s"]).join(" ")
      end

      def int_to_alpha(num)
        ("A".."Z").to_a[num]
      end
    end
  end
end
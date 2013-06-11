require 'rubberband'

module Oculus
  module Helpers
    class ElasticsearchHelper

      attr_accessor :url, :timeout, :index, :phrase_slop, :dtw_radius, :dtw_scale_points, :euclidian_scale_points

      def initialize(url, index, type, timeout)
        @url = url
        @index = index
        @timeout = timeout
        @type = type
        @client = ElasticSearch.new(@url, :index => index, :type => type, :timeout => @timeout)
      end

      def reinitialize_collections
        begin
          @client.delete_index(@index)
        rescue ElasticSearch::RequestError
          puts "Index name #{index} not found, cannot delete."
        end

        #Create index
        @client.create_index(@index,
                              {
                                  "number_of_shards" => 5,
                                  "number_of_replicas" => 0
                              })

        #Create mapping
        client = ElasticSearch.new(@url, :index => index, :type => "collection", :timeout => @timeout)
        client.update_mapping({
                                    "collection" => {
                                        "_all" => {"enabled" => false},
                                        "properties" => {
                                            "metric_name" => {
                                                "type" => "string",
                                                "index" => "not_analyzed",
                                                "include_in_all" => false
                                            },
                                            "collection_name" => {
                                                "type" => "multi_field",
                                                "fields" => {
                                                    "collection_name" => {
                                                        "type" => "string",
                                                        "analyzer" => "whitespace"
                                                    },
                                                    "untouched" => {
                                                        "type" => "string",
                                                        "index" => "not_analyzed",
                                                        "include_in_all" => false
                                                    }
                                                }
                                            },
                                            "msgpack_data" => {
                                                "type" => "string",
                                                "index" => "not_analyzed",
                                                "include_in_all" => false
                                            },
                                            "fingerprint" => {
                                                "type" => "multi_field",
                                                "fields" => {
                                                    "fingerprint" => {
                                                        "type" => "string",
                                                        "analyzer" => "whitespace"
                                                    },
                                                    "untouched" => {
                                                        "type" => "string",
                                                        "index" => "not_analyzed",
                                                        "include_in_all" => false
                                                    }
                                                }
                                            },
                                            "values" => {
                                                "type" => "multi_field",
                                                "fields" => {
                                                    "values" => {
                                                        "type" => "string",
                                                        "include_in_all" => false
                                                    },
                                                    "untouched" => {
                                                        "type" => "string",
                                                        "index" => "not_analyzed",
                                                        "include_in_all" => false
                                                    }
                                                }
                                            }
                                        }
                                    }
                                })

        #Update freetext mapping
        client = ElasticSearch.new(@url, :index => index, :type => "collection_freetext", :timeout => @timeout)
        client.update_mapping({
                                   "collection_freetext" => {
                                       "_all" => {"enabled" => false},
                                       "properties" => {
                                           "collection_name" => {
                                               "type" => "multi_field",
                                               "fields" => {
                                                   "collection_name" => {
                                                       "type" => "string",
                                                       "analyzer" => "whitespace"
                                                   },
                                                   "untouched" => {
                                                       "type" => "string",
                                                       "index" => "not_analyzed",
                                                       "include_in_all" => false
                                                   }
                                               }
                                           },
                                           "free_text" => {
                                               "type" => "string",
                                               "index" => "not_analyzed",
                                               "include_in_all" => false
                                           },
                                       }
                                   }
                               })
        end

      def recreate_index
        begin
          @client.delete_index(@index)
        rescue ElasticSearch::RequestError
          puts "Index name #{@index} not found, cannot delete."
        end
        @client.create_index(@index,
                              {
                                  "number_of_shards" => 5,
                                  "number_of_replicas" => 0,
                                  "analysis" => {
                                      "analyzer" => {
                                        "graphite_namespace" => {
                                          "tokenizer" => "graphite_namespace"
                                        }
                                      },
                                      "tokenizer" => {
                                        "graphite_namespace" => {
                                          "pattern" => "[^.]+",
                                          "type" => "pattern",
                                          "group" => 0
                                        }
                                      }
                                  }
                              })
      end

      def update_mapping
        @client.update_mapping({
                                  "metric" => {
                                      "_id" => {
                                        "path" => "id"
                                      },
                                      "_all" => {"enabled" => false},
                                      "properties" => {
                                        "datapoints" => {
                                          "type" => "double"
                                        },
                                        "fingerprint" => {
                                          "type" => "multi_field",
                                          "fields" => {
                                            "fingerprint" => {
                                              "type" => "string",
                                              "analyzer" => "whitespace"
                                            },
                                            "untouched" => {
                                              "type" => "string",
                                              "index" => "not_analyzed",
                                              "include_in_all" => false
                                            }
                                          }
                                        },
                                        "id" => {
                                          "type" => "multi_field",
                                          "fields" => {
                                            "id" => {
                                              "type" => "string",
                                              "analyzer" => "graphite_namespace"
                                            },
                                            "untouched" => {
                                              "type" => "string",
                                              "index" => "not_analyzed",
                                              "include_in_all" => false
                                            }
                                          }
                                        },
                                        "values" => {
                                          "type" => "multi_field",
                                          "fields" => {
                                            "values" => {
                                              "type" => "string",
                                              "include_in_all" => false
                                            },
                                            "untouched" => {
                                              "type" => "string",
                                              "index" => "not_analyzed",
                                              "include_in_all" => false
                                            }
                                          }
                                        }
                                      }
                                  }
                                })
      end

      #This method expects an array of arrays of [value, timestamp]
      def index_item(name, datapoints, values, fingerprint, trend)
        @client.index({:datapoints => datapoints, :fingerprint => fingerprint, :values => values, :trend => trend, :id => name})
      end

      def index_collection(collection_name, metric_name, values, fingerprint, msgpack_data)
        @client.index({:collection_name => collection_name, :metric_name => metric_name, :values => values, :fingerprint => fingerprint, :msgpack_data => msgpack_data})
      end

      def index_collection_freetext(collection_name, free_text)
        @client.index({:collection_name => collection_name, :free_text => free_text}, :type => "collection_freetext" )
      end

      def bulk_index(items)
        @client.bulk do
          items.each do |i|
            unless i.nil?
              @client.index({:fingerprint => i[:fingerprint], :values => i[:values], :id => i[:id]}) unless i[:values].empty?
            end
          end
        end
      end

      def bulk_index_with_datapoints(items)
        @client.bulk do
          items.each do |i|
            unless i.nil?
              @client.index({:fingerprint => i[:fingerprint], :values => i[:values], :datapoints => i[:datapoints], :id => i[:id]}) unless i[:values].empty?
            end
          end
        end
      end
      
      def match_query(field_name, field_value)
        {:match => {"#{field_name}" => {:query => field_value, :type => "phrase", :slop => @phrase_slop}}}
      end

      def boolean_query(filters,operator,*args)
        {:bool => {"#{operator}" => args,:must_not => filters}}
      end
      
      def dtw_custom_score_query(query, field_name, field_value, radius, scale_points)
        {:custom_score => {
            :query => query,
            :script => "oculus_dtw",
            :params => {
              :query_value => field_value,
              :query_field => "#{field_name}.untouched",
              :radius => radius,
              :scale_points => scale_points
            },
            :lang => "native"
            }
        }
      end

      def euclidian_custom_score_query(query, field_name, field_value, scale_points)
        {:custom_score => {
            :query => query,
            :script => "oculus_euclidian",
            :params => {
              :query_value => field_value,
              :query_field => "#{field_name}.untouched",
              :scale_points => scale_points
            },
            :lang => "native"
            }
        }
      end

      def boolean_filters(filters)
        filter_array = []

        filters.each do |f|
           filter_array << {:term => { :id => f }}
        end
        filter_array
      end

      def search(search_type,fields,filters,options = {})

        bucket_query = boolean_query(boolean_filters(filters),
                                     "must",
                                     match_query("fingerprint",fields["fingerprint"])
                                     )

         case search_type
         when "fastdtw"
           custom_score_query = dtw_custom_score_query(bucket_query,"values",fields["values"],@dtw_radius,@dtw_scale_points)
         when "euclidian"
           custom_score_query = euclidian_custom_score_query(bucket_query,"values",fields["values"],@euclidian_scale_points)
         else
           custom_score_query = euclidian_custom_score_query(bucket_query,"values",fields["values"],@euclidian_scale_points)
         end

          #@@client.search({:query => bucket_query
          #              },options)
          @client.search({:query => custom_score_query,
                       :sort => [{ "_score" => "asc" }]
                      },options)
      end
      
      def get_fingerprint(name)
        @client.get(name).fingerprint
      end

      def get_trend(name)
        @client.get(name).trend
      end

      def get_datapoints(name)
        @client.get(name).datapoints
      end

      def get_values(name)
        @client.get(name).values
      end

      def get_collections
        @client.search({:query => {
                          :match_all => {  }
                          },
                        :facets => {
                          :collection => {
                            :terms => {
                              :field => "collection_name.untouched",
                              :all_terms => true
                            }
                          }
                        }
                        })
      end

      def get_collection(name)
        @client.search({:query =>
                            {:term => { "collection_name.untouched" => name }}},{:type => "collection"})
      end

      def get_collection_freetext(name)
        @client.search({:query =>
                            {:term => { "collection_name.untouched" => name }}},{:type => "collection_freetext"})
      end

      def update_collection_metrics(old_name,name)
        metrics = get_collection(old_name)
        metrics.each do |m|
          @client.index({:collection_name => name, :metric_name => m.metric_name, :values => m.values, :fingerprint => m.fingerprint, :msgpack_data => m.msgpack_data}, :id => m.id)
        end

      end

      def update_collection_freetext(old_name,name,description)
        freetext = @client.search({:query => {:term => { "collection_name.untouched" => old_name }}},{:type => "collection_freetext"}).first
        @client.index({:collection_name => name, :free_text => description}, :type => "collection_freetext", :id => freetext.id )
      end

      def delete_metric(collection_name,metric_name)
        results = @client.search({:query =>
                                     {:bool => {
                                         :must => [
                                            {:term => { "collection_name.untouched" => collection_name }},
                                            {:term => { :metric_name => metric_name }}
                                         ]
                                     }}
                                })
        if results.length > 1
          raise "Delete metric got #{results.length} results, expected 1."
        else
          id = results.first.id
          @client.delete(id)
        end
      end

      def delete_collection(name)
        @client.bulk do
          get_collection(name).each do |collection|
            @client.delete(collection.id)
          end
          get_collection_freetext(name).each do |collection|
            @client.delete(collection.id)
          end
        end
      end
    end
  end
end

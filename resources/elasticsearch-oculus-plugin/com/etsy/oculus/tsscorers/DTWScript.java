 package com.etsy.oculus.tsscorers;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;
import org.elasticsearch.script.AbstractDoubleSearchScript;
import com.timeseries.TimeSeries;
import com.util.DistanceFunction;
import com.util.DistanceFunctionFactory;
import com.dtw.TimeWarpInfo;
import java.util.ArrayList;
import java.util.Collections;

import java.util.Map;
import java.lang.Math;

public class DTWScript extends AbstractDoubleSearchScript {

    private String query;
    private String comparison_field;
    private Integer radius;
    private Double scalepoints;

    public DTWScript(@Nullable Map<String,Object> params){
      query = (String) params.get("query_value");
      comparison_field = (String) params.get("query_field");
      radius = (Integer) params.get("radius");
      scalepoints = (double) ((Integer) params.get("scale_points"));
    }

    @Override public double runAsDouble() {

      try{
          String current_value = doc().field(comparison_field).stringValue();
      
          String[] strQueryArray = query.split(" ");
          ArrayList intQueryArrayList = new ArrayList();
          for(int i = 0; i < strQueryArray.length; i++) {
              intQueryArrayList.add(Double.valueOf(strQueryArray[i]));
          }
      
          String[] strComparisonArray = current_value.split(" ");
          ArrayList intComparisonArrayList = new ArrayList();
          for(int i = 0; i < strComparisonArray.length; i++) {
              intComparisonArrayList.add(Double.valueOf(strComparisonArray[i]));
          }
      
          final TimeSeries tsI = new TimeSeries(scaleArrayList(intQueryArrayList,scalepoints), false, false, ',');
          final TimeSeries tsJ = new TimeSeries(scaleArrayList(intComparisonArrayList,scalepoints), false, false, ',');
            
          DistanceFunction distFn = DistanceFunctionFactory.getDistFnByName("EuclideanDistance");      
      
          final TimeWarpInfo info = com.dtw.FastDTW.getWarpInfoBetween(tsI, tsJ, radius, distFn);
      
          return (double) info.getDistance();
      }catch (NullPointerException e){
          return 9999999;
      }
    }
    
    public ArrayList scaleArrayList(ArrayList a, Double scalepoints){
      ArrayList scaled = new ArrayList();
      Double min_value = (Double) Collections.min(a);
      Double max_value = (Double) Collections.max(a);
      
      for (int i = 0; i < a.size(); i++) {
          scaled.add(((scalepoints/(max_value-min_value)) * ((Double) a.get(i)-min_value)));
      };
      return scaled;
    }
}
package com.etsy.oculus.tsscorers;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;
import org.elasticsearch.script.AbstractDoubleSearchScript;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.lang.Math;
import java.io.*;

public class EuclidianScript extends AbstractDoubleSearchScript {

    private String query;
    private String comparison_field;
    private Double scalepoints;

    public EuclidianScript(@Nullable Map<String,Object> params){
      query = (String) params.get("query_value");
      comparison_field = (String) params.get("query_field");
      scalepoints = (double) ((Integer) params.get("scale_points"));
    }

    @Override public double runAsDouble(){
      
      try{ 
          String current_value = doc().field(comparison_field).stringValue();
      
          String[] strQueryArray = new String[0]; 
          strQueryArray = query.split(" ");
          ArrayList intQueryArrayList = new ArrayList();
          for(int i = 0; i < strQueryArray.length; i++) {
              intQueryArrayList.add(Double.valueOf(strQueryArray[i]));
          }
      
          String[] strComparisonArray = new String[0];  
          strComparisonArray = current_value.split(" ");
          ArrayList intComparisonArrayList = new ArrayList();
          for(int i = 0; i < strComparisonArray.length; i++) {
              intComparisonArrayList.add(Double.valueOf(strComparisonArray[i]));
          }
          return (double) euclidianDistance(scaleArrayList(intComparisonArrayList,scalepoints), scaleArrayList(intQueryArrayList,scalepoints));
      }catch (NullPointerException e){
          return 9999999;
      }
    }
    
    public Double euclidianDistance( ArrayList m, ArrayList n) {
      Double distance = 0.0;
      for (int i = 0; i < Math.min(m.size(),n.size()); i++) {
          distance = distance + Math.pow(((Double) m.get(i) - (Double) n.get(i)),2);
      };
      return distance;
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
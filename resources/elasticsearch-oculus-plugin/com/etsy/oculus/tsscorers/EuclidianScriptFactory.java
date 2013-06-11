package com.etsy.oculus.tsscorers;

import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;
import com.etsy.oculus.tsscorers.EuclidianScript;

import java.util.Map;

public class EuclidianScriptFactory implements NativeScriptFactory {

  @Override public ExecutableScript newScript (@Nullable Map<String,Object> params){
    return new EuclidianScript(params);
  }
}


/*
 * SineWave.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.timeseries;

import java.util.Random;

/**
 * This class...
 *
 * @author Stan Salvador, stansalvador@hotmail.com
 * @version last changed: Jun 30, 2004
 * @see
 * @since Jun 30, 2004
 */

public class SineWave extends TimeSeries
{
   final private static Random rand = new Random();


   // CONSTRUCTORS
   public SineWave(int length, double cycles, double noise)
   {
      super(1);  // 1 dimensional TimeSeries

     // final Random rand = new Random();

      for (int x=0; x<length; x++)
      {
         final double nextPoint = Math.sin((double)x/length*2.0*Math.PI*cycles) + rand.nextGaussian()*noise;
         super.addLast((int)x, new TimeSeriesPoint(new double[] {nextPoint}));
      }
   }


   // PUBLIC FUNCTIONS


}  // end class SineWave

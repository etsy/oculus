/*
 * FastDTW.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.dtw;

import com.timeseries.TimeSeries;
import com.timeseries.PAA;
import com.util.DistanceFunction;


public class FastDTW
{
   // CONSTANTS
   final static int DEFAULT_SEARCH_RADIUS = 1;


   public static double getWarpDistBetween(TimeSeries tsI, TimeSeries tsJ, DistanceFunction distFn)
   {
      return fastDTW(tsI, tsJ, DEFAULT_SEARCH_RADIUS, distFn).getDistance();
   }


   public static double getWarpDistBetween(TimeSeries tsI, TimeSeries tsJ, int searchRadius, DistanceFunction distFn)
   {
      return fastDTW(tsI, tsJ, searchRadius, distFn).getDistance();
   }


   public static WarpPath getWarpPathBetween(TimeSeries tsI, TimeSeries tsJ, DistanceFunction distFn)
   {
      return fastDTW(tsI, tsJ, DEFAULT_SEARCH_RADIUS, distFn).getPath();
   }


   public static WarpPath getWarpPathBetween(TimeSeries tsI, TimeSeries tsJ, int searchRadius, DistanceFunction distFn)
   {
      return fastDTW(tsI, tsJ, searchRadius, distFn).getPath();
   }


   public static TimeWarpInfo getWarpInfoBetween(TimeSeries tsI, TimeSeries tsJ, int searchRadius, DistanceFunction distFn)
   {
      return fastDTW(tsI, tsJ, searchRadius, distFn);
   }


   private static TimeWarpInfo fastDTW(TimeSeries tsI, TimeSeries tsJ, int searchRadius, DistanceFunction distFn)
   {
      if (searchRadius < 0)
         searchRadius = 0;

      final int minTSsize = searchRadius+2;

      if ( (tsI.size() <= minTSsize) || (tsJ.size()<=minTSsize) )
      {
         // Perform full Dynamic Time Warping.
         return DTW.getWarpInfoBetween(tsI, tsJ, distFn);
      }
      else
      {
         final double resolutionFactor = 2.0;

         final PAA shrunkI = new PAA(tsI, (int)(tsI.size()/resolutionFactor));
         final PAA shrunkJ = new PAA(tsJ, (int)(tsJ.size()/resolutionFactor));

          // Determine the search window that constrains the area of the cost matrix that will be evaluated based on
          //    the warp path found at the previous resolution (smaller time series).
          final SearchWindow window = new ExpandedResWindow(tsI, tsJ, shrunkI, shrunkJ,
                                                            FastDTW.getWarpPathBetween(shrunkI, shrunkJ, searchRadius, distFn),
                                                            searchRadius);

         // Find the optimal warp path through this search window constraint.
         return DTW.getWarpInfoBetween(tsI, tsJ, window, distFn);
      }  // end if
   }  // end recFastDTW(...)

}  // end class fastDTW

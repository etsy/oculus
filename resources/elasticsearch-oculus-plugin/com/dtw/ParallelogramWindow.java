/*
 * ParallelogramWindow.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.dtw;

import com.timeseries.TimeSeries;


public class ParallelogramWindow extends SearchWindow
{

   // CONSTRUCTOR
   public ParallelogramWindow(TimeSeries tsI, TimeSeries tsJ, int searchRadius)
   {
      super(tsI.size(), tsJ.size());

      // Find the coordinates of the parallelogram's corners..other than (minI,minJ) and (maxI, maxJ)
      final double upperCornerI = Math.max(maxI()/2.0-searchRadius*((double)maxI()/maxJ()), minI());
      final double upperCornerJ = Math.min(maxJ()/2.0+searchRadius*((double)maxJ()/maxI()), maxJ());
      final double lowerCornerI = Math.min(maxI()/2.0+searchRadius*((double)maxI()/maxJ()), maxI());
      final double lowerCornerJ = Math.max(maxJ()/2.0-searchRadius*((double)maxJ()/maxI()), minJ());

      // For each column determine the minimum and maximum row ranges that are in the paralellogram's window.
      for (int i=0; i<tsI.size(); i++)
      {
         final int minJ;
         final int maxJ;
         final boolean isIlargest = tsI.size() >= tsJ.size(); 

         if (i < upperCornerI)// left side of upper line
         {
            if (isIlargest)
            {
               final double interpRatio = i / upperCornerI;
               maxJ = (int)Math.round(interpRatio*upperCornerJ);
            }
            else
            {
               final double interpRatio = (i+1) / upperCornerI;
               maxJ = (int)Math.round(interpRatio*upperCornerJ)-1;
            }  // end if
         }
         else  // right side of upper line
         {

            if (isIlargest)
            {
               final double interpRatio = (i-upperCornerI) / (maxI()-upperCornerI);
               maxJ = (int)Math.round(upperCornerJ + interpRatio*(maxJ()-upperCornerJ));
            }
            else
            {
               final double interpRatio = (i+1-upperCornerI) / (maxI()-upperCornerI);
               maxJ = (int)Math.round(upperCornerJ + interpRatio*(maxJ()-upperCornerJ))-1;
            }  // end if
         }  // end if

         if (i <= lowerCornerI)// left side of lower line
         {

            final double interpRatio = i / lowerCornerI;
            minJ = (int)Math.round(interpRatio*lowerCornerJ);
         }
         else // right side of lower line
         {

            final double interpRatio = (i-lowerCornerI) / (maxI()-lowerCornerI);
            minJ = (int)Math.round(lowerCornerJ + interpRatio*(maxJ()-lowerCornerJ));
         }  // end if

         super.markVisited(i, minJ);
         super.markVisited(i, maxJ);
      }  // end for loop
   }  // end Constructor


}  // end class ParallelogramWindow

/*
 * ExpandedResWindow.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.dtw;

import com.timeseries.TimeSeries;
import com.timeseries.PAA;
import com.matrix.ColMajorCell;


public class ExpandedResWindow extends SearchWindow
{

   public ExpandedResWindow(TimeSeries tsI, TimeSeries tsJ, PAA shrunkI, PAA shrunkJ,
                            WarpPath shrunkWarpPath, int searchRadius)
   {
      // Initialize the private data in the super class.
      super(tsI.size(), tsJ.size());

      // Variables to keep track of the current location of the higher resolution projected path.
      int currentI = shrunkWarpPath.minI();
      int currentJ = shrunkWarpPath.minJ();

      // Variables to keep track of the last part of the low-resolution warp path that was evaluated
      //    (to determine direction).
      int lastWarpedI = Integer.MAX_VALUE;
      int lastWarpedJ = Integer.MAX_VALUE;

      // For each part of the low-resolution warp path, project that path to the higher resolution by filling in the
      //    path's corresponding cells at the higher resolution.
      for (int w=0; w<shrunkWarpPath.size(); w++)
      {
         final ColMajorCell currentCell = shrunkWarpPath.get(w);
         final int warpedI = currentCell.getCol();
         final int warpedJ = currentCell.getRow();

         final int blockISize = shrunkI.aggregatePtSize(warpedI);
         final int blockJSize = shrunkJ.aggregatePtSize(warpedJ);

         // If the path moved up or diagonally, then the next cell's values on the J axis will be larger.
         if (warpedJ > lastWarpedJ)
            currentJ += shrunkJ.aggregatePtSize(lastWarpedJ);

         // If the path moved up or diagonally, then the next cell's values on the J axis will be larger.
         if (warpedI > lastWarpedI)
            currentI += shrunkI.aggregatePtSize(lastWarpedI);

         // If a diagonal move was performed, add 2 cells to the edges of the 2 blocks in the projected path to create
         //    a continuous path (path with even width...avoid a path of boxes connected only at their corners).
         //                        |_|_|x|x|     then mark      |_|_|x|x|
         //    ex: projected path: |_|_|x|x|  --2 more cells->  |_|X|x|x|
         //                        |x|x|_|_|        (X's)       |x|x|X|_|
         //                        |x|x|_|_|                    |x|x|_|_|
         if ((warpedJ>lastWarpedJ) && (warpedI>lastWarpedI))
         {
            super.markVisited(currentI-1, currentJ);
            super.markVisited(currentI, currentJ-1);
         }  // end if

         // Fill in the cells that are created by a projection from the cell in the low-resolution warp path to a
         //    higher resolution.
         for (int x=0; x<blockISize; x++)
         {
            super.markVisited(currentI+x, currentJ);
            super.markVisited(currentI+x, currentJ+blockJSize-1);
         }  // end for loop

         // Record the last position in the warp path so the direction of the path can be determined when the next
         //    position of the path is evaluated.
         lastWarpedI = warpedI;
         lastWarpedJ = warpedJ;
      }  // end for loop


      // Expand the size of the projected warp path by the specified width.
      super.expandWindow(searchRadius);
   }  // end Constructor


}  // end class ExpandedResWindow





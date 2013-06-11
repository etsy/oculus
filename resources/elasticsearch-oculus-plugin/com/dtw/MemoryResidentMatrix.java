/*
 * MemoryResidentMatrix.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.dtw;


class MemoryResidentMatrix implements CostMatrix
{
   // CONSTANTS
   private static final double OUT_OF_WINDOW_VALUE = Double.POSITIVE_INFINITY;


   // PRIVATE DATA
   private final SearchWindow window;
   private double[] cellValues;
   private int[] colOffsets;


   // CONSTRUCTOR
   MemoryResidentMatrix(SearchWindow searchWindow)
   {
      window = searchWindow;
      cellValues = new double[window.size()];
      colOffsets = new int[window.maxI()+1];

      // Fill in the offset matrix
      int currentOffset = 0;
      for (int i=window.minI(); i<=window.maxI(); i++)
      {
         colOffsets[i] = currentOffset;
         currentOffset += window.maxJforI(i)-window.minJforI(i)+1;
      }
   }  // end Constructor



   // PUBLIC FUNCTIONS
   public void put(int col, int row, double value)
   {
      if ((row<window.minJforI(col)) || (row>window.maxJforI(col)))
      {
         throw new InternalError("CostMatrix is filled in a cell (col="+col+", row="+row+") that is not in the "+
                                 "search window");
      }
      else
         cellValues[colOffsets[col]+row-window.minJforI(col)] = value;
   }


   public double get(int col, int row)
   {
      if ((row<window.minJforI(col)) || (row>window.maxJforI(col)))
         return OUT_OF_WINDOW_VALUE;
      else
         return cellValues[colOffsets[col]+row-window.minJforI(col)];
   }


   public int size()
   {
      return cellValues.length;
   }

}  // end class MemoryResidentMatrix

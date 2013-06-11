/*
 * CostMatrix.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.dtw;


interface CostMatrix
{
   public void put(int col, int row, double value);

   public double get(int col, int row);

   public int size();

}  // end interface CostMatrix

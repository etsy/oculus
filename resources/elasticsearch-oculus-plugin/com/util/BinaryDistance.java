/*
 * Arrays.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.util;


public class BinaryDistance implements DistanceFunction
{
   public BinaryDistance()
   {
      
   }
   
   
   public double calcDistance(double[] vector1, double[] vector2)
   {
      if (vector1.length != vector2.length)
         throw new InternalError("ERROR:  cannot calculate the distance "
                                 + "between vectors of different sizes.");
      else if (java.util.Arrays.equals(vector1, vector2))
      {
         return 0.0;
      }
      else
      {
         return 1.0;
      }   // end if
   }  // end class binaryDist(..)

}
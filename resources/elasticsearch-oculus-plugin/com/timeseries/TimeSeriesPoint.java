/*
 * TimeSeriesPoint.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.timeseries;

import java.util.Collection;
import java.util.Iterator;
import java.math.BigInteger;


public class TimeSeriesPoint
{
   // PRIVATE DATA
   private double[] measurements;
   private int hashCode;


   // CONSTRUCTORS
   public TimeSeriesPoint(double[] values)
   {
      hashCode = 0;
      measurements = new double[values.length];
      for (int x=0; x<values.length; x++)
      {
         hashCode += new Double(values[x]).hashCode();
         measurements[x] = values[x];
      }
   }


   public TimeSeriesPoint(Collection values)
   {
      measurements = new double[values.size()];
      hashCode = 0;

      final Iterator i = values.iterator();
      int index = 0;
      while (i.hasNext())
      {
         final Object nextElement = i.next();
         if (nextElement instanceof Double)
            measurements[index] = ((Double)nextElement).doubleValue();
         else if (nextElement instanceof Integer)
            measurements[index] = ((Integer)nextElement).doubleValue();
         else if (nextElement instanceof BigInteger)
            measurements[index] = ((BigInteger)nextElement).doubleValue();
         else
            throw new InternalError("ERROR:  The element " + nextElement +
                                    " is not a valid numeric type");

         hashCode += new Double(measurements[index]).hashCode();
         index++;
      }  // end while loop
   }  // end constructor



   // FUNCTIONS
   public double get(int dimension)
   {
      return measurements[dimension];
   }


   public void set(int dimension, double newValue)
   {
      hashCode -= new Double(measurements[dimension]).hashCode();
      measurements[dimension] = newValue;
      hashCode += new Double(newValue).hashCode();
   }


   public double[] toArray()
   {
      return measurements;
   }


   public int size()
   {
      return measurements.length;
   }


   public String toString()
   {
      String outStr = "(";
      for (int x=0; x<measurements.length; x++)
      {
         outStr += measurements[x];
         if (x < measurements.length-1)
            outStr += ",";
      }
      outStr += ")";

      return outStr;
   }  // end toString()


   public boolean equals(Object o)
   {
      if (this == o)
         return true;
      else if (o instanceof TimeSeriesPoint)
      {
         final double[] testValues = ((TimeSeriesPoint)o).toArray();
         if (testValues.length == measurements.length)
         {
            for (int x=0; x<measurements.length; x++)
               if (measurements[x] != testValues[x])
                  return false;

            return true;
         }
         else
            return false;
      }
      else
         return false;
   }  // end public boolean equals


   public int hashCode()
   {
      return this.hashCode;
   }

}  // end class TimeSeriesPoint

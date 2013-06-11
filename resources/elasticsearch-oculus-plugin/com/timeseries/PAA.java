/*
 * PAA.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.timeseries;


public class PAA extends TimeSeries
{
   // PRIVATE DATA
   private int[] aggPtSize;  // ArrayList of Integer
   private final int originalLength;




   public PAA(TimeSeries ts, int shrunkSize)
   {
      if (shrunkSize > ts.size())
         throw new InternalError("ERROR:  The size of an aggregate representation may not be largerr than the \n" +
                                 "original time series (shrunkSize=" + shrunkSize + " , origSize=" + ts.size() + ").");

      if (shrunkSize <= 0)
         throw new InternalError("ERROR:  The size of an aggregate representation must be greater than zero and \n" +
                                 "no larger than the original time series.");

      // Initialize private data.
      this.originalLength = ts.size();
      this.aggPtSize = new int[shrunkSize];

      // Ensures that the data structure storing the time series will not need
      //    to be expanded more than once.  (not necessary, for optimization)
      super.setMaxCapacity(shrunkSize);

      // Initialize the new aggregate time series.
      this.setLabels(ts.getLabels());

      // Determine the size of each sampled point. (may be a fraction)
      final double reducedPtSize = (double)ts.size()/(double)shrunkSize;

      // Variables that keep track of the range of points being averaged into a single point.
      int ptToReadFrom = 0;
      int ptToReadTo;


      // Keep averaging ranges of points into aggregate points until all of the data is averaged.
      while (ptToReadFrom < ts.size())
      {
         ptToReadTo = (int)Math.round(reducedPtSize*(this.size()+1))-1;   // determine end of current range
         final int ptsToRead = ptToReadTo-ptToReadFrom+1;

         // Keep track of the sum of all the values being averaged to create a single point.
         double timeSum = 0.0;
         final double[] measurementSums = new double[ts.numOfDimensions()];

         // Sum all of the values over the range ptToReadFrom...ptToReadFrom.
         for (int pt=ptToReadFrom; pt<=ptToReadTo; pt++)
         {
            final double[] currentPoint = ts.getMeasurementVector(pt);

            timeSum += ts.getTimeAtNthPoint(pt);

            for (int dim=0; dim<ts.numOfDimensions(); dim++)
               measurementSums[dim] += currentPoint[dim];
         }  // end for loop

         // Determine the average value over the range ptToReadFrom...ptToReadFrom.
         timeSum = timeSum / ptsToRead;
         for (int dim=0; dim<ts.numOfDimensions(); dim++)
               measurementSums[dim] = measurementSums[dim] / ptsToRead;   // find the average of each measurement

         // Add the computed average value to the aggregate approximation.
         this.aggPtSize[super.size()] = ptsToRead;
         this.addLast(timeSum, new TimeSeriesPoint(measurementSums));

         ptToReadFrom = ptToReadTo + 1;    // next window of points to average startw where the last window ended
      }  // end while loop
   }  // end Constructor


   public int originalSize()
   {
      return originalLength;
   }


   public int aggregatePtSize(int ptIndex)
   {
      return aggPtSize[ptIndex];
   }


   public String toString()
   {
      return "(" + this.originalLength + " point time series represented as " + this.size() + " points)\n" +
             super.toString();
   }  // end toString()


}  // end class PAA

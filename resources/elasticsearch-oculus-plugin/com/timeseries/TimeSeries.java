/*
 * TimeSeries.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.timeseries;

import com.util.Arrays;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.File;


public class TimeSeries
{
   private static final int[] ZERO_ARRAY = new int[0];
   private static final boolean DEFAULT_IS_TIME_1ST_COL = true;
   private static final char DEFAULT_DELIMITER = ',';
   private static final boolean DEFAULT_IS_LABELED = true;


   // PRIVATE DATA
   private final ArrayList labels;   // labels for each column
   private final ArrayList timeReadings;        // ArrayList of Double
   private final ArrayList tsArray;    // ArrayList of TimeSeriesPoint.. no time

                                                                                     // TODO don't use defaults delimiter/1stColTime... determine if not specified

   // CONSTRUCTORS                                                                   // TODO method to peek at determined delimiter, 1st col time
   TimeSeries()
   {
      labels = new ArrayList();                                                      // TODO isLabeled constuctor options?
      timeReadings = new ArrayList();
      tsArray = new ArrayList();
   }


   public TimeSeries(int numOfDimensions)                                               
   {
      this();
      labels.add("Time");
      for (int x=0; x<numOfDimensions; x++)                
         labels.add(""+x);
   }

   // Copy Constructor
   public TimeSeries(TimeSeries origTS)
   {
      labels = new ArrayList(origTS.labels);
      timeReadings = new ArrayList(origTS.timeReadings);
      tsArray = new ArrayList(origTS.tsArray);
   }


   public TimeSeries(ArrayList metric, boolean isFirstColTime)
   {
      this(metric,  ZERO_ARRAY, isFirstColTime);
   }


   public TimeSeries(ArrayList metric, char delimiter)
   {
      this(metric, ZERO_ARRAY, DEFAULT_IS_TIME_1ST_COL, DEFAULT_IS_LABELED, delimiter);
   }


   public TimeSeries(ArrayList metric, boolean isFirstColTime, char delimiter)
   {
      this(metric, ZERO_ARRAY, isFirstColTime, DEFAULT_IS_LABELED, delimiter);
   }


   public TimeSeries(ArrayList metric, boolean isFirstColTime, boolean isLabeled, char delimiter)
   {
      this(metric, ZERO_ARRAY, isFirstColTime, isLabeled, delimiter);
   }


   public TimeSeries(ArrayList metric, int[] colToInclude, boolean isFirstColTime)
   {
      this(metric, colToInclude, isFirstColTime, DEFAULT_IS_LABELED, DEFAULT_DELIMITER);
   }


   public TimeSeries(ArrayList metric, int[] colToInclude, boolean isFirstColTime, boolean isLabeled, char delimiter)
   {
      this();
         labels.add("Time");
         Integer m_max = metric.size()-1;
         for(int i = 1; i <= m_max; i++) {
             double[] tmp_array = new double[1];
             tmp_array[0] = Double.parseDouble(metric.get(i).toString());
             final TimeSeriesPoint readings = new TimeSeriesPoint(tmp_array);
             tsArray.add(readings);
             timeReadings.add(new Double(timeReadings.size()));
         }
   }  // end constructor



   // FUNCTIONS
   public void save(File outFile) throws IOException
   {
      final PrintWriter out = new PrintWriter(new FileOutputStream(outFile));
      out.write(this.toString());
      out.flush();
      out.close();
   }


   public void clear()
   {
      labels.clear();
      timeReadings.clear();
      tsArray.clear();
 //     timeValueMap.clear();
   }


   public int size()
   {
      return timeReadings.size();
   }


   public int numOfPts()
   {
      return this.size();
   }


   public int numOfDimensions()
   {
      return labels.size()-1;
   }


   public double getTimeAtNthPoint(int n)
   {
      return ((Double)timeReadings.get(n)).doubleValue();
   }


   public String getLabel(int index)
   {
      return (String)labels.get(index);
   }


   public String[] getLabelsArr()
   {
      final String[] labelArr = new String[labels.size()];
      for (int x=0; x<labels.size(); x++)
         labelArr[x] = (String)labels.get(x);
      return labelArr;
   }


   public ArrayList getLabels()
   {
      return labels;
   }


   public void setLabels(String[] newLabels)
   {
      labels.clear();
      for (int x=0; x<newLabels.length; x++)
         labels.add(newLabels[x]);
   }


   public void setLabels(ArrayList newLabels)
   {
      labels.clear();
      for (int x=0; x<newLabels.size(); x++)
         labels.add(newLabels.get(x));
   }


   public double getMeasurement(int pointIndex, int valueIndex)
   {
      return ((TimeSeriesPoint)tsArray.get(pointIndex)).get(valueIndex);
   }


   public double getMeasurement(int pointIndex, String valueLabel)
   {
      final int valueIndex = labels.indexOf(valueLabel);
      if (valueIndex < 0)
         throw new InternalError("ERROR:  the label '" + valueLabel + "' was " +
                                 "not one of:  " + labels);

      return ((TimeSeriesPoint)tsArray.get(pointIndex)).get(valueIndex-1);
   }


   public double[] getMeasurementVector(int pointIndex)
   {
      return ((TimeSeriesPoint)tsArray.get(pointIndex)).toArray();
   }


   public void setMeasurement(int pointIndex, int valueIndex, double newValue)
   {
      ((TimeSeriesPoint)tsArray.get(pointIndex)).set(valueIndex, newValue);
   }


   public void addFirst(double time, TimeSeriesPoint values)
   {
      if (labels.size() != values.size()+1)  // labels include a label for time
         throw new InternalError("ERROR:  The TimeSeriesPoint: " + values +
                                 " contains the wrong number of values. " +
                                 "expected:  " + labels.size() + ", " +
                                 "found: " + values.size());

      if (time >= ((Double)timeReadings.get(0)).doubleValue())
         throw new InternalError("ERROR:  The point being inserted into the " +
                                 "beginning of the time series does not have " +
                                 "the correct time sequence. ");

      timeReadings.add(0, new Double(time));
      tsArray.add(0, values);
   }  // end addFirst(..)


   public void addLast(double time, TimeSeriesPoint values)
   {
      if (labels.size() != values.size()+1)  // labels include a label for time
         throw new InternalError("ERROR:  The TimeSeriesPoint: " + values +
                                 " contains the wrong number of values. " +
                                 "expected:  " + labels.size() + ", " +
                                 "found: " + values.size());

      if ( (this.size()>0) && (time<=((Double)timeReadings.get(timeReadings.size()-1)).doubleValue()) )
         throw new InternalError("ERROR:  The point being inserted at the " +
                                 "end of the time series does not have " +
                                 "the correct time sequence. ");

      timeReadings.add(new Double(time));
      tsArray.add(values);
   }  // end addLast(..)


   public void removeFirst()
   {
      if (this.size() == 0)
         System.err.println("WARNING:  TimeSeriesPoint:removeFirst() called on an empty time series!");
      else
      {
         timeReadings.remove(0);
         tsArray.remove(0);
      }  // end if
   }  // end removeFirst()


   public void removeLast()
   {
      if (this.size() == 0)
         System.err.println("WARNING:  TimeSeriesPoint:removeLast() called on an empty time series!");
      else
      {
         tsArray.remove(timeReadings.size()-1);
         timeReadings.remove( timeReadings.size()-1 );
      }  // end if
   }  // end removeFirst()


   public void normalize()
   {
      // Calculate the mean of each FD.
      final double[] mean = new double[this.numOfDimensions()];
      for (int col=0; col<numOfDimensions(); col++)
      {
         double currentSum = 0.0;
         for (int row=0; row<this.size(); row++)
            currentSum += this.getMeasurement(row, col);

         mean[col] = currentSum / this.size();
      }  // end for loop

      // Calculate the standard deviation of each FD.
      final double[] stdDev = new double[numOfDimensions()];
      for (int col=0; col<numOfDimensions(); col++)
      {
         double variance = 0.0;
         for (int row=0; row<this.size(); row++)
            variance += Math.abs(getMeasurement(row, col)-mean[col]);

         stdDev[col] = variance / this.size();
      }  // end for loop


      // Normalize the values in the data using the mean and standard deviation
      //    for each FD.  =>  Xrc = (Xrc-Mc)/SDc
      for (int row=0; row<this.size(); row++)
      {
         for (int col=0; col<numOfDimensions(); col++)
         {
            // Normalize data point.
            if (stdDev[col] == 0.0)   // prevent divide by zero errors
               setMeasurement(row, col, 0.0);  // stdDev is zero means all pts identical
            else   // typical case
               setMeasurement(row, col, (getMeasurement(row, col)-mean[col]) / stdDev[col]);
         }  // end for loop
      }  // end for loop
   }  // end normalize();



   public String toString()
   {
      final StringBuffer outStr = new StringBuffer();
/*
      // Write labels
      for (int x=0; x<labels.size(); x++)
      {
         outStr.append(labels.get(x));
         if (x < labels.size()-1)
            outStr.append(",");
         else
            outStr.append("\n");
      }  // end for loop
*/
      // Write the data for each row.
      for (int r=0; r<timeReadings.size(); r++)
      {
         // Time
//         outStr.append(timeReadings.get(r).toString());

         // The rest of the value on the row.
         final TimeSeriesPoint values = (TimeSeriesPoint)tsArray.get(r);
         for (int c=0; c<values.size(); c++)
            outStr.append(values.get(c));

         if (r < timeReadings.size()-1)
            outStr.append("\n");
      }  // end for loop

      return outStr.toString();
   }  // end toString()



   // Returns the first non-digit (and not a '.') character in a file under the
   //    assumption that it is the delimiter in the file.
   private static char determineDelimiter(String filePath)
   {
      final char DEFAULT_DELIMITER = ',';

      try
      {
         final BufferedReader in = new BufferedReader(new FileReader(filePath));

         String line = in.readLine().trim();   // read first line

         if (!Character.isDigit(line.charAt(0)))  // go to 2nd line if 1st line appears to be labels
            line = in.readLine();

         in.close();

         // Searches the 2nd line of the file until a non-number character is
         //    found.  The delimiter is assumed to be that character.
         //    numbers, minus signs, periods, and 'E' (exponent) are accepted
         //    number characters.
         for (int x=0; x<line.length(); x++)
         {
            if ( !Character.isDigit(line.charAt(x)) && (line.charAt(x)!='.') && (line.charAt(x)!='-') &&
                 (Character.toUpperCase(line.charAt(x))!='E') )
               return line.charAt(x);
         }

         // No delimiters were found, which means that there must be only one column
         //    A delimiter does not need to be known to read this file.
         return DEFAULT_DELIMITER;
      }
      catch (IOException e)
      {
         return DEFAULT_DELIMITER;
      }
   }  // end determineDelimiter(.)



   private static double extractFirstNumber(String str)
   {
      StringBuffer numStr = new StringBuffer();

      // Keep adding characters onto numStr until a non-number character
      //    is reached.
      for (int x = 0; x<str.length(); x++)
      {
         if ((Character.isDigit(str.charAt(x))) || (str.charAt(x) == '.') || (str.charAt(x) == '-') ||
                  (Character.toUpperCase(str.charAt(x)) == 'E'))
            numStr.append(str.charAt(x));
         else
            Double.parseDouble(numStr.toString());
      }  // end for loop

      return -1;
   }



   // Automatically determines if the first column in a file is time measurements.
   //    It assumes that a column of time will have equal spacing between all
   //    values.
   private static boolean determineIsFirstColTime(String filePath)
   {
      final boolean DEFAULT_VALUE = false;

      try
      {
         final BufferedReader in = new BufferedReader(new FileReader(filePath));

         // This parameter is the percentage of flexibility that is permitted from
         //    a perfectly even distribution of time values.  This function will
         //    most likely not work if this is set to zero because of floating-
         //    point math roundoff errors.
         //    (a setting of '0.05' is '5 percent')
         final double EQUALITY_FLEXIBILITY_PCT = 0.001;

         final int NUM_OF_VALUES_TO_CMP = 100;   // $ of time values to look examine

         final Vector possibleTimeValues = new Vector(NUM_OF_VALUES_TO_CMP);  // 'stores numOfValuesToCompare' values

         // Read the first 'numOfValuesToCompare' possible time values from the file
         //    and store them in 'possibleTimeValues'.
         String line = in.readLine();

         while ((possibleTimeValues.size()<NUM_OF_VALUES_TO_CMP) && ((line=in.readLine())!=null))
            possibleTimeValues.add(new Double(extractFirstNumber(line)));

         if (possibleTimeValues.size()<=1)
            return DEFAULT_VALUE;

         // See if there is equal spacing (with a flexibility of
         //    'equalityFlexibilityFactor') between all values in              // TODO TimeSeries is now messy...in need of design
         //    'possibleTimeValues'.
         if ((possibleTimeValues.size()>1) && possibleTimeValues.get(1).equals(possibleTimeValues.get(0)))
            return DEFAULT_VALUE;   // special case needed for very flat data


         final double expectedDiff = ((Double)possibleTimeValues.get(1)).doubleValue() -
                                     ((Double)possibleTimeValues.get(0)).doubleValue();
         final double flexibility = expectedDiff * EQUALITY_FLEXIBILITY_PCT;
         for (int x=1; x<possibleTimeValues.size(); x++)
         {
            if (Math.abs( ((Double)possibleTimeValues.get(x)).doubleValue() -
                          ((Double)possibleTimeValues.get(x-1)).doubleValue() - expectedDiff)
                > Math.abs(flexibility))
            {
               return false;
            }
         }   // end for loop

         return true;
      }
      catch (IOException e)
      {
         return DEFAULT_VALUE;
      }
   }  // end determineIsFirstColTime(.)


   protected void setMaxCapacity(int capacity)
   {
      this.timeReadings.ensureCapacity(capacity);
      this.tsArray.ensureCapacity(capacity);
   }



}  // end class TimeSeries

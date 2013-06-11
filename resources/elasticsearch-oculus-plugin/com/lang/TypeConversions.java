/*
 * TypeConversions.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.lang;


public class TypeConversions
{


   public static byte[] doubleToByteArray(double number)
   {
      // double to long representation
      long longNum = Double.doubleToLongBits(number);

      // long to 8 bytes
      return new byte[] {(byte)((longNum >>> 56) & 0xFF),
                         (byte)((longNum >>> 48) & 0xFF),
                         (byte)((longNum >>> 40) & 0xFF),
                         (byte)((longNum >>> 32) & 0xFF),
                         (byte)((longNum >>> 24) & 0xFF),
                         (byte)((longNum >>> 16) & 0xFF),
                         (byte)((longNum >>>  8) & 0xFF),
                         (byte)((longNum >>>  0) & 0xFF)};
   }  // end doubleToByte(.)



   public static byte[] doubleArrayToByteArray(double[] numbers)
   {
      final int doubleSize = 8;  // 8 byes in a double
      final byte[] byteArray = new byte[numbers.length*doubleSize];

      for (int x=0; x<numbers.length; x++)
         System.arraycopy(doubleToByteArray(numbers[x]), 0, byteArray, x*doubleSize, doubleSize);

      return byteArray;
   }  // end doubleArrayToByteArray(.)


}  // end class Typeconversions

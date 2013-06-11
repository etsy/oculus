/*
 * Array2D.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;



public class Array2D
{
   // PRIVATE DATA
   final private ArrayList rows;  // ArrayList of ArrayList (an array of rows in the array)
   private int numOfElements;


   // CONSTRUCTOR
   public Array2D()
   {
      rows = new ArrayList();
      numOfElements = 0;
   }


   public Array2D(Array2D array)
   {
      this.rows = new ArrayList(array.rows);
      this.numOfElements = array.numOfElements;
   }



   // PUBLIC FU?NCTIONS
   public void clear()
   {
      rows.clear();
      numOfElements = 0;
   }


   public int size()
   {
      return numOfElements;
   }


   public int numOfRows()
   {
      return rows.size();
   }


   public int getSizeOfRow(int row)
   {
      return ((ArrayList)rows.get(row)).size();
   }


   public Object get(int row, int col)
   {
      return ((ArrayList)rows.get(row)).get(col);
   }


   public void set(int row, int col, Object newVal)
   {
      ((ArrayList)rows.get(row)).set(col, newVal);
   }


   public void addToEndOfRow(int row, Object value)
   {
      ((ArrayList)rows.get(row)).add(value);
      numOfElements++;
   }


   public void addAllToEndOfRow(int row, Collection objects)
   {
      final Iterator i = objects.iterator();
      while (i.hasNext())
      {
         ((ArrayList)rows.get(row)).add(i.next());
         numOfElements++;
      }
   }


   public void addToNewFirstRow(Object value)
   {
      final ArrayList newFirstRow = new ArrayList(1);
      newFirstRow.add(value);
      rows.add(0, newFirstRow);
      numOfElements++;
   }


   public void addToNewLastRow(Object value)
   {
      final ArrayList newLastRow = new ArrayList(1);
      newLastRow.add(value);
      rows.add(newLastRow);
      numOfElements++;
   }


   public void addAllToNewLastRow(Collection objects)
   {
      final Iterator i = objects.iterator();
      final ArrayList newLastRow = new ArrayList(1);
      while (i.hasNext())
      {
         newLastRow.add(i.next());
         numOfElements++;
      }

      rows.add(newLastRow);
   }


   public void removeFirstRow()
   {
      numOfElements -= ((ArrayList)rows.get(0)).size();
      rows.remove(0);
   }


   public void removeLastRow()
   {
      numOfElements -= ((ArrayList)rows.get(rows.size()-1)).size();
      rows.remove(rows.size()-1);
   }


   public String toString()
   {
      String outStr = "";
      for (int r=0; r<rows.size(); r++)
      {
         final ArrayList currentRow = (ArrayList)rows.get(r);
         for (int c=0; c<currentRow.size(); c++)
         {
            outStr += currentRow.get(c);
            if (c == currentRow.size()-1)
               outStr += "\n";
            else
               outStr += ",";
         }
      }  // end for

      return outStr;
   }

}  // end class matrix.Array2D

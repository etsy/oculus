/*
 * ColMajorCell.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.matrix;


public class ColMajorCell
{
   private final int col;
   private final int row;


   public int getCol()
   {
      return col;
   }



   public int getRow()
   {
     return row;
   }


   public ColMajorCell(int column, int row)
   {
      this.col = column;
      this.row = row;
   }  // end Constructor


   public boolean equals(Object o)
   {
      return (o instanceof ColMajorCell) &&
             (((ColMajorCell)o).col == this.col) &&
             (((ColMajorCell)o).row == this.row);
   }


   public int hashCode()
   {
      return (1<<col) + row;
   }


   public String toString()
   {
      return "(" + col + "," + row + ")";
   }

}  // end class ColMajorCell

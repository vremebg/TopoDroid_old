/** @file ParserShot.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid parser shot
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.DistoX;

class ParserShot
{
    String from;
    String to;
    float len, ber, cln, rol;
    int extend;
    int leg;
    boolean duplicate;
    boolean surface;
    boolean backshot;
    String comment;

    public ParserShot( String f, String t, float l, float b, float c, float r, int ex, int lg,
                       boolean d, boolean s, boolean bs, String cmnt )
    {
      from = f;
      to   = t;
      len = l;
      ber = b;
      cln = c;
      rol = r;
      extend = ex;
      leg    = lg;
      duplicate = d;
      surface = s;
      backshot = bs;
      comment = cmnt;
    }
}


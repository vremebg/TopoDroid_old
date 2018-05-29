/** @file SymbolPoint.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: point symbol
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.io.File;
// import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Locale;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Matrix;

// to log error
// import android.util.Log;

class SymbolPoint extends Symbol
{
  static final private float dxfScale = 0.05f;
  static final private float csxScale = 5.00f;
  static final private float csxdxfScale = csxScale * dxfScale;
  Paint  mPaint;
  Path   mPath;
  Path   mOrigPath; // PRIVATE
  String mName;
  String mDxf; // PRIVATE
  String mSvg;

  int mHasText;         // whether the point has a text (1), value (2), or none (0)
  boolean mOrientable; // PRIVATE
  double mOrientation;      // orientation [degrees]
  // SymbolPointBasic mPoint1; // basic point

  // boolean hasText() { return mHasText; }
  // double orientation() { return mOrientation; }

  @Override public boolean isOrientable() { return mOrientable; }

  // @Override public boolean isEnabled() { return mEnabled; }
  // @Override public void setEnabled( boolean enabled ) { mEnabled = enabled; }
  // @Override public void toggleEnabled() { mEnabled = ! mEnabled; }

  @Override
  public boolean setAngle( float angle ) // degrees
  {
    if ( ! mOrientable ) return false;
    float a = angle - (float)mOrientation;
    if ( Math.abs(a) > 1 ) {
      rotateGradP( a );
      return true;
    }
    return false;
  }
  @Override public int getAngle() { return (int)mOrientation; } // degrees

  @Override public String getName( ) { return mName; }
  @Override public String getThName( ) { return mThName; }

  String getDxf( ) { return mDxf; }

  boolean hasThName( String th_name ) { return ( th_name.equals( mThName ) ); } 

  @Override public Path getPath( ) { return mPath; }

  Path getOrigPath( ) { return mOrigPath; }
 
  @Override public Paint getPaint( ) { return mPaint; }

  SymbolPoint( String pathname, String fname, String locale, String iso )
  {
    super( null, fname );
    mOrientable = false;
    mHasText = 0;
    mOrientation = 0.0;
    readFile( pathname, locale, iso );
  }

  SymbolPoint( String n1, String tn1, String fname, int c1, String path, boolean orientable )
  {
    super( tn1, fname );
    mName = n1;
    mDxf    = null;
    makePaint( c1, Paint.Style.STROKE ); // FIXME style
    if ( path != null ) {
      makePath( path );
    } else {
      makePath( );
    }
    mOrigPath = new Path( mPath );
    
    mOrientable = orientable;
    mHasText = 0;
    mOrientation = 0.0;
  }

  SymbolPoint( String n1, String tn1, String fname, int c1, String path, boolean orientable, int has_text )
  {
    super( tn1, fname ); // FIXME fname
    mName = n1;
    mDxf    = null;
    makePaint( c1, Paint.Style.STROKE ); //FIXME style
    if ( path != null ) {
      makePath( path );
    } else {
      makePath( );
    }
    mOrigPath = new Path( mPath );

    mOrientable = orientable;
    mHasText = has_text;
    mOrientation = 0.0;
  }

  void rotateGradP( double a )
  {
    if ( mOrientable ) {
      mOrientation += a;
      if ( mOrientation > 360.0 ) mOrientation -= 360.0;
      if ( mOrientation < 0.0 )   mOrientation += 360.0;
      Matrix m = new Matrix();
      m.postRotate( (float)(a) );
      mPath.transform( m );
    }
  }

  void resetOrientation()
  {
    if ( mOrientable && mOrientation != 0.0 ) {
      Matrix m = new Matrix();
      m.postRotate( (float)(-mOrientation) );
      mPath.transform( m );
      mOrientation = 0.0;
    }
  }

  /** create a symbol reading it from a file
   *  The file syntax is 
   *      symbol point
   *      name NAME
   *      th_name THERION_NAME
   *      has_text yes | NO
   *      has_value yes | NO
   *      orientation yes | NO
   *      color 0xHHHHHH_COLOR
   *      style fill | STROKE 
   *      path
   *        MULTILINE_PATH_STRING
   *      endpath
   *      endsymbol
   */
  private void readFile( String pathname, String locale, String iso )
  {
    // Log.v(  TopoDroidApp.TAG, "SymbolPoint::readFile " + pathname + " locale " + locale );
 
    String name    = null;
    String th_name = null;
    int color      = 0;
    Paint.Style style = Paint.Style.STROKE;
    String path    = null;
    int cnt = 0;

    try {
      // FileReader fr = new FileReader( pathname );
      FileInputStream fr = new FileInputStream( pathname );
      BufferedReader br = new BufferedReader( new InputStreamReader( fr, iso ) );
      String line;
      line = br.readLine();
      while ( line != null ) {
        line = line.trim();
        String[] vals = line.split(" ");
        int s = vals.length;
        for (int k=0; k<s; ++k ) {
          if ( vals[k].startsWith( "#" ) ) break;
          if ( vals[k].equals("symbol") ) {
            name = null;
            th_name = null;
            color = 0x00000000;
            path = null;
          } else if ( vals[k].equals("name") || vals[k].equals(locale) ) {
            ++k; while ( k < s && vals[k].length() == 0 ) ++k;
            if ( k < s ) {
              // name = (new String( vals[k].getBytes(iso) )).replace("_", " ");
              name = vals[k].replace("_", " ");
              // Log.v(  TopoDroidApp.TAG, "set name " + name );
            }
          } else if ( vals[k].equals("th_name") ) {
            ++k; while ( k < s && vals[k].length() == 0 ) ++k;
            if ( k < s ) {
              th_name = vals[k];
            }
          } else if ( vals[k].equals("orientation") ) {
            if ( cnt == 0 ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mOrientable = ( vals[k].equals("yes") || vals[k].equals("1") );
              }
            }
          } else if ( vals[k].equals("has_text") ) {
            if ( cnt == 0 ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mHasText = ( vals[k].equals("yes") || vals[k].equals("1") )? 1 : 0;
                if ( vals[k].equals("2") ) mHasText = 2;
              }
            }
          } else if ( vals[k].equals("has_value") ) {
            if ( cnt == 0 ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mHasText = ( vals[k].equals("yes") || vals[k].equals("1") )? 2 : 0;
                if ( vals[k].equals("1") ) mHasText = 1;
              }
            }
          } else if ( vals[k].equals("style") ) {
            if ( cnt == 0 ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                if ( vals[k].equals("fill") ) {
                //  style = Paint.Style.FILL;
                // } else if ( vals[k].equals("fill-stroke") ) {
                  style = Paint.Style.FILL_AND_STROKE;
                }
              }
            }
          } else if ( vals[k].equals("color") ) {
            ++k; while ( k < s && vals[k].length() == 0 ) ++k;
            if ( k < s ) {
              color = Integer.decode( vals[k] );
              color |= 0xff000000;
            }
          } else if ( vals[k].equals("csurvey") ) {
            try {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mCsxLayer = Integer.parseInt( vals[k] );
              }
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mCsxType = Integer.parseInt( vals[k] );
              }
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mCsxCategory = Integer.parseInt( vals[k] );
              }
            } catch ( NumberFormatException e ) {
              TDLog.Error( pathname + " parse csurvey error: " + line );
            }
          } else if ( vals[k].equals("path") ) {
            path = br.readLine();
            if ( path != null ) {
              while ( ( line = br.readLine() ) != null ) {
                if ( line.startsWith( "endpath" ) ) break;
                path = path + " " + line;
              }
            }
          } else if ( vals[k].equals("endsymbol") ) {
            if ( name == null ) {
              TDLog.Error("NULL name " + pathname );
            } else if ( th_name == null ) {
              TDLog.Error("NULL th_name " + pathname );
            } else if ( path == null ) {
              TDLog.Error("NULL path " + pathname);
            } else {
              if ( cnt == 0 ) {
                mName = name;
                mThName = th_name;
                makePaint( color, style );
                makePath( path );
                mOrigPath = new Path( mPath );
                // mPoint1 = new SymbolPointBasic( name, th_name, fname, color, path );
              // } else if ( cnt == 1 ) {
              //   if ( mOrientable == true ) {
              //     // ERROR point1 is orientable
              //   } else {
              //     mPoint2 = new SymbolPointBasic( name, th_name, fname, color, path );
              //     mOrientable = true;
              //   }
              // } else {
              //   // ERROR only two points max
              }
              ++ cnt;
            }
          }
        }
        line = br.readLine();
      }
    } catch ( FileNotFoundException e ) {
      // FIXME
    } catch( IOException e ) {
      // FIXME
    }
    mOrientation = 0.0;
    // Log.v(  TopoDroidApp.TAG, "SymbolPoint::readFile " + pathname + " csurvey " + mCsxLayer );
  }

  private void makePath()
  {
    mPath = new Path();
    mPath.moveTo(0,0);
    mDxf  = "  0\nLINE\n  8\nPOINT\n" 
          + "  100\nAcDbEntity\n  100\nAcDbLine\n"
          + "  10\n0.0\n  20\n0.0\n  30\n0.0\n"
          + "  11\n1.0\n  21\n0.0\n  31\n0.0\n"; // 1 mm long
    mCsx = "";
    mSvg = "";
  }

  /* Make the path from its stringn description
   * The path string description is composed of the following directives
   *     - "moveTo X Y"
   *     - "lineTo X Y"
   *     - "cubicTo X1 Y1 X2 Y2 X Y"
   *     - "addCircle X Y R"
   *     - "arcTo X1 Y1 X2 Y2 A0 DA"
   */
  private void makePath( String path )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    StringWriter sv1 = new StringWriter();
    PrintWriter pv1  = new PrintWriter( sv1 ); // CSX path writer
    StringWriter sv2 = new StringWriter();
    PrintWriter pv2  = new PrintWriter( sv2 ); // CSX circle writer
    StringWriter sv3 = new StringWriter();
    PrintWriter pv3  = new PrintWriter( sv3 ); // SVG circle writer

    float x00=0, y00=0;  // last drawn point1

    float unit = TDSetting.mUnit;
    mPath = new Path();
    String[] vals = path.split(" ");
    int s = vals.length;
    for ( int k = 0; k<s; ++k ) {
      float x0=0, y0=0, x1=0, y1=0, x2=0, y2=0;
      if ( "moveTo".equals( vals[k] ) ) {
        try {
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) {
            y0 = Float.parseFloat( vals[k] );
            mPath.moveTo( x0*unit, y0*unit );
            x00 = x0 * dxfScale;
            y00 = y0 * dxfScale;

            pv1.format(Locale.US, "M %.2f %.2f ", x00*csxScale, y00*csxScale );
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( path + " parse moveTo error" );
        }
      } else if ( "lineTo".equals( vals[k] ) ) {      
        try {
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { 
            y0 = Float.parseFloat( vals[k] ); 
            mPath.lineTo( x0*unit, y0*unit );
            DrawingDxf.printString( pw, 0, "LINE" );
            DrawingDxf.printString( pw, 8, "POINT" );
            DrawingDxf.printAcDb( pw, -1, "AcDbEntity", "AcDbLine" );
            DrawingDxf.printXYZ( pw, x00, -y00, 0.0f, 0 ); // prev point
            x00 = x0 * dxfScale;
            y00 = y0 * dxfScale;
            DrawingDxf.printXYZ( pw, x00, -y00, 0.0f, 1 ); // current point
            
            pv1.format(Locale.US, "L %.2f %.2f ", x00*csxScale, y00*csxScale );
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( path + " parse lineTo error" );
        }
      } else if ( "cubicTo".equals( vals[k] ) ) {
        // cp1x cp1y cp2x cp2y p2x p2y
        try {
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;  // CP1
          if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { y0 = Float.parseFloat( vals[k] ); }

          ++k; while ( k < s && vals[k].length() == 0 ) ++k;  // CP2
          if ( k < s ) { x1 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { y1 = Float.parseFloat( vals[k] ); }

          ++k; while ( k < s && vals[k].length() == 0 ) ++k;  // P2
          if ( k < s ) { x2 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { 
            y2 = Float.parseFloat( vals[k] ); 
            mPath.cubicTo( x0*unit, y0*unit, x1*unit, y1*unit, x2*unit, y2*unit );

            x00 /= dxfScale;
            y00 /= dxfScale;

            float dx = TDMath.abs( x00 - x2 );
            float dy = TDMath.abs( y00 - y2 );

            float a1 = 0.0f;
            float a2 = 0.0f;
            // float zz = 1.0f;
            float cx = 0.0f;
            float cy = 0.0f;
            float r = TDMath.abs( x00-x2 );
            float e = TDMath.abs( r /(y00-y2) );

            if ( x00 > x2 ) {
              if ( y00 > y2 ) {
                if ( Math.abs(x1-x00) > Math.abs(y1-y00) ) { // clockwise
                  cx = x00;
                  cy = y2;
                  a1 = TDMath.M_PI;
                  a2 = 3 * TDMath.M_PI2;
                } else { // counter-clockwise
                  cx = x2;
                  cy = y00;
                  a1 = 0.0f;
                  a2 = TDMath.M_PI2;
                }
              } else if ( y00 < y2 ) {
                if ( Math.abs(x1-x00) > Math.abs(y1-y00) ) { // counter-clockwise
                  cx = x00;
                  cy = y2;
                  a1 = TDMath.M_PI2;
                  a2 = TDMath.M_PI;
                } else {
                  cx = x2;
                  cy = y00;
                  a1 = 3 * TDMath.M_PI2;
                  a2 = 2 * TDMath.M_PI;
                }
              } else { // y00 == y2 : semicircle
                cx = ( x00 + x2 ) /2;
                cy = y00;
                r /= 2;
                e = 1.0f;
                if ( y1 > y00 ) { // down
                  a1 = TDMath.M_PI;
                } else {
                  a1 = 0.0f;
                }
                a2 = a1 + TDMath.M_PI;
              }
            } else if ( x00 < x2 ) {
              if ( y00 > y2 ) {
                if ( Math.abs(x1-x00) > Math.abs(y1-y00) ) { // counter-clockwise
                  cx = x00;
                  cy = y2;
                  a1 = 3 * TDMath.M_PI2;
                  a2 = 2 * TDMath.M_PI;
                } else {
                  cx = x2;
                  cy = y00;                   
                  a1 = TDMath.M_PI2;
                  a2 = TDMath.M_PI;
                }
              } else if ( y00 < y2 ) {
                if ( Math.abs(x1-x00) > Math.abs(y1-y00) ) { // counter-clockwise
                  cx = x00;
                  cy = y2;
                  a1 = 0.0f;
                  a2 = TDMath.M_PI2;
                } else {
                  cx = x2;
                  cy = y00;
                  a1 = TDMath.M_PI;
                  a2 = 3 * TDMath.M_PI2;
                }
              } else { // y00 == y2 : semicircle
                cx = ( x00 + x2 ) / 2;
                cy = y00;
                r /= 2;
                e = 1.0f;
                if ( y1 > y00 ) { // down
                  a1 = TDMath.M_PI;
                } else {
                  a1 = 0.0f;
                }
                a2 = a1 + TDMath.M_PI;
              }
            } else { // x00 == x2 : semicircle
              cx = x00;
              cy = ( y00 + y2 ) / 2;
              r = TDMath.abs( y00-y2 ) / 2;
              e = 1.0f;
              if ( y00 > y2 ) {
                if ( x1 < x00 ) { // left
                  a1 = TDMath.M_PI2;
                } else {
                  a1 = 3 * TDMath.M_PI2;
                }
              } else {
                if ( x1 < x00 ) {
                  a1 = 3 * TDMath.M_PI2;
                } else {
                  a1 = TDMath.M_PI2;
                }
              }
              a2 = a1 + TDMath.M_PI;
            }

            // Log.v(TopoDroidApp.TAG, mName + " cubic " + x00 + " " + y00 + " " + x0 + " " + y0 + " " + x1 + " " + y1 + " " + x2 + " " + y2 );
            // Log.v(TopoDroidApp.TAG, mName + " " + cx + " " + cy + " R " + r + " E " + e + " angles " + a1 + " " + a2 );

            // DrawingDxf.printString( pw, 0, "ELLIPSE" );
            // DrawingDxf.printString( pw, 8, "POINT" );
            // DrawingDxf.printAcDb( pw, -1, "AcDbEntity", "AcDbEllipse" );
            // pw.printf(Locale.US,
            //           "  10\n%.2f\n  20\n%.2f\n  30\n%.2f\n  11\n%.2f\n  21\n%.2f\n  31\n%.2f\n  40\n%.2f\n  41\n%.2f\n  42\n%.2f\n",
            //           cx*dxfScale, -cy*dxfScale, 0.0f,        // CENTER
            //           r*dxfScale, 0.0, 0.0f,                  // ENDPOINT OF MAJOR AXIS - CENTER
            //           e,                                      // RATIO MINOR/MAJOR
            //           a1, a2 );                               // START and END ANGLES [rad]
            DrawingDxf.printString( pw, 0, "ARC" );
            DrawingDxf.printString( pw, 8, "POINT" );
            DrawingDxf.printAcDb( pw, -1, "AcDbEntity", "AcDbCircle" );
            DrawingDxf.printXYZ( pw, cx*dxfScale, -cy*dxfScale, 0.0f, 0 );
            DrawingDxf.printFloat( pw, 40, r*dxfScale );
            DrawingDxf.printString( pw, 100, "AcDbArc" );
            DrawingDxf.printFloat( pw, 50, a1 * TDMath.RAD2DEG );
            DrawingDxf.printFloat( pw, 51, a2 * TDMath.RAD2DEG );
    
            x00 = x2 * dxfScale;
            y00 = y2 * dxfScale;

            pv1.format(Locale.US, "C %.2f %.2f %.2f %.2f %.2f %.2f ",
               x0*csxdxfScale, y0*csxdxfScale, x1*csxdxfScale, y1*csxdxfScale, x2*csxdxfScale, y2*csxdxfScale );
          
            // FIXME
            // DrawingDxf.printString( pw, 0, "LINE" );
            // DrawingDxf.printString( pw, 8, "POINT" );
            // DrawingDxf.printAcDb( pw, -1, "AcDbEntity", "AcDbLine" );
            // DrawingDxf.printXYZ( pw, x00, -y00, 0.0f, 0 );
            // x00 = x2 * dxfScale;
            // y00 = y2 * dxfScale;
            // DrawingDxf.printXYZ( pw, x00, -y00, 0.0f, 1 );
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( path + " parse cubicTo error" );
        }
      } else if ( "addCircle".equals( vals[k] ) ) {
        try {
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }  // center X coord
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { y0 = Float.parseFloat( vals[k] ); }  // center Y coord
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) {
            x1 = Float.parseFloat( vals[k] );                 // radius
            mPath.addCircle( x0*unit, y0*unit, x1*unit, Path.Direction.CCW );
            DrawingDxf.printString( pw, 0, "CIRCLE" );
            DrawingDxf.printString( pw, 8, "POINT" );
            DrawingDxf.printAcDb( pw, -1, "AcDbEntity", "AcDbCircle" );
            DrawingDxf.printXYZ( pw, x0*dxfScale, -y0*dxfScale, 0.0f, 0 );
            DrawingDxf.printFloat( pw, 40, x1*dxfScale );

            pv2.format(Locale.US,
              "&lt;circle cx=&quot;%.2f&quot; cy=&quot;%.2f&quot; r=&quot;%.2f&quot; /&gt;",
              x0*csxdxfScale, y0*csxdxfScale, x1*csxdxfScale );
            pv3.format(Locale.US,
              "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%.2f\" />",
              x0*csxdxfScale, y0*csxdxfScale, x1*csxdxfScale );
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( path + " parse circle error" );
        }
      } else if ( "arcTo".equals( vals[k] ) ) {
        // (x0,y0) top-left corner of rect
        // (x1,y1) bottom-right corner of rect
        // x2 start-angle [degrees]
        // y2 sweep angle (clockwise) [degrees]
        // 
        //    (x0,y0) +-----=-----+
        //            |     |     |
        //            |=====+=====| 0 angle (?)
        //            |     |     | | sweep direction
        //            +-----=-----+ V
        //
        try {
          ++k; while ( k < s && vals[k].length() == 0 ) ++k; // RECTANGLE first endpoint
          if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { y0 = Float.parseFloat( vals[k] ); }

          ++k; while ( k < s && vals[k].length() == 0 ) ++k; // RECTANGLE second endpoint
          if ( k < s ) { x1 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { y1 = Float.parseFloat( vals[k] ); }

          ++k; while ( k < s && vals[k].length() == 0 ) ++k;  // FROM, TT angles [deg]
          if ( k < s ) { x2 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { 
            y2 = Float.parseFloat( vals[k] ); 
            mPath.arcTo( new RectF(x0*unit, y0*unit, x1*unit, y1*unit), x2, y2 );

            // DrawingDxf.printString( pw, 0, "ELLIPSE" );
            // DrawingDxf.printString( pw, 8, "POINT" );
            // DrawingDxf.printAcDb(pw, -1, "AcDbEntity", AcDbEllipse" );
            // pw.printf(Locale.US,
            //           "  10\n%.2f\n  20\n%.2f\n  30\n%.2f\n  11\n%.2f\n  21\n%.2f\n  31\n%.2f\n  40\n%.2f\n  41\n%.2f\n  42\n%.2f\n",
            //           (x0+x1)/2*dxfScale, -(y0+y1)/2*dxfScale, 0.0f,                 // CENTER
            //           x1*dxfScale, -(y0+y1)/2*dxfScale, 0.0f,                        // ENDPOINT OF MAJOR AXIS
            //           (y1-y0)/(x1-x0),                                              // RATIO MINOR/MAJOR
            //           x2*TDMath.DEG2RAD, (x2+y2)*TDMath.DEG2RAD );  // START and END PARAMS
            DrawingDxf.printString( pw, 0, "ARC" );
            DrawingDxf.printString( pw, 8, "POINT" );
            DrawingDxf.printAcDb(pw, -1, "AcDbEntity", "AcDbCircle" );
            DrawingDxf.printXYZ( pw, (x0+x1)/2*dxfScale, -(y0+y1)/2*dxfScale, 0.0f, 0 ); // CENTER
            DrawingDxf.printFloat( pw, 40, x1*dxfScale );                                // RADIUS
            DrawingDxf.printString( pw, 100, "AcDbArc" );
            DrawingDxf.printFloat( pw, 50, x2 );                                         // ANGLES
            DrawingDxf.printFloat( pw, 51, x2+y2 );

            float cx = (x1+x0)/2;
            float cy = (y1+y0)/2;
            float rx = (x1-x0)/2;
            float ry = (y1-y0)/2;
    
            float x0i = (cx + rx * TDMath.cosd( x2 ) )* dxfScale; // initial point
            float y0i = (cy + ry * TDMath.sind( x2 ) )* dxfScale;
            x00 = (cx + rx * TDMath.cosd( x2+y2 ) )* dxfScale;    // final point
            y00 = (cy + ry * TDMath.sind( x2+y2 ) )* dxfScale;
            
            // mode to (x00, y00)
            pv1.format(Locale.US, "M %.2f %.2f ", x0i*csxScale, y0i*csxScale );
            pv1.format(Locale.US, "A %.2f %.2f 0 1 %.2f %.2f ", rx*csxdxfScale, ry*csxdxfScale, x00*csxScale, y00*csxScale );
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( path + " parse arcTo error" );
        }
      }
    }
    mDxf = sw.getBuffer().toString();
    mCsx = "&lt;path d=&quot;" + sv1.getBuffer().toString() + "&quot; /&gt;" + sv2.getBuffer().toString();
    mSvg = "<path d=\"" + sv1.getBuffer().toString() + "\"/> " + sv3.getBuffer().toString();
  }

  private void makePaint( int color, Paint.Style style )
  {
    mPaint = new Paint();
    mPaint.setDither(true);
    mPaint.setColor( color );
    mPaint.setStyle( style );
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth( 1 );
  }
}

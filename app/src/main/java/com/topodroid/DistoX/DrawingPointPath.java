/* @file DrawingPointPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: points
 *        type DRAWING_PATH_POINT
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */

package com.topodroid.DistoX;

import android.graphics.Canvas;
// import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

// import android.util.Log;

/**
 */
class DrawingPointPath extends DrawingPath
{
  private static float toTherion = TDConst.TO_THERION;

  static final int SCALE_NONE = -3; // used to force scaling
  static final int SCALE_XS = -2;
  static final int SCALE_S  = -1;
  static final int SCALE_M  = 0;
  static final int SCALE_L  = 1;
  static final int SCALE_XL = 2;
  static private final String SCALE_STR[] = { "xs", "s", "m", "l", "xl" };
  static private final String SCALE_STR_UC[] = { "XS", "S", "M", "L", "XL" };
  static private String scaleToString( int scale ) 
  { return ( scale >= SCALE_XS && scale <= SCALE_XL )? SCALE_STR[ scale+2 ] : "-"; }
  static String scaleToStringUC( int scale ) 
  { return ( scale >= SCALE_XS && scale <= SCALE_XL )? SCALE_STR_UC[ scale+2 ] : "-"; }

  // float mXpos;             // scene coords
  // float mYpos;
  int mPointType;
  protected int mScale;       //! symbol scale
  double mOrientation;
  String mPointText;

  // FIXME SECTION_RENAME
  DrawingPointPath fixScrap( String survey_name )
  {
    if ( survey_name != null && mPointType == BrushManager.mPointLib.mPointSectionIndex ) {
      String scrap = mOptions.replace("-scrap ", "");
      if ( ! scrap.startsWith(survey_name) ) {
        int pos = scrap.lastIndexOf('-');
        scrap = survey_name + "-" + scrap.substring(pos+1);
      }
      mOptions = "-scrap " + scrap;
    }
    return this;
  }


  // String getTextFromOptions( String options )
  // {
  //   if ( options != null ) {
  //     int len = options.length();
  //     int pos = options.indexOf("-text");
  //     if ( pos > 0 ) {
  //       int start = pos + 5;
  //       while ( start < len && options.charAt( start ) == ' ' ) ++ start;
  //       if ( start < len ) {
  //         int end = start + 1;
  //         while ( end < len && options.charAt( end ) != ' ' ) ++ end;
  //         if ( end < len ) {
  //           mOptions = options.substring(0, start) + options.substring(end);
  //         } else {
  //           mOptions = options.substring(0, start);
  //         }
  //         if ( options.charAt( start ) == '"' ) start ++;
  //         if ( options.charAt( end ) == '"' ) end --;
  //         return options.substring( start, end );
  //       }
  //     }
  //   }
  //   return null;
  // }

  DrawingPointPath( int type, float x, float y, int scale, String text, String options )
  {
    super( DrawingPath.DRAWING_PATH_POINT, null );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + type + " X " + x + " Y " + y );
    mPointType = type;
    setCenter( x, y );
    mScale   = SCALE_NONE;
    mOrientation = 0.0;
    mOptions = options;
    mPointText = text; // getTextFromOptions( options ); // this can also reset mOptions

    if ( BrushManager.mPointLib.isSymbolOrientable( type ) ) {
      mOrientation = BrushManager.getPointOrientation(type);
    }
    setPaint( BrushManager.mPointLib.getSymbolPaint( mPointType ) );
    mScale = scale;
    resetPath( 1.0f );
    // Log.v( TopoDroidApp.TAG, "Point cstr " + type + " orientation " + mOrientation );
  }

  static DrawingPointPath loadDataStream( int version, DataInputStream dis, float x, float y, SymbolsPalette missingSymbols ) 
  {
    float ccx, ccy, orientation;
    int   type;
    int   scale;
    String fname;
    String options = null;
    String text = null;
    try {
      ccx = x + dis.readFloat();
      ccy = y + dis.readFloat();
      fname = dis.readUTF( );
      orientation = dis.readFloat();
      scale   = dis.readInt();
      if ( version >= 303066 ) text = dis.readUTF();
      options = dis.readUTF();

      BrushManager.mPointLib.tryLoadMissingPoint( fname );
      type = BrushManager.mPointLib.getSymbolIndexByFilename( fname );
      // TDLog.Log( TDLog.LOG_PLOT, "P " + fname + " " + type + " " + ccx + " " + ccy + " " + orientation + " " + scale + " options (" + options + ")" );
      if ( type < 0 ) {
        if ( missingSymbols != null ) missingSymbols.addPointFilename( fname ); 
        type = 0;
      }
      // FIXME SECTION_RENAME
      // if ( type == BrushManager.mPointLib.mPointSectionIndex ) {
      //   String scrap = options.replace("-scrap ", "");
      //   scrap = scrap.replace( mApp.mSurvey + "-", "" ); // remove survey name from options
      //   option = "-scrap " + scrap;
      // }
      DrawingPointPath ret = new DrawingPointPath( type, ccx, ccy, scale, text, options );
      ret.setOrientation( orientation );
      return ret;

      // // TODO parse option for "-text"
      // setPaint( BrushManager.mPointLib.getSymbolPaint( mPointType ) );
      // if ( BrushManager.mPointLib.isSymbolOrientable( mPointType ) ) {
      //   BrushManager.rotateGradPoint( mPointType, mOrientation );
      //   resetPath( 1.0f );
      //   BrushManager.rotateGradPoint( mPointType, -mOrientation );
      // }
    } catch ( IOException e ) {
      TDLog.Error( "POINT in error " + e.getMessage() );
      // Log.v("DistoX", "POINT in error " + e.getMessage() );
    }
    return null;
  }

  void setCenter( float x, float y )
  {
    cx = x;
    cy = y;
    left   = x; 
    right  = x+1;
    top    = y;
    bottom = y+1;
  }

  @Override
  boolean rotateBy( float dy )
  {
    if ( ! BrushManager.isPointOrientable( mPointType ) ) return false;
    setOrientation ( mOrientation + dy );
    return true;
  }


  @Override
  void shiftBy( float dx, float dy )
  {
    cx += dx;
    cy += dy;
    mPath.offset( dx, dy );
    left   += dx;
    right  += dx;
    top    += dy;
    bottom += dy;
  }

  @Override
  void scaleBy( float z, Matrix m )
  {
    cx *= z;
    cy *= z;
    mPath.transform( m );
    left   *= z;
    right  *= z;
    top    *= z;
    bottom *= z;
  }

  @Override
  public void shiftPathBy( float dx, float dy ) 
  {
    // x1 += dx;
    // y1 += dy;
    // x2 += dx;
    // y2 += dy;
    // cx += dx;
    // cy += dy;
    // mPath.offset( dx, dy );
    // left   += dx;
    // right  += dx;
    // top    += dy;
    // bottom += dy;
  }

  // FIXME SCALE
  @Override
  public void scalePathBy( float z, Matrix m )
  {
    // x1 *= z;
    // y1 *= z;
    // x2 *= z;
    // y2 *= z;
    // cx *= z;
    // cy *= z;
    // mPath.transform( m );
    // left   *= z;
    // right  *= z;
    // top    *= z;
    // bottom *= z;
  }


  // N.B. canvas is guaranteed ! null
  @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      if ( TDSetting.mUnscaledPoints ) {
        resetPath( 4 * scale );
      }
      mTransformedPath = new Path( mPath );
      if ( mLandscape && ! BrushManager.isPointOrientable( mPointType ) ) {
	Matrix rot = new Matrix();
	rot.postRotate( 90, cx, cy );
	mTransformedPath.transform( rot );
      }
      mTransformedPath.transform( matrix );
      drawPath( mTransformedPath, canvas );
    }
  }

  void setScale( int scale )
  {
    if ( scale != mScale ) {
      mScale = scale;
      resetPath( 1.0f );
    }
  }

  int getScale() { return mScale; }
      

  private void resetPath( float f )
  {
    // Log.v("DistoX", "Reset path " + mOrientation + " scale " + mScale );
    Matrix m = new Matrix();
    if ( mPointType != BrushManager.mPointLib.mPointLabelIndex ) {
      if ( BrushManager.mPointLib.isSymbolOrientable( mPointType ) ) {
        m.postRotate( (float)mOrientation );
      }
      switch ( mScale ) {
        case SCALE_XS: f = 0.50f; break;
        case SCALE_S:  f = 0.72f; break;
        case SCALE_L:  f = 1.41f; break;
        case SCALE_XL: f = 2.00f; break;
      }
      m.postScale(f,f);
      makePath( BrushManager.getPointOrigPath( mPointType ), m, cx, cy );
    }
  }

  // public void setPos( float x, float y ) 
  // {
  //   setCenter( x, y );
  // }

  // public void setPointType( int t ) { mPointType = t; }
  public int pointType() { return mPointType; }

  // public double xpos() { return cx; }
  // public double ypos() { return cy; }

  // public double orientation() { return mOrientation; }

  @Override
  public void setOrientation( double angle ) 
  { 
    // TDLog.Log( TDLog.LOG_PATH, "Point " + mPointType + " set Orientation " + angle );
    // Log.v( "DistoX", "Point::set Orientation " + angle );
    mOrientation = angle; 
    while ( mOrientation >= 360.0 ) mOrientation -= 360.0;
    while ( mOrientation < 0.0 ) mOrientation += 360.0;
    resetPath( 1.0f );
  }

  String getPointText() { return mPointText; }

  void setPointText( String text )
  {
    mPointText = text;
  }

  public void shiftTo( float x, float y ) // x,y scene coords
  {
    mPath.offset( x-cx, y-cy );
    setCenter( x, y );
  }

  // @Override
  public void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind, DrawingUtil mDrawingUtil )
  { 
    int size = mScale - SCALE_XS;
    int layer  = BrushManager.getPointCsxLayer( mPointType );
    int type   = BrushManager.getPointCsxType( mPointType );
    int cat    = BrushManager.getPointCsxCategory( mPointType );
    String csx = BrushManager.getPointCsx( mPointType );
    pw.format("<item layer=\"%d\" cave=\"%s\" branch=\"%s\" type=\"%d\" category=\"%d\" transparency=\"0.00\" data=\"",
      layer, cave, branch, type, cat );
    pw.format("&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;!DOCTYPE svg PUBLIC &quot;-//W3C//DTD SVG 1.1//EN&quot; &quot;http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd&quot;[]&gt;&lt;svg xmlns=&quot;http://www.w3.org/2000/svg&quot; xml:space=&quot;preserve&quot; style=&quot;shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd&quot; xmlns:xlink=&quot;http://www.w3.org/1999/xlink&quot;&gt;&lt;defs&gt;&lt;style type=&quot;text/css&quot;&gt;&lt;![CDATA[ .str0 {stroke:#1F1A17;stroke-width:0.2} .fil0 {fill:none} ]]&gt;&lt;/style&gt;&lt;/defs&gt;&lt;g id=&quot;Livello_%d&quot;&gt;", layer );
    pw.format("%s", csx );
    pw.format("&lt;/g&gt;&lt;/svg&gt;\" ");
    if ( bind != null ) pw.format(" bind=\"%s\" ", bind );
    pw.format(Locale.US, "dataformat=\"0\" signsize=\"%d\" angle=\"%.2f\" >\n", size, mOrientation );
    pw.format("  <pen type=\"10\" />\n");
    pw.format("  <brush type=\"7\" />\n");
    float x = mDrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
    float y = mDrawingUtil.sceneToWorldY( cx, cy );
    pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
    pw.format("  <datarow>\n");
    pw.format("  </datarow>\n");
    pw.format("</item>\n");

    // Log.v( TopoDroidApp.TAG, "toCSurevy() Point " + mPointType + " (" + x + " " + y + ") orientation " + mOrientation );
  }

  @Override
  public String toTherion( )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);

    String th_name = BrushManager.mPointLib.getSymbolThName(mPointType);
    pw.format(Locale.US, "point %.2f %.2f %s", cx*toTherion, -cy*toTherion, th_name );
    toTherionOrientation( pw );
    // FIXME SECTION_RENAME
    // if ( mPointType != BrushManager.mPointLib.mPointSectionIndex )
    toTherionTextOrValue( pw );
    toTherionOptions( pw );
    pw.format("\n");

    return sw.getBuffer().toString();
  }

  void toTherionOrientation( PrintWriter pw )
  {
    if ( mOrientation != 0.0 ) {
      pw.format(Locale.US, " -orientation %.2f", mOrientation);
    }
  }

  private void toTherionTextOrValue( PrintWriter pw )
  {
    if ( mPointText != null && mPointText.length() > 0 ) {
      if ( BrushManager.mPointLib.pointHasText(mPointType) ) { // label, remark
        pw.format(" -text \"%s\"", mPointText );
      } else if ( BrushManager.mPointLib.pointHasValue(mPointType) ) { // passage-height
        pw.format(" -value %s", mPointText );
      }
    }
  }

  void toTherionOptions( PrintWriter pw )
  {
    if ( mScale != SCALE_M ) {
      pw.format( " -scale %s", scaleToString( mScale ) );
      // switch ( mScale ) {
      //   case SCALE_XS: pw.format( " -scale xs" ); break;
      //   case SCALE_S:  pw.format( " -scale s" ); break;
      //   case SCALE_L:  pw.format( " -scale l" ); break;
      //   case SCALE_XL: pw.format( " -scale xl" ); break;
      // }
    }
    // FIXME SECTION_RENAME
    // if ( type == BrushManager.mPointLib.mPointSectionIndex ) {
    //   String scrap = mOptions.replace("-scrap ", "" );
    //   pw.format(" -scrap %s-%s", mApp.mSurvey, scrap );
    // } else {
      if ( mOptions != null && mOptions.length() > 0 ) {
        pw.format(" %s", mOptions );
      }
    // }
  }

  @Override
  void toDataStream( DataOutputStream dos )
  {
    String name = BrushManager.mPointLib.getSymbolThName(mPointType);
    try {
      dos.write( 'P' );
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      dos.writeUTF( name );
      dos.writeFloat( (float)mOrientation );
      dos.writeInt( mScale );
      dos.writeUTF( (mPointText != null)? mPointText : "" );
      dos.writeUTF( (mOptions != null)? mOptions : "" );
      // TDLog.Log( TDLog.LOG_PLOT, "P " + name + " " + cx + " " + cy );
    } catch ( IOException e ) {
      TDLog.Error( "POINT out error " + e.toString() );
    }
  }

}


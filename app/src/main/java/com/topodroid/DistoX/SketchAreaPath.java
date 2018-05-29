/* @file SketchAreaPath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: area-path (areas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.graphics.Canvas;
// import android.graphics.Paint;
// import android.graphics.Path;
// import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedOutputStream;
import java.io.IOException;

// import java.util.Iterator;
// import java.util.List;
// import java.util.ArrayList;

// import android.util.Log;

/**
 */
class SketchAreaPath extends SketchPath
{
  private static int area_id_cnt = 0;
  // private static final String TAG = "DistoX";

  private static String makeId() 
  {
    ++ area_id_cnt;
    return "a" + area_id_cnt;
  }

  private int mAreaCnt;
  private boolean mVisible; // visible border
  private Line3D mLine;

  SketchAreaPath( int type, String s1, String s2, String id, boolean visible )
  {
    super( DrawingPath.DRAWING_PATH_AREA, s1, s2 );
    // mViewType = SketchDef.VIEW_3D;
    mThType = type;
    if ( id != null ) {
      mAreaCnt = Integer.parseInt( id.substring(1) );
      if ( mAreaCnt > area_id_cnt ) area_id_cnt = mAreaCnt;
    } else {
      ++area_id_cnt;
      mAreaCnt = area_id_cnt;
    }
    mVisible = visible;
  }

  void addPoint( float x, float y, float z )
  {
    mLine.points.add( new Vector(x,y,z) );
  }

  void addPoint3( float x1, float y1, float z1, float x2, float y2, float z2, float x, float y, float z )
  {
    mLine.points.add( new Vector( x,y,z ) );
  }

  void close()
  {
    // FIXME TODO
  }

  void setAreaType( int t )
  {
    mThType = t;
  }

  int areaType() { return mThType; }

  @Override
  public String toTherion()
  {
    // FIXME if ( mLine.size() == 0 ) return null;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line border -id a%d -close on ", mAreaCnt );
    if ( ! mVisible ) pw.format("-visibility off ");
    pw.format("\n");
    for ( Vector pt : mLine.points ) {
      pt.toTherion( pw );
    }
    pw.format("endline\n");
    pw.format("area %s\n", BrushManager.mAreaLib.getSymbolThName( mThType ) );
    pw.format("  a%d\n", mAreaCnt );
    pw.format("endarea\n");
    return sw.getBuffer().toString();
  }

  void toTdr( BufferedOutputStream bos ) throws IOException
  {
    SketchModel.toTdr( bos, (short)3 );
    SketchModel.toTdr( bos, BrushManager.mAreaLib.getSymbolThName( mThType ) );
    SketchModel.toTdr( bos, st1 );
    SketchModel.toTdr( bos, st2 );
    // SketchModel.toTdr( bos, mAreaCnt );
    SketchModel.toTdr( bos, (short)(mLine.points.size()) );
    for ( Vector pt : mLine.points ) {
      SketchModel.toTdr( bos, pt.x, pt.y, pt.z);
    }
  }

}


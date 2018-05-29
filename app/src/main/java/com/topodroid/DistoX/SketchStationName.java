/* @file SketchStationName.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid 3d sketch: station name (this is not a station point)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.BufferedOutputStream;
import java.io.IOException;

import java.util.Locale;

import android.graphics.Canvas;
// import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Matrix;

// import android.util.Log;

/**
 */
class SketchStationName extends SketchPath
{
  private static float toTherion = 1.0f; // TDConst.TO_THERION;

  String mName;   // station name
  float x, y, z;  // world coordinates
  private float xc, yc;   // scene coordinates

  /**
   * @param   n   name
   * @param   x0  X world coordinate
   * @param   y0  Y world coordinate
   * @param   z0  Z world coordinate
   */
  SketchStationName( String n, float x0, float y0, float z0 )
  {
    super( DrawingPath.DRAWING_PATH_NAME, null, null );
    mName = n;
    x = x0;
    y = y0;
    z = z0;
    xc = 0.0f;
    yc = 0.0f;
  }

  // public void draw( Canvas canvas )
  // {
  //   Path path = new Path( );
  //   path.moveTo(0,0);
  //   path.lineTo(20,0);
  //   canvas.drawTextOnPath( mName, path, 0f, 0f, mPaint );
  // }

  void draw( Canvas canvas, Matrix matrix, Sketch3dInfo info )
  {
    Path path = new Path( );
    path.moveTo(0,0);
    path.lineTo(20,0);
    PointF q = new PointF();
    // project on (cos_clino*sin_azi, -cos_clino*cos_azimuth, -sin_clino)
    info.worldToSceneOrigin( x, y, z, q );
    xc = Sketch3dInfo.mXScale*q.x;
    yc = Sketch3dInfo.mXScale*q.y;
    path.offset( xc, yc );
    path.transform( matrix );
    canvas.drawTextOnPath( mName, path, 0f, 0f, mPaint );
  }

  float sceneDistance( float xx, float yy )
  {
    return (float)Math.sqrt( (xc-xx)*(xc-xx) + (yc-yy)*(yc-yy) );
  }

  @Override
  public String toTherion()
  {
    return String.format(Locale.US, "point %.2f %.2f %.2f station -name \"%s\"", 
      x*toTherion, -y*toTherion, -z*toTherion, mName );
  }

  void toTdr( BufferedOutputStream bos ) throws IOException
  {
    SketchModel.toTdr( bos, "stat" );
    SketchModel.toTdr( bos, mName );
    SketchModel.toTdr( bos, x*toTherion, y*toTherion, z*toTherion );
  }
  
}

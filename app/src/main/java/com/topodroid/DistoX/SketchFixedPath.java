/* @file SketchFixedPath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: fixed-line path 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
// import android.graphics.RectF;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
// import java.util.Iterator;
// import java.util.List;
// import java.util.ArrayList;

// import android.util.Log;

/**
 */
class SketchFixedPath extends SketchPath
{
  DBlock mBlock;
  private Paint mStepPaint;
  private float sx, sy;     // midpoint scene 2d coords
  private float cx, cy, cz; // midpoint 3d coords
  private Line3D  mLine;


  SketchFixedPath( int type, DBlock blk, Paint paint, Paint step_paint )
  {
    super( type, blk.mFrom, blk.mTo );
    mBlock = blk;
    mPaint = paint;
    mStepPaint = step_paint;
    mLine = new Line3D();
    sx = 0;
    sy = 0;
  }

  boolean isSplay() { return mBlock == null || mBlock.isSplay(); }

  /**
   * (x,y,z) world coords referred to the mInfo origin
   */
  void addPoint( float x, float y, float z )
  {
    // Log.v("DistoX", "add 3d point " + x + " " + y + " " + z );
    mLine.points.add( new Vector(x,y,z) );
  }

  float distance( float x, float y ) // 2D scene distance
  {
    return (float)( Math.abs( sx - x ) + Math.abs( sy - y ) );
  }

  float distance( float x, float y, float z ) // 3D distance
  {
    return (float)( Math.abs( cx - x ) + Math.abs( cy - y ) + Math.abs( cz - z ) );
  }

  void set3dMidpoint( float x, float y, float z )
  {
    cx = x;
    cy = y;
    cz = z;
  }

  void draw( Canvas canvas, Matrix matrix, Sketch3dInfo info, int activity_mode )
  {
    Path  path = new Path();
    int np = 0;
    PointF q = new PointF();
    for ( Vector p : mLine.points ) {
      info.worldToSceneOrigin( p.x, p.y, p.z, q );
      if ( np == 0 ) {
        path.moveTo( q.x, q.y );
        sx = q.x;
        sy = q.y;
        
      } else {
        path.lineTo( q.x, q.y );
        sx += q.x;
        sy += q.y;
      }
      ++ np;
    }
    path.transform( matrix );
    canvas.drawPath( path, mPaint );
    // if ( activity_mode == SketchDef.MODE_STEP && mStepPaint != null ) 
    if ( activity_mode == SketchDef.MODE_SELECT && mStepPaint != null )
    {
      float radius = 5 / info.zoom_3d;
      if ( np > 1 ) {
        sx /= np;
        sy /= np;
      }
      path = new Path();
      path.addCircle( sx, sy, radius, Path.Direction.CCW );
      path.transform( matrix );
      canvas.drawPath( path, mStepPaint );
    }
  }

  @Override
  public String toTherion()
  {
    // FIXME if ( mLine.size() == 0 ) return null;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line shot" );
    pw.format("\n");

    for ( Vector pt : mLine.points ) {
      pt.toTherion( pw );
    }
    pw.format("endline\n");
    return sw.getBuffer().toString();
  }

}


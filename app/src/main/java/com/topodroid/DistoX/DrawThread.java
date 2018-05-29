/* @file DrawThread.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid  surface (canvas) drawing thread
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.view.SurfaceHolder;

// import android.util.Log;


class DrawThread extends  Thread
{
  private volatile SurfaceHolder mHolder;
  private volatile boolean mRunning;

  private IDrawingSurface mParent;

  DrawThread( IDrawingSurface parent, SurfaceHolder holder)
  {
    // TDLog.Log( TDLog.LOG_PLOT, "draw thread cstr");
    mParent = parent;
    mHolder = holder;
  }

  void setHolder( SurfaceHolder holder )
  {
    // TDLog.Log( TDLog.LOG_PLOT, "draw thread set holder " + ( ( holder == null )? "null" : "non-null" ) );
    mHolder = holder;
  }

  public void setRunning( boolean run ) { mRunning = run; }

  public boolean isRunning() { return mRunning; }

  @Override
  public void run() 
  {
    // TDLog.Log( TDLog.LOG_PLOT, "draw thread run");
    mRunning = true;
    while ( mRunning ) {
      if ( mHolder != null && mParent.isDrawing() ) {
        mParent.refresh( mHolder );
        Thread.yield();
        try { Thread.sleep(1); } catch ( InterruptedException e ) { } // FIXME this is necessary
      } else {
        try { Thread.sleep(100); } catch ( InterruptedException e ) { }
      }
    }
    // TDLog.Log( TDLog.LOG_PLOT, "draw thread exit");
  }
}


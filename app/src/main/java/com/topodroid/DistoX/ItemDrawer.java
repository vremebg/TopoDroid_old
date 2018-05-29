/** @file ItemDrawer.java
 *
 * @author marco corvi
 * @date oct 2014
 *
 * @brief TopoDroid label adder interfare
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Activity;

// import android.util.Log;

public class ItemDrawer extends Activity
{
  static final int POINT_MAX = 32678;

  protected Activity mActivity = null;

  int mCurrentPoint;
  int mCurrentLine;
  int mCurrentArea;
  protected int mPointScale;
  protected int mLinePointStep = 1;

  int mSymbol = Symbol.LINE; // kind of symbol being drawn

  // -----------------------------------------------------------
  static Symbol mRecentPoint[] = { null, null, null, null, null, null };
  static Symbol mRecentLine[]  = { null, null, null, null, null, null };
  static Symbol mRecentArea[]  = { null, null, null, null, null, null };
  static final int NR_RECENT = 6; // max is 6

  void setPointScale( int scale )
  {
    if ( scale >= DrawingPointPath.SCALE_XS && scale <= DrawingPointPath.SCALE_XL ) mPointScale = scale;
  }

  int getPointScale() { return mPointScale; }

  // --------------------------------------------------------------
  // MOST RECENT SYMBOLS
  // recent symbols are stored with their filenames
  //
  // update of the "recent" arrays is done either with symbol index, or with symbol itself
  // load and save is done using a string of symbol filenames (separated by space)

  static void updateRecentPoint( int point )
  {
    // if ( BrushManager.mPointLib == null ) return;
    updateRecent( BrushManager.mPointLib.getSymbolByIndex( point ), mRecentPoint );
  }

  static void updateRecentLine( int line )
  {
    // if ( BrushManager.mLineLib == null ) return;
    updateRecent( BrushManager.mLineLib.getSymbolByIndex( line ), mRecentLine );
  }

  static void updateRecentArea( int area )
  {
    // if ( BrushManager.mAreaLib == null ) return;
    updateRecent( BrushManager.mAreaLib.getSymbolByIndex( area ), mRecentArea );
  }

  static void updateRecentPoint( Symbol point ) { updateRecent( point, mRecentPoint ); }

  static void updateRecentLine( Symbol line ) { updateRecent( line, mRecentLine ); }

  static void updateRecentArea( Symbol area ) { updateRecent( area, mRecentArea ); }

  private static void updateRecent( Symbol symbol, Symbol symbols[] )
  {
    if ( symbol == null ) return;
    for ( int k=0; k<NR_RECENT; ++k ) {
      if ( symbol == symbols[k] ) {
        for ( ; k > 0; --k ) symbols[k] = symbols[k-1];
        symbols[0] = symbol;
        break;
      }
    }
    if ( symbols[0] != symbol ) {
      for ( int k = NR_RECENT-1; k > 0; --k ) symbols[k] = symbols[k-1];
      symbols[0] = symbol;
    }
  }

  // recent symbols are stored with their th_names
  //
  protected void loadRecentSymbols( DataHelper data )
  {
    // Log.v("DistoX", "load recent tools");
    BrushManager.mPointLib.setRecentSymbols( mRecentPoint );
    BrushManager.mLineLib.setRecentSymbols( mRecentLine );
    BrushManager.mAreaLib.setRecentSymbols( mRecentArea );

    String names = data.getValue( "recent_points" );
    if ( names != null ) {
      String points[] = names.split(" ");
      for ( String point : points ) {
        updateRecent( BrushManager.mPointLib.getSymbolByFilename( point ), mRecentPoint );
      }
    }
    names = data.getValue( "recent_lines" );
    if ( names != null ) {
      String lines[] = names.split(" ");
      for ( String line : lines ) {
        updateRecent( BrushManager.mLineLib.getSymbolByFilename( line ), mRecentLine );
      }
    }
    names = data.getValue( "recent_areas" );
    if ( names != null ) {
      String areas[] = names.split(" ");
      for ( String area : areas ) {
        updateRecent( BrushManager.mAreaLib.getSymbolByFilename( area ), mRecentArea );
      }
    }
  }

  protected void saveRecentSymbols( DataHelper data )
  {
    // Log.v("DistoX", "save recent tools");
    boolean first = false;
    if ( mRecentPoint[0] != null ) {
      StringBuilder points = new StringBuilder( );
      first = false;
      for ( int k=NR_RECENT-1; k>=0; --k ) {
        if ( mRecentPoint[k] != null ) {
          if ( first ) {
            points.append( " " + mRecentPoint[k].mThName );
          } else {
            first = true;
            points.append( mRecentPoint[k].mThName );
          }
        }
      }
      data.setValue( "recent_points", points.toString() );
    }

    if ( mRecentLine[0] != null ) {
      StringBuilder lines = new StringBuilder( );
      first = false;
      for ( int k=NR_RECENT-1; k>=0; --k ) {
        if ( mRecentLine[k] != null ) {
          if ( first ) {
            lines.append( " " + mRecentLine[k].mThName );
          } else {
            first = true;
            lines.append( mRecentLine[k].mThName );
          }
        }
      }
      data.setValue( "recent_lines", lines.toString() );
    }

    if ( mRecentArea[0] != null ) { 
      StringBuilder areas = new StringBuilder( );
      first = false;
      for ( int k=NR_RECENT-1; k>=0; --k ) {
        if ( mRecentArea[k] != null ) {
          if ( first ) {
            areas.append( " " + mRecentArea[k].mThName );
          } else {
            first = true;
            areas.append( mRecentArea[k].mThName );
          }
        }
      }
      data.setValue( "recent_areas", areas.toString() );
    }
  }

  // ----------------------------------------------------------------------
  // SELECTION

    public void areaSelected( int k, boolean update_recent ) 
    {
      mSymbol = Symbol.AREA;
      if ( k >= 0 && k < BrushManager.mAreaLib.mSymbolNr ) {
        mCurrentArea = k;
      }
      setTheTitle();
      if ( update_recent ) {
        updateRecentArea( mCurrentArea );
      }
      mLinePointStep = TDSetting.mLineType;
    }

    public void lineSelected( int k, boolean update_recent ) 
    {
      mSymbol = Symbol.LINE;
      if ( k >= 0 && k < BrushManager.mLineLib.mSymbolNr ) {
        mCurrentLine = k;
      }
      setTheTitle();
      if ( update_recent ) {
        updateRecentLine( mCurrentLine );
      }
      mLinePointStep = BrushManager.mLineLib.getStyleX( mCurrentLine );
      if ( mLinePointStep != POINT_MAX ) mLinePointStep *= TDSetting.mLineType;
    }

    public void pointSelected( int p, boolean update_recent )
    {
      mSymbol = Symbol.POINT;
      if ( p >= 0 && p < BrushManager.mPointLib.mSymbolNr ) {
        mCurrentPoint = p;
      }
      setTheTitle();
      if ( update_recent ) {
        updateRecentPoint( mCurrentPoint );
      }
    }

    public void setTheTitle() { }

}

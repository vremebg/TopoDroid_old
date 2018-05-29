/* @file PtCmapActivity.java
 *
 * @author marco corvi
 * @date sept 2015
 *
 * @brief TopoDroid PocketTopo colormap activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.io.File;
// import java.util.List;
// import java.util.ArrayList;

// import android.app.Application;
import android.app.Activity;
import android.os.Bundle;

// import android.content.Context;
import android.content.Intent;
// import android.content.res.Resources;

// import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
// import android.view.WindowManager.LayoutParams;
import android.view.KeyEvent;

// import android.widget.Toast;

// import android.util.Log;

public class PtCmapActivity extends Activity
                            implements OnClickListener
{
  private TopoDroidApp mApp;

  static String mCmapLine[] = { "wall", "border", "pit", "rock-border", "arrow", "contour", "user" };
  static String mCmapPoint[] = { "air-draught", "water-flow", "stalactite", "blocks", "debris", "pebbles", "clay" };

  private EditText mETline[];
  private EditText mETpoint[];

  private Button mBtOk;

  static String getLineThName( int k ) 
  {
    if ( k < 1 || k > 7 ) return "user";
    return mCmapLine[k-1];
  }

  static String getPointThName( int k ) 
  {
    if ( k < 1 || k > 7 ) return "user";
    return mCmapPoint[k-1];
  }

  static void setMap( String cmap )
  {
    if ( cmap == null ) return;
    String vals[] = cmap.split(" ");
    if ( vals.length < 14 ) return;
    for ( int k=0; k<7; ++k ) {
      mCmapLine[k] = vals[k];
      mCmapPoint[k] = vals[7+k];
    }
  }

  private boolean setPreference()
  {
    StringBuilder sb = new StringBuilder();
    for ( int k=0; k<7; ++k ) {
      String txt = mETline[k].getText().toString().trim();
      if ( ! BrushManager.mLineLib.hasSymbolByThName( txt ) ) {
        mETline[k].setError( getResources().getString( R.string.bad_line ) );
        return false;
      }
      if ( k > 0 ) sb.append( " " );
      sb.append( txt );
    }
    for ( int k=0; k<7; ++k ) {
      String txt = mETpoint[k].getText().toString().trim();
      if ( ! BrushManager.mPointLib.hasSymbolByThName( txt ) ) {
        mETpoint[k].setError( getResources().getString( R.string.bad_point ) );
        return false;
      }
      sb.append( " " );
      sb.append( txt );
    }
    String cmap = sb.toString();

    mApp.setPtCmapPreference( cmap );
    Intent intent = new Intent();
    intent.putExtra( TDTag.TOPODROID_CMAP, cmap );
    setResult( RESULT_OK, intent );
    return true;
  }
    

  // ---------------------------------------------------------------
  // list items click

  
  @Override
  public void onCreate( Bundle b )
  {
    super.onCreate( b );
    setContentView(R.layout.pt_cmap_activity);
    mApp = (TopoDroidApp) getApplication();

    getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN );

    ((Button) findViewById( R.id.btn0 ) ).setBackgroundColor( TDColor.DARK_GRAY );
    ((Button) findViewById( R.id.btn1 ) ).setBackgroundColor( TDColor.LIGHT_GRAY );
    ((Button) findViewById( R.id.btn2 ) ).setBackgroundColor( TDColor.FIXED_ORANGE );
    ((Button) findViewById( R.id.btn3 ) ).setBackgroundColor( TDColor.FULL_BLUE );
    ((Button) findViewById( R.id.btn4 ) ).setBackgroundColor( TDColor.FULL_RED );
    ((Button) findViewById( R.id.btn5 ) ).setBackgroundColor( TDColor.FULL_GREEN );
    ((Button) findViewById( R.id.btn6 ) ).setBackgroundColor( TDColor.FIXED_YELLOW );

    mETline  = new EditText[7];
    mETpoint = new EditText[7];

    mETline[0] = (EditText) findViewById( R.id.etline0 );
    mETline[1] = (EditText) findViewById( R.id.etline1 );
    mETline[2] = (EditText) findViewById( R.id.etline2 );
    mETline[3] = (EditText) findViewById( R.id.etline3 );
    mETline[4] = (EditText) findViewById( R.id.etline4 );
    mETline[5] = (EditText) findViewById( R.id.etline5 );
    mETline[6] = (EditText) findViewById( R.id.etline6 );

    mETline[0].setText( mCmapLine[0] );
    mETline[1].setText( mCmapLine[1] );
    mETline[2].setText( mCmapLine[2] );
    mETline[3].setText( mCmapLine[3] );
    mETline[4].setText( mCmapLine[4] );
    mETline[5].setText( mCmapLine[5] );
    mETline[6].setText( mCmapLine[6] );

    mETpoint[0] = (EditText) findViewById( R.id.etpoint0 );
    mETpoint[1] = (EditText) findViewById( R.id.etpoint1 );
    mETpoint[2] = (EditText) findViewById( R.id.etpoint2 );
    mETpoint[3] = (EditText) findViewById( R.id.etpoint3 );
    mETpoint[4] = (EditText) findViewById( R.id.etpoint4 );
    mETpoint[5] = (EditText) findViewById( R.id.etpoint5 );
    mETpoint[6] = (EditText) findViewById( R.id.etpoint6 );

    mETpoint[0].setText( mCmapPoint[0] );
    mETpoint[1].setText( mCmapPoint[1] );
    mETpoint[2].setText( mCmapPoint[2] );
    mETpoint[3].setText( mCmapPoint[3] );
    mETpoint[4].setText( mCmapPoint[4] );
    mETpoint[5].setText( mCmapPoint[5] );
    mETpoint[6].setText( mCmapPoint[6] );

    mBtOk = (Button) findViewById( R.id.button_ok );

    mBtOk.setOnClickListener( this );
  }

  // @Override
  // public synchronized void onStop()
  // { 
  //   super.onStop();
  // }

  @Override
  public void onClick( View v )
  {
    if ( (Button)v == mBtOk ) {
      if ( setPreference() ) {
        finish();
      }
    }
  }

  // @Override
  // public void onBackPressed()
  // {
  //   finish();
  // }


  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        onBackPressed();
        return true;
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.PtCmapActivity );
        if ( help_page != null ) UserManualActivity.showHelpPage( this, help_page );
        return true;
      // case KeyEvent.KEYCODE_SEARCH:
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }
}


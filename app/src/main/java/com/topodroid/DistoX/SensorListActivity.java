/* @file SensorListActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey sensor listing
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.io.File;
// import java.io.IOException;
// import java.io.EOFException;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
// import java.io.BufferedReader;
// import java.io.FileReader;
// import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;

// import android.app.Application;
import android.app.Activity;

// import android.content.ActivityNotFoundException;
// import android.content.res.ColorStateList;
// import android.content.Context;
// import android.content.Intent;

// import android.view.Menu;
// import android.view.MenuItem;
// import android.view.SubMenu;
// import android.view.MenuInflater;
import android.view.KeyEvent;

// import android.location.LocationManager;

// import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
// import android.widget.Toast;
// import android.app.Dialog;
// import android.widget.Button;
import android.view.View;
// import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.preference.PreferenceManager;

// import android.provider.MediaStore;
// import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;
// import android.net.Uri;

public class SensorListActivity extends Activity
                                implements OnItemClickListener
{
  private TopoDroidApp mApp;

  private ListView mList;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private SensorAdapter   mDataAdapter;
  private long mShotId = -1;   // id of the shot

  private String mSaveData = "";
  private TextView mSaveTextView = null;
  private SensorInfo mSaveSensor = null;

  String mSensorComment;
  long   mSensorId;

  // -------------------------------------------------------------------

  private void updateDisplay( )
  {
    // TDLog.Log( TDLog.LOG_SENSOR, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    if ( TopoDroidApp.mData != null && mApp.mSID >= 0 ) {
      List< SensorInfo > list = TopoDroidApp.mData.selectAllSensors( mApp.mSID, TDStatus.NORMAL );
      // TDLog.Log( TDLog.LOG_PHOTO, "update shot list size " + list.size() );
      updateSensorList( list );
      setTitle( mApp.mySurvey );
    // } else {
    //   TDToast.make( this, R.string.no_survey );
    }
  }

  public void updateSensorList( List< SensorInfo > list )
  {
    // TDLog.Log(TDLog.LOG_SENSOR, "updateSensorList size " + list.size() );
    mDataAdapter.clear();
    mList.setAdapter( mDataAdapter );
    if ( list.size() == 0 ) {
      TDToast.make( this, R.string.no_sensors );
      finish();
    }
    for ( SensorInfo item : list ) {
      mDataAdapter.add( item );
    }
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    // TDLog.Log( TDLog.LOG_INPUT, "SensorListActivity onItemClick id " + id);
    startSensorDialog( (TextView)view, position );
  }

  void startSensorDialog( TextView tv, int pos )
  {
     mSaveSensor = mDataAdapter.get(pos);
     (new SensorEditDialog( this, this, mSaveSensor )).show();
  }

  // ---------------------------------------------------------------
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.sensor_list_activity);
    
    mApp = (TopoDroidApp) getApplication();
    mDataAdapter = new SensorAdapter( this, R.layout.row, new ArrayList< SensorInfo >() );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    updateDisplay( );
  }

  // ------------------------------------------------------------------

  void dropSensor( SensorInfo sensor )
  {
    TopoDroidApp.mData.deleteSensor( sensor.sid, sensor.id );
    updateDisplay( ); // FIXME
  }

  void updateSensor( SensorInfo sensor, String comment )
  {
    // TDLog.Log( TDLog.LOG_SENSOR, "updateSensor comment " + comment );
    if ( TopoDroidApp.mData.updateSensor( sensor.sid, sensor.id, comment ) ) {
      // if ( app.mListRefresh ) {
      //   // This works but it refreshes the whole list
      //   mDataAdapter.notifyDataSetChanged();
      // } else {
      //   mSaveSensor.mComment = comment;
      // }
      updateDisplay(); // FIXME
    } else {
      TDToast.make( this, R.string.no_db );
    }
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.SensorListActivity );
        if ( help_page != null ) UserManualActivity.showHelpPage( this, help_page );
        return true;
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      // case KeyEvent.KEYCODE_SEARCH:
        // return onSearchRequested();
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }
}

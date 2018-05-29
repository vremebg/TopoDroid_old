/* @file SensorActivity.java
 *
 * @author marco corvi
 * @date aug 2012
 *
 * @brief TopoDroid DistoX sensor activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;
import android.content.Context;

import android.widget.RadioButton;
import android.widget.Button;
import android.widget.EditText;
// import android.widget.Toast;
import android.view.View;
import android.view.KeyEvent;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

// import android.util.Log;


public class SensorActivity extends Activity
                            implements View.OnClickListener
{ 
  private TopoDroidApp app;
  private SensorManager mSensorManager;
  private float[] mValues;
  private float mAlpha = 0.85f;
  private boolean mFirst;
  private int mSensorType; // current sensor type
  private ArrayList< Sensor > mSensor;

  private RadioButton mRBLight = null;
  private RadioButton mRBMagnetic = null;
  private RadioButton mRBTemperature = null;
  private RadioButton mRBPressure = null;
  private RadioButton mRBGravity = null;
  // private RadioButton mRBHumidity = null;
  private RadioButton mRBExtern = null;

  private EditText mETtype;
  private EditText mETvalue;
  private EditText mETcomment;

  // private Button mBTtype;
  private Button mBTok;
  private Button mBTcancel;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.sensor_activity);
    app = (TopoDroidApp) getApplication();
    mSensorManager = (SensorManager)getSystemService( Context.SENSOR_SERVICE );

    mValues = new float[3];
    mFirst  = true;
    mAlpha  = 0.8f;

    mRBLight         = ( RadioButton ) findViewById( R.id.sensor_light );
    mRBMagnetic      = ( RadioButton ) findViewById( R.id.sensor_magnetic_field );
    mRBTemperature   = ( RadioButton ) findViewById( R.id.sensor_temperature );
    mRBPressure      = ( RadioButton ) findViewById( R.id.sensor_pressure );
    mRBGravity       = ( RadioButton ) findViewById( R.id.sensor_gravity );
    // mRBHumidity      = ( RadioButton ) findViewById( R.id.sensor_humidity );
    mRBExtern        = ( RadioButton ) findViewById( R.id.sensor_extern );

    mETtype  = ( EditText ) findViewById( R.id.sensor_type );
    mETvalue = ( EditText ) findViewById( R.id.sensor_value );
    mETcomment = ( EditText ) findViewById( R.id.sensor_comment );

    mSensor = new ArrayList<>();

    mRBExtern.setOnClickListener( this );

    List< Sensor > sl = mSensorManager.getSensorList( Sensor.TYPE_LIGHT );
    if ( sl.size() > 0 ) {
      mRBLight.setOnClickListener( this );
      for ( Sensor s : sl ) mSensor.add( s );
    } else {
      mRBLight.setEnabled( false );
    }
    
    sl = mSensorManager.getSensorList( Sensor.TYPE_MAGNETIC_FIELD );
    if ( sl.size() > 0 ) {
      mRBMagnetic.setOnClickListener( this );
      for ( Sensor s : sl ) mSensor.add( s );
    } else {
      mRBMagnetic.setEnabled( false );
    }

    sl = mSensorManager.getSensorList( Sensor.TYPE_TEMPERATURE );
    if ( sl.size() > 0 ) {
      mRBTemperature.setOnClickListener( this );
      for ( Sensor s : sl ) mSensor.add( s );
    } else {
      mRBTemperature.setEnabled( false );
    }

    sl = mSensorManager.getSensorList( Sensor.TYPE_PRESSURE );
    if ( sl.size() > 0 ) {
      mRBPressure.setOnClickListener( this );
      for ( Sensor s : sl ) mSensor.add( s );
    } else {
      mRBPressure.setEnabled( false );
    }

    sl = mSensorManager.getSensorList( Sensor.TYPE_ORIENTATION );
    if ( sl.size() > 0 ) {
      mRBGravity.setOnClickListener( this );
      for ( Sensor s : sl ) mSensor.add( s );
    } else {
      mRBGravity.setEnabled( false );
    }

    // sl = mSensorManager.getSensorList( Sensor.TYPE_RELATIVE_HUMIDITY );
    // if ( sl.size() > 0 ) {
    //   mRBHumidity.setOnClickListener( this );
    //   for ( Sensor s : sl ) mSensor.add( s );
    // } else {
    //   mRBHumidity.setEnabled( false );
    // }

    mBTok     = ( Button ) findViewById( R.id.sensor_ok );
    mBTcancel = ( Button ) findViewById( R.id.sensor_cancel );

    mBTok.setOnClickListener( this );
    mBTcancel.setOnClickListener( this );
    // setTitleColor( TDColor.TITLE_NORMAL );

    // mETtype.setText( null );
    // mETvalue.setText( null );
  }

  private void setSensor( )
  { 
    if ( mSensorType != -1 ) {
      mSensorManager.unregisterListener(mListener);
    }
    mETvalue.setText( null );
    mSensorType = -1;
    if ( mRBLight != null && mRBLight.isChecked() ) {
      mSensorType = Sensor.TYPE_LIGHT;
      mETtype.setText( R.string.sensor_light );
    } else if ( mRBMagnetic != null && mRBMagnetic.isChecked() ) {
      mSensorType = Sensor.TYPE_MAGNETIC_FIELD;
      mETtype.setText( R.string.sensor_magnetic_field );
    } else if ( mRBTemperature != null && mRBTemperature.isChecked() ) {
      mSensorType = Sensor.TYPE_TEMPERATURE; //  Sensor.TYPE_AMBIENT_TEMPERATURE;
      mETtype.setText( R.string.sensor_temperature );
    } else if ( mRBPressure != null && mRBPressure.isChecked() ) {
      mSensorType = Sensor.TYPE_PRESSURE;
      mETtype.setText( R.string.sensor_pressure );
    } else if ( mRBGravity != null && mRBGravity.isChecked() ) {
      mSensorType = Sensor.TYPE_ORIENTATION; // Sensor.TYPE_GRAVITY;
      mETtype.setText( R.string.sensor_gravity );
    // } else if ( mRBHumidity != null && mRBHumidity.isChecked() ) {
    //   mSensorType = Sensor.TYPE_RELATIVE_HUMIDITY;
    //   mETtype.setText( R.string.sensor_humidity );
    } else if ( mRBExtern != null && mRBExtern.isChecked() ) {
      mSensorType = -1;
      mETtype.setText( null );
      mSensorManager.unregisterListener(mListener);
    }
    if ( mSensorType != -1 ) {
      mFirst = true;
      mAlpha = 0.8f;
      registerSensorEventListener();
    }
  }


  private void registerSensorEventListener()
  {
    if ( mSensorType != -1 ) {
      for ( Sensor s : mSensor ) {
        if ( s.getType() == mSensorType ) {
          // Log.v("DistoX", "register sensor type " + mSensorType );
          mSensorManager.registerListener(mListener, s, SensorManager.SENSOR_DELAY_NORMAL);
        }
      }
    }
  }
    
  private final SensorEventListener mListener = new SensorEventListener() 
  {
    @Override
    public void onSensorChanged( SensorEvent event)
    {
      if ( event.sensor.getType() == mSensorType ) {
        // TDLog.Log( TDLog.LOG_SENSOR,
        //   "sensorChanged (" + evenmt.values[0] + ", " + evenmt.values[1] + ", " + evenmt.values[2] + ")");
        // if ( mFirst ) Log.v("DistoX", "sensor changed first " + event.values[0] );
        String value = "";
        switch ( mSensorType ) {
          case Sensor.TYPE_LIGHT:
          // case Sensor.TYPE_PROXIMITY:
          case Sensor.TYPE_TEMPERATURE:
          case Sensor.TYPE_PRESSURE:
          // case Sensor.TYPE_RELATIVE_HUMIDITY:
            if ( mFirst ) {
              mValues[0] = event.values[0];
              mFirst = false;
            } else {
              mValues[0] = ( 1 - mAlpha ) * mValues[0] + mAlpha * event.values[0];
              if ( mAlpha > 0.01f ) mAlpha *= 0.9f;
            }
            value = String.format(Locale.US, "%.2f", mValues[0] );
            break;
          case Sensor.TYPE_MAGNETIC_FIELD:
          case Sensor.TYPE_ORIENTATION:
          // case Sensor.TYPE_ACCELEROMETER:
          default:
            if ( mFirst ) {
              mFirst = false;
              mValues[0] = event.values[0];
              mValues[1] = event.values[1];
              mValues[2] = event.values[2];
            } else {
              mValues[0] = ( 1 - mAlpha ) * mValues[0] + mAlpha * event.values[0];
              mValues[1] = ( 1 - mAlpha ) * mValues[1] + mAlpha * event.values[1];
              mValues[2] = ( 1 - mAlpha ) * mValues[2] + mAlpha * event.values[2];
              if ( mAlpha > 0.01f ) mAlpha *= 0.9f;
            }
            value = String.format(Locale.US, "%.2f %.2f %.2f", mValues[0], mValues[1], mValues[2] );
            break;
        }
        mETvalue.setText( value );
      }
    }

    @Override
    public void onAccuracyChanged( Sensor sensor, int accuracy )
    {
      // TODO Auto-generated method stub
      // Log.v("DistoX", "sensor accuracy changed");
      mFirst = true;
      mAlpha = 0.8f;
    }
  };

  @Override
  public void onClick( View view )
  {
    Button b = (Button) view;
    // TDLog.Log(  TDLog.LOG_INPUT, "SensorActivity onClick() button " + b.getText().toString() );
    String error;
    if ( b == mBTok ) {
      String type = mETtype.getText().toString().trim();
      String value = mETvalue.getText().toString().trim();
      String comment = mETcomment.getText().toString().trim();
      if ( type.length() == 0 ) {
        error = getResources().getString( R.string.error_sensor_required );
        mETtype.setError( error );
        return;
      }
      if (  value.length() == 0 ) {
        error = getResources().getString( R.string.error_value_required );
        mETvalue.setError( error );
        return;
      }
      // TDLog.Log( TDLog.LOG_SENSOR, "sensor " + type + " " + value );
      Intent intent = new Intent();
      intent.putExtra( TDTag.TOPODROID_SENSOR_TYPE, type );
      intent.putExtra( TDTag.TOPODROID_SENSOR_VALUE, value );
      intent.putExtra( TDTag.TOPODROID_SENSOR_COMMENT, comment );
      setResult( RESULT_OK, intent );
      finish();
    } else if ( b == mBTcancel ) {
      setResult( RESULT_CANCELED );
      if ( mSensorType != -1 ) {
        mSensorManager.unregisterListener(mListener);
      }
      finish();
    } else { 
      setSensor();
    }
  }  
    
  @Override
  protected void onResume()
  {
    super.onResume();
    registerSensorEventListener();
  }
    
  @Override
  protected void onStop()
  {
     mSensorManager.unregisterListener(mListener);
     super.onStop();
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.SensorActivity );
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



/** @file SurveyCalibrationDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device info dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Locale;

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;
// import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;

class SurveyCalibrationDialog extends MyDialog
                       implements View.OnClickListener
{
  private EditText mETlength;
  private EditText mETazimuth;
  private EditText mETclino;
  private Button mBTok;
  private Button mBTback;
  private CheckBox mCBlrud;

  TopoDroidApp mApp; 

  SurveyCalibrationDialog( Context context, TopoDroidApp app )
  {
    super( context, R.string.SurveyCalibrationDialog );
    mApp    = app;
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    initLayout( R.layout.survey_calibration_dialog, R.string.calibration_title );

    mETlength  = (EditText) findViewById( R.id.et_length );
    mETazimuth = (EditText) findViewById( R.id.et_azimuth );
    mETclino   = (EditText) findViewById( R.id.et_clino );
    mETlength.setText(  String.format(Locale.US, "%.2f", ManualCalibration.mLength ) );
    mETazimuth.setText( String.format(Locale.US, "%.1f", ManualCalibration.mAzimuth ) );
    mETclino.setText(   String.format(Locale.US, "%.1f", ManualCalibration.mClino ) );

    mCBlrud   = (CheckBox) findViewById( R.id.cb_lrud );
    mCBlrud.setChecked( ManualCalibration.mLRUD );

    mBTok = (Button) findViewById( R.id.button_ok );
    mBTok.setOnClickListener( this );
    mBTback = (Button) findViewById( R.id.button_back );
    mBTback.setOnClickListener( this );
  }

  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    if ( b == mBTok ) {
      if ( mETlength.getText() != null ) {
        try {
          ManualCalibration.mLength = Float.parseFloat( mETlength.getText().toString() );
        } catch ( NumberFormatException e ) { }
      }
      if ( mETazimuth.getText() != null ) {
        try {
          ManualCalibration.mAzimuth = Float.parseFloat( mETazimuth.getText().toString() );
        } catch ( NumberFormatException e ) { }
      }
      if ( mETclino.getText() != null ) {
        try {
          ManualCalibration.mClino = Float.parseFloat( mETclino.getText().toString() );
        } catch ( NumberFormatException e ) { }
      }
      ManualCalibration.mLRUD = mCBlrud.isChecked();

    // } else if ( b == mBTback ) {
    //   /* nothing */
    }
    dismiss();
  }

}

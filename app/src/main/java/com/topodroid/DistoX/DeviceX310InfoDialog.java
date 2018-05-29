/** @file DeviceX310InfoDialog.java
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

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;
import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.Button;

class DeviceX310InfoDialog extends MyDialog
                           implements View.OnClickListener
{
  private RadioButton mRBa3;
  private RadioButton mRBx310;
  private Button mBTok;
  private Button mBTback;

  DeviceActivity mParent;
  Device mDevice;

  private TextView tv_code;
  private TextView tv_firmware;
  private TextView tv_hardware;

  DeviceX310InfoDialog( Context context, DeviceActivity parent, Device device )
  {
    super( context, R.string.DeviceX310InfoDialog );
    mParent = parent;
    mDevice = device;
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    initLayout( R.layout.device_x310_info_dialog, R.string.device_info );

    Resources res = mParent.getResources();

    mRBa3   = (RadioButton) findViewById( R.id.rb_a3 );
    mRBx310 = (RadioButton) findViewById( R.id.rb_x310 );
    // mRBa3.setChecked( false );
    mRBx310.setChecked( true );

    TextView tv_address  = (TextView) findViewById( R.id.tv_address );
    tv_code     = (TextView) findViewById( R.id.tv_code );
    tv_firmware = (TextView) findViewById( R.id.tv_firmware );
    tv_hardware = (TextView) findViewById( R.id.tv_hardware );

    tv_address.setText( String.format( res.getString( R.string.device_address ), mDevice.mAddress ) );
    tv_code.setText( res.getString( R.string.getting_info ) );
    // tv_firmware.setText( "" );
    // tv_hardware.setText( "" );
    mParent.readX310Info( this );

    mBTok = (Button) findViewById( R.id.btn_ok );
    mBTback = (Button) findViewById( R.id.button_cancel );
    mBTok.setOnClickListener( this );
    mBTback.setOnClickListener( this );
  }

  void updateInfo( DeviceX310Info info )
  {
    if ( info == null ) return;
    tv_code.setText(     info.mCode );
    tv_firmware.setText( info.mFirmware );
    tv_hardware.setText( info.mHardware );
  }

  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;
    if ( b == mBTok ) {
      // TODO ask confirm
      TopoDroidAlertDialog.makeAlert( mContext, mParent.getResources(),
                                mParent.getResources().getString( R.string.device_model_set ) + " ?",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doSetModel( );
          }
        }
      );
    } else if ( b == mBTback ) {
      dismiss();
    }
  }

  private void doSetModel()
  {
    if ( mRBa3.isChecked() ) {
      mParent.setDeviceModel( mDevice, Device.DISTO_A3 );
    } else if ( mRBx310.isChecked() ) {
      mParent.setDeviceModel( mDevice, Device.DISTO_X310 );
    }
  }

}

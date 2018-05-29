/** @file DeviceX310MemoryDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device memory dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

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

import android.widget.Button;
import android.widget.EditText;
// import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

class DeviceX310MemoryDialog extends MyDialog
                             implements View.OnClickListener
                             , IMemoryDialog
{
  // private Button mBtnDump;
  // private Button mBtnBack;

  private EditText mETdumpfrom;
  private EditText mETdumpto;
  private EditText mETdumpfile;

  // List< MemoryOctet> mMemory;
  ArrayAdapter< String > mArrayAdapter;
  ListView mList;

  DeviceActivity mParent;

  DeviceX310MemoryDialog( Context context, DeviceActivity parent )
  {
    super( context, R.string.DeviceX310MemoryDialog );
    mParent = parent;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    initLayout( R.layout.device_x310_memory_dialog, R.string.memoryX310 );

    mETdumpfrom  = (EditText) findViewById( R.id.et_dumpfrom );
    mETdumpto    = (EditText) findViewById( R.id.et_dumpto );
    mETdumpfile  = (EditText) findViewById( R.id.et_dumpfile );

    Button btnDump = (Button) findViewById(R.id.button_dump );
    btnDump.setOnClickListener( this );
    // mBtnBack = (Button) findViewById(R.id.button_cancel);
    // mBtnBack.setOnClickListener( this );
    
    mArrayAdapter = new ArrayAdapter<>( mParent, R.layout.message );
    mList = (ListView) findViewById(R.id.list_memory);
    mList.setAdapter( mArrayAdapter );
    // mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // int[] ht = new int[2];
    // mParent.retrieveDeviceHeadTail( ht );
    // setText( mTVshead, mTVstail, ht );
  }

  public void updateList( ArrayList<MemoryOctet> memory )
  {
    mArrayAdapter.clear();
    for ( MemoryOctet m : memory ) mArrayAdapter.add( m.toString() );
    mList.invalidate();
  }


  @Override
  public void onClick( View view )
  {
    int[] ht = new int[2];
    String from, to, error;
    switch ( view.getId() ) {
      case R.id.button_dump:
        from = mETdumpfrom.getText().toString();
        to   = mETdumpto.getText().toString();
        if ( /* from == null || */ from.length() == 0 ) {
          error = mParent.getResources().getString( R.string.error_begin_required );
          mETdumpfrom.setError( error );
          return;
        }
        if ( /* to == null || */ to.length() == 0 ) {
          error = mParent.getResources().getString( R.string.error_end_required );
          mETdumpto.setError( error );
          return;
        }
        try {
          ht[0] = Integer.parseInt( from );
        } catch ( NumberFormatException e ) {
          error = mParent.getResources().getString( R.string.error_invalid_number );
          mETdumpfrom.setError( error );
          return;
        }
        try {
          ht[1] = Integer.parseInt( to );
        } catch ( NumberFormatException e ) {
          error = mParent.getResources().getString( R.string.error_invalid_number );
          mETdumpto.setError( error );
          return;
        }
        if ( ht[0] < 0 ) ht[0] = 0;
        if ( ht[1] > DeviceX310Details.MAX_INDEX_X310 )  ht[1] = DeviceX310Details.MAX_INDEX_X310;
        if ( ht[0] < ht[1] ) {
          String file = null;
          if ( mETdumpfile.getText() != null ) file = mETdumpfile.getText().toString();
          mParent.readX310Memory( this, ht, file );
        }
        break;
      // case R.id.button_cancel:
      //   dismiss();
      //   break;
    }
  }

  // void askReset( final int ht[] )
  // {
  //   TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), R.string.ask_reset,
  //     new DialogInterface.OnClickListener() {
  //       @Override
  //       public void onClick( DialogInterface dialog, int btn ) {
  //         mParent.resetX310DeviceHeadTail( ht );
  //         // finish(); 
  //       }
  //     }
  //   );
  // }

}

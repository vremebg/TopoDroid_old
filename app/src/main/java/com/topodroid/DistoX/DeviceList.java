/* @file DeviceList.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX device list activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Set;

import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;

// import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.widget.Toast;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;


public class DeviceList extends Activity
                                implements OnItemClickListener
{ 
  private TopoDroidApp mApp;

  public static final int DEVICE_PAIR = 0x1;
  public static final int DEVICE_SCAN = 0x2;
  
  private ArrayAdapter<String> mArrayAdapter;
  // private ListView mList;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.device_list);
    mApp = (TopoDroidApp) getApplication();

    mArrayAdapter = new ArrayAdapter<>( this, R.layout.message );
    // mDataAdapter = new ArrayAdapter<>( this, R.layout.data );

    ListView list = (ListView) findViewById(R.id.list);
    list.setAdapter( mArrayAdapter );
    list.setOnItemClickListener( this );
    list.setDividerHeight( 2 );

    // setTitleColor( TDColor.TITLE_NORMAL );

    int command = 0;
    try {
      command = getIntent().getExtras().getInt( TDTag.TOPODROID_DEVICE_ACTION );
    } catch ( NullPointerException e ) { }

    // TDLog.Log( TDLog.LOG_BT, "command " + command );
    switch ( command )
    {
      case DEVICE_PAIR:
        showPairedDevices();
        break;
      case DEVICE_SCAN:
        scanBTDevices();
        break;
      default:  // 0x0 or unknown
         // TDLog.Log(TDLog.LOG_BT, "Unknown intent command! ("+command+")");
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    // TDLog.Log( TDLog.LOG_BT, "onItemClick " + mDistoX.StatusName() + " value: " + value + " pos " + position );
    if ( value.startsWith( "DistoX", 0 ) || value.startsWith( "A3", 0 ) || value.startsWith( "X310", 0 ) ) 
    {
      StringBuffer buf = new StringBuffer( item );
      int k = buf.lastIndexOf(" ");
      String address = buf.substring(k+1);
      // TDToast.make( mApp.getApplicationContext(), address );
      TDLog.Log( TDLog.LOG_BT, "DeviceList item click Address " + address );
      Intent intent = new Intent();
      intent.putExtra( TDTag.TOPODROID_DEVICE_ACTION, address );
      setResult( RESULT_OK, intent );
    } else {
      setResult( RESULT_CANCELED );
    }
    finish();
  }

  
  private void showPairedDevices()
  {
    if ( mApp.mBTAdapter != null ) {
      Set<BluetoothDevice> device_set = mApp.mBTAdapter.getBondedDevices();
      if ( device_set.isEmpty() ) {
        TDToast.make(this, R.string.no_paired_device );
      } else {
        setTitle( R.string.title_device );
        mArrayAdapter.clear();
        for ( BluetoothDevice device : device_set ) {
          // Log.v("DistoX", "paired device <" + device.getName() + "> " + device.getAddress() );
          mArrayAdapter.add( "DistoX " + device.getName() + " " + device.getAddress() );
       }
      }
      // TDLog.Log( TDLog.LOG_BT, "showPairedDevices n. " + mArrayAdapter.getCount() );
    } else {
      TDToast.make(this, R.string.not_available );
    }
  }

  private BroadcastReceiver mBTReceiver = null;

  private void scanBTDevices()
  {
    TDLog.Log( TDLog.LOG_BT, "scanBTDevices" );
    // Log.v( "DistoX", "scanBTDevices" );
    mArrayAdapter.clear();
    resetReceiver(); // FIXME should not be necessary
    mBTReceiver = new BroadcastReceiver() 
    {
      @Override
      public void onReceive( Context ctx, Intent data )
      {
        String action = data.getAction();
        // TDLog.Log( TDLog.LOG_BT, "onReceive action " + action );
        if ( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals( action ) ) {
          TDLog.Log(  TDLog.LOG_BT, "onReceive BT DISCOVERY STARTED" );
          setTitle(  R.string.title_discover );
        } else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) ) {
          TDLog.Log(  TDLog.LOG_BT, "onReceive BT DISCOVERY FINISHED, found " + mArrayAdapter.getCount() );
          setTitle( R.string.title_device );
          resetReceiver();
          if ( mArrayAdapter.getCount() < 1 ) { 
            TDToast.make( mApp.getApplicationContext(), R.string.no_device_found );
            finish(); // no need to keep list of scanned distox open
          }
        } else if ( BluetoothDevice.ACTION_FOUND.equals( action ) ) {
          BluetoothDevice device = data.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );
          TDLog.Log(  TDLog.LOG_BT, "onReceive BT DEVICES FOUND, name " + device.getName() );
          if ( device.getBondState() != BluetoothDevice.BOND_BONDED ) {
            String model = device.getName();
            if ( model != null && model.startsWith("DistoX") ) { // DistoX and DistoX2
              String device_addr = device.getAddress();
              String name = Device.modelToName( model );
              // Log.v( "DistoX", "scan receiver <" + name + "> " + device_addr ); 
              TopoDroidApp.mDData.insertDevice( device_addr, model, name );
              mArrayAdapter.add( Device.typeString[ Device.stringToType(model) ] + " " + name + " " + device_addr );
            }
          }
        // } else if ( BluetoothDevice.ACTION_ACL_CONNECTED.equals( action ) ) {
        //   TDLog.Log( TDLog.LOG_BT, "ACL_CONNECTED");
        // } else if ( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals( action ) ) {
        //   TDLog.Log( TDLog.LOG_BT, "ACL_DISCONNECT_REQUESTED");
        // } else if ( BluetoothDevice.ACTION_ACL_DISCONNECTED.equals( action ) ) {
        //   // Bundle extra = data.getExtras();
        //   // String device = extra.getString( BluetoothDevice.EXTRA_DEVICE ).toString();
        //   // Log.v("DistoX", "DeviceList ACL_DISCONNECTED from " + device );
        //   TDLog.Log( TDLog.LOG_BT, "ACL_DISCONNECTED");
        }
      }
    };
    IntentFilter foundFilter = new IntentFilter( BluetoothDevice.ACTION_FOUND );
    IntentFilter startFilter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_STARTED );
    IntentFilter finishFilter = new IntentFilter( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
    // IntentFilter connectedFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_CONNECTED );
    // IntentFilter disconnectRequestFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED );
    // IntentFilter disconnectedFilter = new IntentFilter( BluetoothDevice.ACTION_ACL_DISCONNECTED );

    // IntentFilter uuidFilter  = new IntentFilter( myUUIDaction );
    // IntentFilter bondFilter  = new IntentFilter( BluetoothDevice.ACTION_BOND_STATE_CHANGED );

    registerReceiver( mBTReceiver, foundFilter );
    registerReceiver( mBTReceiver, startFilter );
    registerReceiver( mBTReceiver, finishFilter );
    // registerReceiver( mBTReceiver, connectedFilter );
    // registerReceiver( mBTReceiver, disconnectRequestFilter );
    // registerReceiver( mBTReceiver, disconnectedFilter );
    // registerReceiver( mBTReceiver, uuidFilter );
    // registerReceiver( mBTReceiver, bondFilter );

    mArrayAdapter.clear();
    mApp.mBTAdapter.startDiscovery();
  }

  @Override
  public void onStop()
  {
    super.onStop();
    resetReceiver();
  }

  private void resetReceiver()
  {
    if ( mBTReceiver != null ) {
      TDLog.Log(  TDLog.LOG_BT, "resetReceiver");
      unregisterReceiver( mBTReceiver );
      mBTReceiver = null;
    }
  }

  // private void ensureDiscoverable()
  // {
  //   if ( mBTAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ) {
  //     Intent discoverIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
  //     discoverIntent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300 );
  //     startActivity( discoverIntent );
  //   }
  // }

}


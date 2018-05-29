/** @file PlotRecoverDialog.java
 *
 * @author marco corvi
 * @date jan 2015
 *
 * @brief TopoDroid plot recover dialog
 *
 * displays the stack of saved stations and allows to push 
 * a station on it or pop one from it
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;

// import java.util.List;
// import java.util.ArrayList;

import android.os.Bundle;
// import android.app.Dialog;

import android.content.Context;

import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.ViewGroup.LayoutParams;

import android.widget.ArrayAdapter;
// import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Button;
// import android.widget.CheckBox;
// import android.widget.TextView;
// import android.widget.EditText;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
// import android.widget.AdapterView.OnItemClickListener;
// import android.widget.AdapterView.OnItemLongClickListener;

// import android.widget.Toast;

// import android.util.Log;

class PlotRecoverDialog extends MyDialog
                        implements View.OnClickListener
                        , OnItemSelectedListener
                        // , OnItemClickListener
{
  private TopoDroidApp mApp;
  private DrawingWindow mParent; 

  // private TextView mTVfilename;
  private Button mBtnOK;
  private Button mBtnBack;

  // private ListView mList;
  private Spinner mSpin;
  ArrayAdapter<String> mAdapter;
  private String[] mFiles;
  private String mFilename;
  private String mSelected;
  long mType;             // plot type

  // type is either 1 or 2
  PlotRecoverDialog( Context context, DrawingWindow parent, String filename, long type )
  {
    super( context, R.string.PlotRecoverDialog );
    mParent   = parent;
    mFilename = filename;
    mType     = type;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.plot_recover_dialog, R.string.title_plot_reload );

    // mList = (ListView) findViewById( R.id.th2_list);
    // mList.setDividerHeight( 2 );
    // mList.setOnItemClickListener( this );
    // mTVfilename = (TextView) findViewById( R.id.filename );

    mSpin = (Spinner)findViewById( R.id.tdr_spin );
    mSpin.setOnItemSelectedListener( this );

    mBtnOK      = (Button) findViewById( R.id.btn_ok );
    mBtnBack    = (Button) findViewById( R.id.btn_back );
    mBtnOK.setOnClickListener( this );   // OK-SAVE
    mBtnBack.setOnClickListener( this );  

    updateList();
  }

  private String getAge( long age )
  {
    age /= 60000;
    if ( age < 120 ) return Long.toString(age) + "\'";
    age /= 60;
    if ( age < 24 ) return Long.toString(age) + "h";
    age /= 24;
    if ( age < 60 ) return Long.toString(age) + "d";
    age /= 30;
    if ( age < 24 ) return Long.toString(age) + "m";
    age /= 12;
    return Long.toString(age) + "y";
  }
    
  private void populateAdapter( String filename, String ext )
  {
    mFiles = new String[2 + TDPath.NR_BACKUP];
    int kk = 0;

    long millis = System.currentTimeMillis();
    File file = new File( filename );
    if ( file.exists() ) {
      String age = getAge( millis - file.lastModified() );
      String name  = mFilename + ext;
      mAdapter.add( age + " [" + Long.toString(file.length()) + "] " +  name );
      mFiles[kk++] = name;
      mSelected = name;
    }
    String filename1 = filename + TDPath.BCK_SUFFIX;
    file = new File( filename1 );
    if ( file.exists() ) {
      String age = getAge( millis - file.lastModified() );
      String name = mFilename + ext + TDPath.BCK_SUFFIX;
      mAdapter.add( age + " [" + Long.toString(file.length()) + "] " + name );
      mFiles[kk++] = name;
    }
    for ( int i=0; i< TDPath.NR_BACKUP; ++i ) {
      filename1 = filename + TDPath.BCK_SUFFIX + Integer.toString(i);
      file = new File( filename1 );
      if ( file.exists() ) {
        String age = getAge( millis - file.lastModified() );
	String name = mFilename + ext + TDPath.BCK_SUFFIX + Integer.toString(i);
        mAdapter.add( age + " [" + Long.toString(file.length()) + "] " + name );
        mFiles[kk++] = name;
      }
    }
  }

  private void updateList()
  {
    mAdapter = new ArrayAdapter<>( mContext, R.layout.message );

    // if ( TDSetting.mBinaryTh2 ) { // TDR BINARY
      populateAdapter( TDPath.getTdrFileWithExt( mFilename ), ".tdr" );
      // mTVfilename.setText( name );

    // } else {
    //   populateAdapter( TDPath.getTh2FileWithExt( mFilename ), ".th2" );
    //   mTVfilename.setText( mFilename + ".th2" );
    // }
    // mList.setAdapter( mAdapter );
    mSpin.setAdapter( mAdapter );
  }

  // @Override 
  // public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  // {
  //   CharSequence item = ((TextView) view).getText();
  //   String[] name = item.toString().split(" ");
  //   mTVfilename.setText( name[2] );
  // }

  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) { mSelected = mFiles[ pos ]; }

  @Override
  public void onNothingSelected( AdapterView av ) { mSelected = null; }

 
  @Override
  public void onClick(View v) 
  {
    // TDLog.Log(  TDLog.LOG_INPUT, "PlotRecoverDialog onClick() " );
    Button b = (Button) v;
    if ( b == mBtnOK ) { // OK
      // mParent.doRecover( mTVfilename.getText().toString(), mType );
      if ( mSelected != null ) mParent.doRecover( mSelected, mType );
      dismiss();
    } else if ( b == mBtnBack ) {
      dismiss();
    }
  }

}

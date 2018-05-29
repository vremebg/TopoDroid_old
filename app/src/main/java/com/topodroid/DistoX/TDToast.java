/* @file TDColor.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid colors
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.widget.Toast;
import android.widget.TextView;

import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
// import android.view.ViewGroup;
// import android.view.LayoutInflater;

import android.content.Context;

class TDToast
{
  static final private int mBgColor = 0xff6699ff;
  static final private int mGravity = Gravity.BOTTOM | Gravity.CENTER | Gravity.FILL_HORIZONTAL;
  static final private int SHORT    = Toast.LENGTH_SHORT;
  static final private int LONG     = Toast.LENGTH_LONG;

  static void make( Context context, int r ) { show( Toast.makeText( context, r, SHORT ) ); }

  static void make( Context context, String text ) { show( Toast.makeText( context, text, SHORT ) ); }
  
  static Toast makeToast( Context context, int r )
  {
    Toast toast = Toast.makeText( context, r, SHORT );
    show( toast );
    return toast;
  }

  static void makeLong( Context context, int r ) { show( Toast.makeText( context, r, LONG ) ); }

  static void makeLong( Context context, String text ) { show( Toast.makeText( context, text, LONG ) ); }

  static void makeBG( Context context, int r, int color )
  {
    Toast toast = Toast.makeText( context, r, SHORT );
    getView( toast, color );
    toast.setGravity( mGravity, 0, 0 );
    toast.show();
  }

  static void makeColor( Context context, int r, int color )
  {
    Toast toast = Toast.makeText( context, r, SHORT );
    View view = getView( toast );
    toast.setGravity( mGravity, 0, 0 );
    TextView tv = (TextView)view.findViewById( android.R.id.message );
    tv.setTextColor( color );
    toast.show();
  }

  static void makeBG( Context context, String str, int color )
  {
    Toast toast = Toast.makeText( context, str, SHORT );
    View view = getView( toast, color );
    toast.setGravity( mGravity, 0, 0 );
    toast.show();
  }

  static void makeColor( Context context, String str, int color )
  {
    Toast toast = Toast.makeText( context, str, SHORT );
    View view = getView( toast, color );
    toast.setGravity( mGravity, 0, 0 );
    TextView tv = (TextView)view.findViewById( android.R.id.message );
    tv.setTextColor( color );
    toast.show();
  }

  static void makeGravity( Context context, String str, int gravity )
  {
    Toast toast = Toast.makeText( context, str, SHORT );
    View view = getView( toast );
    toast.setGravity( gravity, 10, 10 );
    toast.show();
  }

  // ---------------------------------------------------------------------
  
  static private View getView( Toast toast )
  {
    View view = toast.getView();
    view.setOnClickListener( new OnClickListener() { public void onClick( View v ) { v.setVisibility( View.GONE ); } } );
    view.setBackgroundColor( mBgColor );
    return view;
  }

  static private View getView( Toast toast, int color )
  {
    View view = toast.getView();
    view.setOnClickListener( new OnClickListener() { public void onClick( View v ) { v.setVisibility( View.GONE ); } } );
    view.setBackgroundColor( color );
    return view;
  }

  static private void show( Toast toast )
  {
    View view = getView( toast );
    toast.setGravity( mGravity, 0, 0 );
    toast.show();
  }
}
  

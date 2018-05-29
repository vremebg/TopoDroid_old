/** @file MyDialog.java
 *
 * @author marco corvi
 * @date jan 2016
 *
 * @brief TopoDroid generic dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.app.Activity;
import android.app.Dialog;
// import android.os.Bundle;
import android.content.Context;
// import android.content.Intent;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
import android.view.KeyEvent;
import android.view.Window;

class MyDialog extends Dialog
                      // implements View.OnClickListener
{
  protected Context mContext;
  private String mHelpPage = null;

  MyDialog( Context context, int help_resource )
  {
    super( context );
    mContext = context;
    if ( help_resource != 0 ) {
      mHelpPage = mContext.getResources().getString( help_resource );
    }
  }

  // utility method for derived classes
  protected void initLayout( int layout_resource, int title_resource )
  {
    if ( title_resource == -1 ) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    setContentView( layout_resource );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );
    if ( title_resource != -1 ) {
      setTitle( title_resource );
    }
  }

  protected void initLayout( int layout_resource, String title )
  {
    if ( title == null ) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    setContentView( layout_resource );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );
    if ( title != null ) {
      setTitle( title );
    }
  }

  protected void initLayout( View v, int title_resource )
  {
    if ( title_resource == -1 ) {
      requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    setContentView( v );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );
    if ( title_resource != -1 ) {
      setTitle( title_resource );
    }
  }

  @Override 
  // public boolean onKeyLongPress( int code, KeyEvent ev )
  public boolean onKeyDown( int code, KeyEvent ev )
  {
    if ( code == KeyEvent.KEYCODE_BACK ) {
      onBackPressed();
      return true;
    } else if ( code == KeyEvent.KEYCODE_MENU ) {
      if ( mHelpPage != null ) {
        UserManualActivity.showHelpPage( mContext, mHelpPage );
      }
      return true;
    }
    return false;
  }

}


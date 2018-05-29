/** @file MyKeyboard.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid numerical keyboard dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;

// import android.app.Dialog;

import android.widget.EditText;

import android.content.Context;
// import android.os.Bundle;

import android.view.View;
import android.view.View.OnKeyListener;
// import android.view.View.OnClickListener; 
import android.view.View.OnFocusChangeListener; 
// import android.view.View.OnTouchListener; 
// import android.view.Gravity; 
// import android.view.Window; 
// import android.view.WindowManager; 
import android.view.inputmethod.InputMethodManager; 
import android.view.KeyEvent;
// import android.view.MotionEvent;
// import android.view.ViewGroup;

import android.text.InputType;
import android.text.Editable;
// import android.text.Layout;
import android.text.method.KeyListener; 

// import android.util.Log;

/* you need to override EditText::onTouchListener()
 */
class MyKeyboard // FIXME DIALOG extends Dialog
                        implements OnKeyListener
                                 , OnKeyboardActionListener 
{
  private static final String TAG = "DistoX0";

  static final int FLAG_SIGN   = 0x01;
  static final int FLAG_POINT  = 0x02;
  static final int FLAG_DEGREE = 0x04;
  static final int FLAG_LCASE  = 0x08;
  static final int FLAG_NOEDIT = 0x10;
  static final int FLAG_2ND    = 0x20;
  static final int FLAG_POINT_SIGN        = 0x03; // FLAG_POINT | FLAG_SIGN
  static final int FLAG_POINT_DEGREE      = 0x06; // FLAG_POINT | FLAG_DEGREE
  static final int FLAG_POINT_SIGN_DEGREE = 0x07; // FLAG_POINT | FLAG_SIGN | FLAG_DEGREE
  static final int FLAG_POINT_LCASE       = 0x0a; // FLAG_POINT | FLAG_LCASE
  static final int FLAG_POINT_LCASE_2ND   = 0x2a; // FLAG_POINT | FLAG_LCASE | FLAG_2ND

  private boolean hasDegree;
  private boolean hasPoint;
  private boolean hasSign;
  private boolean hasLcase;

  private boolean inLcase;

  private Map< EditText, Integer > mFlags;

  private Context  mContext;
  private EditText mEdit; 
  private KeyboardView mKeyboardView;
  private Keyboard mKeyboard;
  private Keyboard mKeyboard1;
  private Keyboard mKeyboard2; // secondary kbd

  EditText getEditText() { return mEdit; }
  Context  getContext() { return mContext; }

  // Keyboard.Key mKeySign;
  // Keyboard.Key mKeyDegree;
  // Keyboard.Key mKeyMinute;
  // Keyboard.Key mKeyPoint;

  private int setFlags( EditText e )
  { 
    Integer j = mFlags.get( e );
    int flag = ( j == null )? 0 : j.intValue();
    hasSign   = ( ( flag & FLAG_SIGN ) == FLAG_SIGN );
    hasPoint  = ( ( flag & FLAG_POINT ) == FLAG_POINT );
    hasDegree = ( ( flag & FLAG_DEGREE ) == FLAG_DEGREE );
    hasLcase  = ( ( flag & FLAG_LCASE ) == FLAG_LCASE );
    return flag;
  }
    
  private Integer addFlag( EditText e, int f )
  {
    return mFlags.put( e, Integer.valueOf(f) );
  }

  static void registerEditText( final MyKeyboard kbd, final EditText e, int flag )
  { 
    if ( kbd == null ) return;
    if ( kbd.addFlag( e, flag ) == null ) {
      // Log.v("DistoX", "set listeners for " + e.getText().toString() + " flag " + flag );

      if ( ( flag & FLAG_NOEDIT ) == FLAG_NOEDIT ) {
        // e.setBackgroundColor( TDColor.MID_GRAY );
        e.setTextColor( TDColor.BLACK );
	e.setFocusable( false );
        // e.setBackgroundResource( R.drawable.edit_text );
      // } else {
        // e.setBackgroundResource( android.R.drawable.edit_text );
      }

      e.setOnFocusChangeListener( new OnFocusChangeListener() {
        @Override
        public void onFocusChange( View v, boolean hasFocus ) {
          CutNPaste.dismissPopup();
          // Log.v(TAG, "onFocusChange() " + hasFocus + " " + e.getText().toString() );
          if ( hasFocus ) {
            InputMethodManager imm = (InputMethodManager)kbd.getContext().getSystemService( Context.INPUT_METHOD_SERVICE );
            imm.hideSoftInputFromWindow( e.getWindowToken(), 0 );
            if ( kbd.setEditText( e ) ) {
              // e.setBackgroundResource( R.drawable.textfield_selected );
              kbd.show( e );
              // kbd.getWindow().makeActive();
            } else {
              // e.setBackgroundColor( TDColor.MID_GRAY );
              kbd.setEditText( null );
            }
          } else {
	    EditText et = (EditText)v;
	    if ( et != null ) clearCursor( et );
            // e.setBackgroundResource( android.R.drawable.edit_text );
            kbd.setEditText( null );
          }
        }
      } );

      e.setOnClickListener( new View.OnClickListener() {
        @Override
        public void onClick( View v ) {
          CutNPaste.dismissPopup();
          // Log.v(TAG, "on click " + e.getText().toString() );
          // EditText et = (EditText) v; 
          // EditText e0 = kbd.getEditText();
          // if ( e0 != null ) {
          //   e0.setBackgroundResource( android.R.drawable.edit_text );
          // }
          e.setInputType( InputType.TYPE_NULL );
          if ( kbd.setEditText( e ) ) {
            // e.setBackgroundResource( android.R.drawable.edit_text );
            // e.setBackgroundResource( R.drawable.textfield_selected );
            kbd.show( e );
          } else {
            // e.setBackgroundColor( TDColor.MID_GRAY );
            kbd.setEditText( null );
          }
        }
      } );

      e.setInputType( InputType.TYPE_NULL );
      e.setCursorVisible( true );
    }

    // if you need the cursor in the EditText use this 
    // e.setOnTouchListener( new OnTouchListener() {
    //   @Override
    //   public boolean onTouch( View v, MotionEvent ev ) {
    //     EditText et = (EditText)v;
    //     int t = et.getInputType();
    //     et.setInputType( InputType.TYPE_NULL );
    //     et.onTouchEvent( ev );
    //     et.setInputType( t );
    //     return true;
    //   }
    // }

    // FIXME getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN );
    // FIXME
    // @Override public void onBackPressed() {
    //   if ( kbd != null ) {
    //     kbd.hide();
    //     kbd.dismiss();
    //     kbd = null;
    //   }
    // }    
  }

  void hide()
  {
    mKeyboardView.setVisibility( View.GONE );
    mKeyboardView.setEnabled( false );
  }

  private void show( View v )
  {
    mKeyboardView.setVisibility( View.VISIBLE );
    mKeyboardView.setEnabled( true );
    // if ( v != null ) {
    // }
  }

  private void switchKeyboard( int flag )
  {
    if ( mKeyboard2 == null ) return;
    Keyboard next = ( (flag & FLAG_2ND) == FLAG_2ND )? mKeyboard2 : mKeyboard1;
    if ( next == mKeyboard ) return;
    boolean visible = mKeyboardView.isShown();
    if ( visible ) hide();
    mKeyboard = next;
    mKeyboardView.setKeyboard(mKeyboard);
    if ( ( flag & FLAG_LCASE ) == FLAG_LCASE ) {
      setKeyLabels( false );
    }
    if ( visible ) show( null );
  }

  boolean isVisible()
  {
    return mKeyboardView.isShown();
  }

  MyKeyboard( Context context, KeyboardView view, int kbdid1, int kbdid2 )
  {
    // FIXME DIALOG super( context );
    mContext = context;
    mEdit = null;
    mFlags = new HashMap< EditText, Integer >();

    // Log.v("DistoX", "id1 " + kbdid1 + " " + kbdid2 );
    mKeyboardView = view;
    mKeyboard1 = new Keyboard( mContext, kbdid1 );
    mKeyboard2 = ( kbdid2 == -1 )? null : new Keyboard( mContext, kbdid2 );
    mKeyboard  = mKeyboard1;
    mKeyboardView.setKeyboard(mKeyboard);
    mKeyboardView.setEnabled(true);
    mKeyboardView.setOnKeyListener(this);
    mKeyboardView.setOnKeyboardActionListener(this);
    mKeyboardView.setPreviewEnabled( false );
  }
 
  private boolean setEditText( EditText e )
  {
    if ( mEdit == e ) return true;
    mEdit = e;
    if ( mEdit != null ) {
      int flag = setFlags( mEdit );
      if ( ( flag & FLAG_NOEDIT ) == FLAG_NOEDIT ) {
        mEdit = null;
        return false;
      }
      switchKeyboard( flag );
      setCursor( e );
      return true;
    } else {
      hide();
    }
    return false;
  }

  static private void setCursor( EditText e )
  {
    if ( e == null || TDSetting.mNoCursor ) return;
    Editable cs = e.getText();
    int len = cs.length();
    if ( len == 0 || cs.charAt(len-1) != CHAR_CURSOR ) {
      cs.append( CHAR_CURSOR );
    }
  }

  private void clearCursor( ) { clearCursor( mEdit ); }

  static private void clearCursor( EditText e )
  {
    if ( e == null || TDSetting.mNoCursor ) return;
    Editable cs = e.getText();
    int len = cs.length();
    if ( len > 0 && cs.charAt(len-1) == CHAR_CURSOR ) {
      cs.delete( len-1, len );
    }
  }


  @Override
  public boolean onKey(View v, int keyCode, KeyEvent event)
  {
    // Log.v( TAG, "[*] Keycode " + keyCode );
    return false;
  }

  final static private char CHAR_MINUS      = (char)'-';
  final static char CHAR_PLUS_MINUS = (char)177;
  final static private char CHAR_DEGREE     = (char)176;
  final static private char CHAR_MINUTE     = (char)39;
  final static private char CHAR_POINT      = (char)46;
  final static private char CHAR_CURSOR     = (char)95; // 95 underscore, 124 vert bar, 63 question mark 166 broken vert bar
  final static private String STR_DEGREE  = Character.toString( CHAR_DEGREE );
  final static private String STR_MINUTE  = Character.toString( CHAR_MINUTE );
  final static private String STR_POINT   = Character.toString( CHAR_POINT  );


  @Override
  public void onKey(int keyCode, int[] keyCodes) 
  {
    if ( keyCode == -4 /* Keyboard.KEYCODE_CANCEL */ 
      || keyCode == 4 /* hw KEYCODE_BACK */ ) {
      // Log.v( TAG, "Keycode " + keyCode + " CANCEL");
      hide();
      return;
    } else if ( keyCode == 256 ) {
      hide();
      if ( mEdit != null ) {
        // cannot use FOCUS_FORWARD
        View next = mEdit.focusSearch( View.FOCUS_RIGHT );
        if ( next != null ) next.requestFocus();
      }
    } else if ( mEdit != null ) {
      Editable editable = mEdit.getText() ; 
      int len = editable.length();
      if ( len > 0 && editable.charAt(len-1) == CHAR_CURSOR ) --len;
      if ( keyCode == -5 /* Keyboard.KEYCODE_DELETE */ ) {
        // Log.v( TAG, "Keycode " + keyCode + " DELETE len " + len + " text " + editable.toString());
        if ( len > 0 ) {
          editable.delete( len-1, len );
        }
      } else if ( keyCode == 177 ) { // +/-
        if ( hasSign ) {
          if ( len > 0 && editable.charAt(0) == CHAR_MINUS ) {
            editable.delete(0,1);
          } else {
            editable.insert(0, "-" );
          }
        }
      } else if ( keyCode == 257 ) { // Aa
        if ( hasLcase ) {
          setKeyLabels( ! inLcase );
        }  
      } else if ( keyCode == 176 ) { // degree
        if ( hasDegree && len > 0 ) {
          boolean ok = true;
          for ( int k=0; k<len; ++k) {
            char ch = editable.charAt(k);
            if ( ch == CHAR_MINUTE || ch == CHAR_POINT || ch == CHAR_DEGREE ) { ok = false; break; }
          }
          // if ( ok ) editable.append( CHAR_DEGREE );
          if ( ok ) editable.insert( len, STR_DEGREE );
        }
      } else if ( keyCode == 39 ) { // minute
        if ( hasDegree && len > 0 ) {
          boolean ok = true;
          for ( int k=0; k<len; ++k) {
            char ch = editable.charAt(k);
            if ( ch == CHAR_MINUTE || ch == CHAR_POINT ) { ok = false; break; }
          }
          // if ( ok ) editable.append( CHAR_MINUTE );
          if ( ok ) editable.insert( len, STR_MINUTE );
        }
      } else if ( keyCode == 46 ) { // point
        if ( hasPoint ) {
          boolean ok = true;
          for ( int k=0; k<len; ++k) {
            char ch = editable.charAt(k);
            if ( ch == CHAR_POINT ) { ok = false; break; }
          }
          // if ( ok ) editable.append( CHAR_POINT );
          if ( ok ) editable.insert( len, STR_POINT );
        }
      } else {
        // Log.v( TAG, "Keycode " + keyCode + " APPEND text " + editable.toString());
        char ch = (char) keyCode;
        if ( inLcase ) {
          if ( ch >= 65 && ch <= 90 ) {
            ch += 32;
          }
        }
        // editable.append( Character.toString(ch) ); 
        editable.insert( len, Character.toString(ch) ); 
      }
      // editable.cursorAt( editable.length() );
      // setTitle( editable.toString() );
    // } else {
    //   // Log.d( TAG, "Keycode " + keyCode );
    }
  }

  private void setKeyLabels( boolean lcase )
  {
   inLcase = lcase;
    List< Keyboard.Key > keys = mKeyboard.getKeys();
    for ( Keyboard.Key key : keys ) {
      int c = key.codes[0];
      int off = inLcase ? 32 : 0;
      if ( c >= 65 && c <= 90 ) {
        key.label = Character.toString( (char) (c+off) );
      }
    }
    mKeyboardView.invalidateAllKeys();
  }

  // -----------------------------------------------------------
  // KeyListener methods

  // @Override
  // public int getInputType() 
  // {
  //   return InputType.TYPE_NULL;
  // }

  // @Override
  // public boolean onKeyDown( View view, Editable text, int keyCode, KeyEvent event)
  // {
  //   Log.d(TAG, "onKeyDown keyCode=" + keyCode);
  //   return true; // key down handled
  // }

  // @Override
  // public boolean onKeyOther(View view, Editable text, KeyEvent event)
  // {
  //   Log.d(TAG, "onKeyOther keyCode=" + keyCode);
  //   return true; // key other handled
  // }

  // @Override
  // public boolean onKeyUp(View view, Editable text, int keyCode, KeyEvent event)
  // {
  //   Log.d(TAG, "onKeyUp keyCode=" + keyCode);
  //   return true; // key up handled
  // }

  // @Override
  // public void clearMetaKeyState(View arg0, Editable arg1, int arg2) { 
  // }
    

  // OnKeyboardActionListener methods

  @Override
  public void swipeUp()
  {
    // Log.d(TAG, "swipeUp");
  }

  @Override
  public void swipeRight() 
  {
    // Log.d(TAG, "swipeRight");
  }

  @Override
  public void swipeLeft() 
  {
    // Log.d(TAG, "swipeLeft");
  }

  @Override
  public void swipeDown() 
  {
    // Log.d(TAG, "swipeDown");
  }

  @Override
  public void onText(CharSequence text) 
  {
    // Log.d(TAG, "onText? \"" + text + "\"");
  }

  @Override
  public void onRelease(int primaryCode) 
  {
    // Log.d(TAG, "onRelease? primaryCode=" + primaryCode);
  }

  @Override
  public void onPress(int primaryCode) 
  {
    // Log.d(TAG, "onPress? primaryCode=" + primaryCode);
  }

  static void setEditable( EditText et, MyKeyboard kbd, KeyListener kl, boolean editable, int flag )
  {
    if ( TDSetting.mKeyboard ) {
      et.setKeyListener( null );
      et.setClickable( true );
      et.setFocusable( editable );
      if ( editable ) {
        registerEditText( kbd, et, flag );
        // et.setKeyListener( mKeyboard );
        // et.setBackgroundResource( android.R.drawable.edit_text );
        // et.setBackgroundResource( R.color.bg );
	// et.setTextColor( R.color.text );
      } else {
        registerEditText( kbd, et, flag | FLAG_NOEDIT );
        et.setBackgroundColor( TDColor.MID_GRAY );
      }
    } else {
      if ( editable ) {
        et.setKeyListener( kl );
        // et.setBackgroundResource( android.R.drawable.edit_text );
        et.setClickable( true );
        et.setFocusable( true );
      } else {
        // et.setFocusable( false );
        // et.setClickable( false );
        et.setKeyListener( null );
        et.setBackgroundColor( TDColor.MID_GRAY );
      }
    }
  }

  static boolean close( MyKeyboard kbd )
  {
    if ( kbd == null ) return false;
    kbd.clearCursor();
    if ( kbd.isVisible() ) {
      kbd.hide();
      return true;
    }
    return false;
  }

}


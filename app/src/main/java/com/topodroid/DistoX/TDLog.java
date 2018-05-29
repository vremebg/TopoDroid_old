/** @file TDLog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid application (consts and prefs)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
// import java.io.FileNotFoundException;

import android.content.SharedPreferences;

import android.util.Log;

class TDLog
{

  // ---------------------------------------------------------
  // DEBUG: logcat flags

  static final String TAG = "DistoX";

  static private int mLogStream = 0;    // log stream
  static private PrintWriter mLog = null;
  static private long mMillis;

  static boolean LOG_BEZIER = false;
  static boolean LOG_BT     = false;   // bluetooth
  static boolean LOG_CALIB  = false;
  static boolean LOG_COMM   = false;   // connection
  static boolean LOG_CSURVEY = false;
  static boolean LOG_DATA   = false;   // shot data
  static boolean LOG_DB     = false;   // sqlite database
  static boolean LOG_DEBUG  = false;
  static boolean LOG_DEVICE = false;
  static boolean LOG_DISTOX = false;   // DistoX packets
  static boolean LOG_ERR    = true;
  static boolean LOG_FIXED  = false;
  static boolean LOG_INPUT  = false;   // user input
  static boolean LOG_LOC    = false;   // location manager
  static boolean LOG_NOTE   = false;   // annotation
  static boolean LOG_MAIN   = false;   // main app
  static boolean LOG_NAME   = false;   // names
  static boolean LOG_NUM    = false;  
  static boolean LOG_PATH   = false;
  static boolean LOG_PLOT   = false;
  static boolean LOG_PHOTO  = false;   // photos
  static boolean LOG_PREFS  = false;   // preferences
  static boolean LOG_PROTO  = false;   // protocol
  static boolean LOG_PTOPO  = false;   // PocketTopo
  static boolean LOG_SENSOR = false;   // sensors and measures
  static boolean LOG_SHOT   = false;   // shot
  static boolean LOG_STATS  = false;
  static boolean LOG_SURVEY = false;
  static boolean LOG_THERION= false;
  static boolean LOG_ZIP    = false;   // archive
  static boolean LOG_UNITS  = false;
  static boolean LOG_SYNC   = false;
  static boolean LOG_PERM   = false;


  // --------------- LOG PREFERENCES ----------------------
  static final private String[] log_key = {
    "DISTOX_LOG_DEBUG",           // + 0
    "DISTOX_LOG_ERR",
    "DISTOX_LOG_INPUT",           // + 2
    "DISTOX_LOG_BT",              // + 3
    "DISTOX_LOG_COMM",
    "DISTOX_LOG_PROTO",
    "DISTOX_LOG_DISTOX",
    "DISTOX_LOG_DEVICE",          // + 7
    "DISTOX_LOG_DATA",
    "DISTOX_LOG_DB",              // + 9
    "DISTOX_LOG_CALIB",
    "DISTOX_LOG_FIXED",
    "DISTOX_LOG_LOC",             // +12
    "DISTOX_LOG_PHOTO",
    "DISTOX_LOG_SENSOR",          // +14
    "DISTOX_LOG_SHOT",
    "DISTOX_LOG_SURVEY",
    "DISTOX_LOG_NUM",             // +17
    "DISTOX_LOG_THERION",
    "DISTOX_LOG_PATH",
    "DISTOX_LOG_PLOT",            // +19
    "DISTOX_LOG_BEZIER",
    "DISTOX_LOG_CSURVEY",         // +21
    "DISTOX_LOG_PTOPO",           // +22
    "DISTOX_LOG_ZIP",
    "DISTOX_LOG_UNITS",
    "DISTOX_LOG_SYNC",
    "DISTOX_LOG_PERM"             // permissions
  };

  // static void Profile( String msg )
  // {
  //   mMillis = System.currentTimeMillis() % 600000;
  //   Log.v("DistoX", Long.toString(mMillis) + " PROFILE " + msg );
  // }

  static void TimeStart() { mMillis = System.currentTimeMillis(); }

  static void TimeEnd( String msg ) 
  {
    long millis = System.currentTimeMillis() - mMillis;
    Log.v( TAG, msg + " " + millis );
  }
  
  static void LogFile( String msg )
  {
    mMillis = System.currentTimeMillis() % 600000;
    mLog.format( "%d: %s\n", mMillis, msg );
  }

  static void Debug( String msg )
  {
    if ( LOG_DEBUG && msg != null ) {
      mMillis = System.currentTimeMillis() % 600000;
      if ( mLogStream == 0 || mLog == null ) {
        Log.v( TAG, mMillis + " " + msg );
      } else {
        mLog.format( "%d: %s\n", mMillis, msg );
        // mLog.flush(); // autoflush ?
      }
    }
  }

  static void Error( String msg )
  {
    if ( LOG_ERR && msg != null ) {
      mMillis = System.currentTimeMillis() % 600000;
      if ( mLogStream == 0 || mLog == null ) {
        Log.v( TAG, mMillis + " " + msg );
      } else {
        mLog.format( "%d: %s\n", mMillis, msg );
        // mLog.flush(); // autoflush ?
      }
    }
  }

  static void Log( boolean flag, String msg )
  {
    if ( flag && msg != null ) {
      mMillis = System.currentTimeMillis() % 600000;
      if ( mLogStream == 0 || mLog == null ) {
        Log.v( TAG, mMillis + " " + msg );
      } else {
        mLog.format( "%d: %s\n", mMillis, msg );
        // mLog.flush(); // autoflush ?
      }
    }
  }

  static void LogStackTrace( Exception e )
  {
    StackTraceElement[] trace = e.getStackTrace();
    if ( trace == null ) return;
    if ( mLogStream == 0 || mLog == null ) {
      for ( StackTraceElement st : trace ) Log.v( TAG, st.toString() );
    } else {
      for ( StackTraceElement st : trace ) mLog.format( "%s\n", st.toString() );
    }
  }

  static void setLogTarget()
  {
    if ( mLog == null ) {
      try {
        File log_file = TDPath.getLogFile();
        FileWriter fw = new FileWriter( log_file );
        mLog = new PrintWriter( fw, true ); // true = autoflush
        mLog.format( "TopoDroid version %s\n", TopoDroidApp.VERSION );
      } catch ( IOException e ) {
        Log.e("DistoX", "cannot create log file" );
      }
    }
  }
  
  static void loadLogPreferences( SharedPreferences prefs )
  {
    int lk = 0;
    
    mLogStream  = Integer.parseInt( prefs.getString("DISTOX_LOG_STREAM", "0") );
    
    LOG_DEBUG   = prefs.getBoolean( log_key[lk++], false );
    LOG_ERR     = prefs.getBoolean( log_key[lk++], true );
    LOG_INPUT   = prefs.getBoolean( log_key[lk++], false );
    LOG_BT      = prefs.getBoolean( log_key[lk++], false );
    LOG_COMM    = prefs.getBoolean( log_key[lk++], false );
    LOG_PROTO   = prefs.getBoolean( log_key[lk++], false );
    LOG_DISTOX  = prefs.getBoolean( log_key[lk++], false );
    LOG_DEVICE  = prefs.getBoolean( log_key[lk++], false );
    LOG_DATA    = prefs.getBoolean( log_key[lk++], false );
    LOG_DB      = prefs.getBoolean( log_key[lk++], false );
    LOG_CALIB   = prefs.getBoolean( log_key[lk++], false );
    LOG_FIXED   = prefs.getBoolean( log_key[lk++], false );
    LOG_LOC     = prefs.getBoolean( log_key[lk++], false );
    LOG_PHOTO   = prefs.getBoolean( log_key[lk++], false );
    LOG_SENSOR  = prefs.getBoolean( log_key[lk++], false );
    LOG_SHOT    = prefs.getBoolean( log_key[lk++], false );
    LOG_SURVEY  = prefs.getBoolean( log_key[lk++], false );
    LOG_NUM     = prefs.getBoolean( log_key[lk++], false );
    LOG_THERION = prefs.getBoolean( log_key[lk++], false );
    LOG_PATH    = prefs.getBoolean( log_key[lk++], false );
    LOG_PLOT    = prefs.getBoolean( log_key[lk++], false );
    LOG_BEZIER  = prefs.getBoolean( log_key[lk++], false );
    LOG_CSURVEY = prefs.getBoolean( log_key[lk++], false );
    LOG_PTOPO   = prefs.getBoolean( log_key[lk++], false );
    LOG_ZIP     = prefs.getBoolean( log_key[lk++], false );
    LOG_UNITS   = prefs.getBoolean( log_key[lk++], false );
    LOG_SYNC    = prefs.getBoolean( log_key[lk++], false );
    LOG_PERM    = prefs.getBoolean( log_key[lk++], false );
  }
    
  static void checkLogPreferences( SharedPreferences sp, String k ) 
  {
    int lk = 0;
    if ( k.equals( "DISTOX_LOG_STREAM" ) ) { // "DISTOX_LOG_STREAM",
      mLogStream = Integer.parseInt( sp.getString(k, "0") );

    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_DEBUG",
      LOG_DEBUG = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_ERR",
      LOG_ERR = sp.getBoolean( k, true );
    } else if ( k.equals( log_key[ lk++ ] )) { // "DISTOX_LOG_INPUT",        
      LOG_INPUT = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_BT",
      LOG_BT = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_COMM",
      LOG_COMM = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_PROTO",
      LOG_PROTO = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_DISTOX",
      LOG_DISTOX = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_DEVICE",       // 40
      LOG_DEVICE = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_DATA",
      LOG_DATA = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_DB",
      LOG_DB = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_CALIB",
      LOG_CALIB = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_FIXED",
      LOG_FIXED = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_LOC",          // 45
      LOG_LOC = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_PHOTO",
      LOG_PHOTO = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_SENSOR"        // 47
      LOG_SENSOR = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_SHOT"        
      LOG_SHOT = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_SURVEY"
      LOG_SURVEY = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_NUM"
      LOG_NUM = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_THERION"
      LOG_THERION = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_PATH"
      LOG_PATH = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_PLOT"
      LOG_PLOT = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_BEZIER"
      LOG_BEZIER = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_CSURVEY"
      LOG_CSURVEY = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_PTOPO"
      LOG_PTOPO = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_ZIP"
      LOG_ZIP = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_UNITS"
      LOG_UNITS = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_SYNC"
      LOG_SYNC = sp.getBoolean( k, false );
    } else if ( k.equals( log_key[ lk++ ] ) ) { // "DISTOX_LOG_PERM"
      LOG_PERM = sp.getBoolean( k, false );
    } 
  }

}

/** @file TDLevel.java
 *
 * @author marco corvi
 * @date jul 2017
 *
 * @brief TopoDroid activity level(s)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.content.Context;
import android.provider.Settings.Secure;

class TDLevel
{
  static final int BASIC    = 0;
  static final int NORMAL   = 1;
  static final int ADVANCED = 2;
  static final int EXPERT   = 3;
  static final int TESTER   = 4;
  static final int COMPLETE = 5;

  static int mLevel = 1; // activity level

  static boolean overBasic    = true;
  static boolean overNormal   = false;
  static boolean overAdvanced = false;
  static boolean overExpert   = false;
  static boolean overTester   = false;


  static void setLevel( Context ctx, int level )
  {
    mLevel = level;
    overBasic    = mLevel > BASIC;
    overNormal   = mLevel > NORMAL;
    overAdvanced = mLevel > ADVANCED;
    overExpert   = mLevel > EXPERT;
    // overTester  = mLevel > TESTER;
    if ( overExpert ) {
      String android_id = Secure.getString( ctx.getContentResolver(), Secure.ANDROID_ID );
      // Log.v("DistoX", "android_id <" + android_id + ">");
      if ( // "e5582eda21cafac3".equals( android_id ) || // Nexus-4
           "8c894b79b6dce351".equals( android_id ) ) {   // Samsung Note-3
        overTester = true;
      }
    }
  }
}

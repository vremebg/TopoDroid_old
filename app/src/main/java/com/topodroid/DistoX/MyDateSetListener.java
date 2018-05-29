/* @file MyDateSetListener.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid date setter
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.widget.TextView;
import android.widget.DatePicker;
import android.app.DatePickerDialog.OnDateSetListener;

class MyDateSetListener implements OnDateSetListener 
{
  private TextView mView;

  MyDateSetListener( TextView v )
  {
    mView = v;
  }

  @Override
  public void onDateSet( DatePicker view, int y, int m, int d ) {
    mView.setText( TopoDroidUtil.composeDate( y, m, d ) );
  }
}

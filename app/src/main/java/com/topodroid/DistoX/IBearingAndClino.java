/** @file IBearingAndClino.java
 *
 * @author marco corvi 
 * @date nov 2013
 *
 * @brief TopoDroid bearing and clino interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

interface IBearingAndClino
{
  // @param b0 bearing
  // @param c0 clino
  // @param o0 orienatation
  public void setBearingAndClino( float b0, float c0, int o0 );

  public void setJpegData( byte[] data );

}

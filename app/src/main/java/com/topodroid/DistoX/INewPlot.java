/* @file INewPlot.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid NewPlot interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

public interface INewPlot 
{
  // public void makeNewPlot( String name, long type, String start, String view );
  public void makeNewPlot( String name, String start, boolean extended, int project );

  // FIXME_SKETCH_3D
  public void makeNewSketch3d(  String name, String start, String next );

  public boolean hasSurveyPlot( String name );

  public boolean hasSurveyStation( String start );

  public void doProjectionDialog( String name, String start );
}



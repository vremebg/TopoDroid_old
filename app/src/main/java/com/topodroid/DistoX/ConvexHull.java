/** @file ConvexHull.java
 *
 * @author marco corvi
 * @date nov 2014
 *
 * @brief TopoDroid main 3d convex hull
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.Math;
import java.util.ArrayList;
// import java.util.Iterator;

// import android.util.Log;

class ConvexHull
{
  // private Vector mV1;  // first base point
  // private Vector mV2;  // second base point

  // private Vector mX;   // X unit vector (mV2-mV1).normalized()
  // private Vector mY;
  // private Vector mZ;

  private ArrayList< Vector > mPts; // other points
  ArrayList< CHTriangle > mTri;

  private class VectorPair
  {
    boolean valid;
    Vector mV1;
    Vector mV2;
   
    VectorPair( Vector v1, Vector v2 ) 
    {
      valid = true;
      mV1 = v1;
      mV2 = v2;
    }
  }

  ConvexHull( Vector mV1, Vector mV2, ArrayList<Vector> pts )
  {
    // mV1 = v1;
    // mV2 = v2;
    mPts = pts;

    Vector mZ;
    Vector mX = mV2.minus( mV1 );
    mX.normalize(); 
    if ( Math.abs( mX.z ) > Math.abs( mX.y ) ) {
      if ( Math.abs( mX.y ) > Math.abs( mX.x ) ) {
        mZ = new Vector( 1, 0, 0 );
      } else {
        mZ = new Vector( 0, 1, 0 );
      }
    } else {
      if ( Math.abs( mX.z ) > Math.abs( mX.x ) ) {
        mZ = new Vector( 1, 0, 0 );
      } else {
        mZ = new Vector( 0, 0, 1 ); 
      }
    }
    Vector mY = mX.cross( mZ );
    mY.normalize();
    mZ = mX.cross( mY ); 
    mZ.normalize(); // not really needed: should be normalized

    // first two triangles mV1-mV2-pmax mV2-mV1-p[0] 
    // use temporary triangle array
    ArrayList<CHTriangle> tri = new ArrayList<>();
    Vector p = (Vector) mPts.get(0);
    tri.add( new CHTriangle( mV1, mV2, p ) );
    tri.add( new CHTriangle( mV2, mV1, p ) );

    ArrayList< VectorPair > vp_to_add = new ArrayList<>();
    for ( int h = 1; h<mPts.size(); ++h ) {
      p = (Vector) mPts.get(h);
     
      vp_to_add.clear();
      for ( CHTriangle t : tri ) {
        if ( ! t.valid ) continue;
        if ( t.signedDistance( p ) > 0 ) {
          vp_to_add.add( new VectorPair(t.mV[0], t.mV[1] ) );
          vp_to_add.add( new VectorPair(t.mV[1], t.mV[2] ) );
          vp_to_add.add( new VectorPair(t.mV[2], t.mV[0] ) );
          t.valid = false;
        }
      }
      // mark mirror vector-pairs invalid
      for ( int n1 = 0; n1 < vp_to_add.size(); ++n1 ) {
        VectorPair vp1 = (VectorPair) vp_to_add.get( n1 );
        for ( int n2 = n1+1; n2 < vp_to_add.size(); ++n2 ) {
          VectorPair vp2 = (VectorPair) vp_to_add.get( n2 );
          if ( vp1.mV1 == vp2.mV2 && vp1.mV2 == vp2.mV1 ) {
            vp1.valid = false;
            vp2.valid = false;
            break;
          }
        }
      }
      for ( VectorPair vp : vp_to_add ) { // ***** add new triangles
        if ( vp.valid ) {
          tri.add( new CHTriangle( vp.mV1, vp.mV2, p ) );
        }
      }
    }
    mTri = new ArrayList<>(); // definitive triangle array list
    for ( CHTriangle t : tri ) {
      if ( t.valid ) mTri.add( t ) ;
    }
  }

}

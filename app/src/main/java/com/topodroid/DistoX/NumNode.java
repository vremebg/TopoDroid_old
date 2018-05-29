/** @file NumNode.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid survey reduction node of the survey net
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

class NumNode
{
  static final int NODE_END = 0; // branch types
  static final int NODE_CROSS = 1;
 
  int type; // node type
  int use;  // tag for loop identification
  NumStation station; // station of this node
  ArrayList< NumShot > shots; // station shots
  private ArrayList< NumBranch > branches;
  float e, s, v; // east, south, vert 3D position

  NumNode( int t, NumStation st )
  {
    type = t;
    station = st;
    shots    = new ArrayList<>();
    shots.add( st.s1 ); // and set first two shots
    shots.add( st.s2 );
    branches = new ArrayList<>();
    use = 0;
    e = st.e;
    s = st.s;
    v = st.v;
  }

  void addBranch( NumBranch branch ) { branches.add( branch ); }

  void addShot( NumShot sh ) { shots.add( sh ); }

}


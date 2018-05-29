/* @file Matrix.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid 3x3 matrix
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * This software is adapted from TopoLinux implementation,
 * which, in turns, is based on PocketTopo implementation.
 */
package com.topodroid.DistoX;

class Matrix
{ 
  Vector x,y,z;
 
  static Matrix zero = new Matrix( Vector.zero, Vector.zero, Vector.zero );

  static Matrix one = new Matrix( new Vector(1.0f, 0.0f, 0.0f),
                                  new Vector(0.0f, 1.0f, 0.0f),
                                  new Vector(0.0f, 0.0f, 1.0f) );

  // Default cstr: zero matrix
  Matrix()
  {
    x = new Vector();
    y = new Vector();
    z = new Vector();
  }

  Matrix( Vector x0, Vector y0, Vector z0 )
  {
    x = new Vector(x0);
    y = new Vector(y0);
    z = new Vector(z0);
  }

  // OUTER PRODUCT: a & b
  Matrix( Vector a, Vector b )
  {
    x = b.times(a.x);
    y = b.times(a.y);
    z = b.times(a.z);
  }

  Matrix( Matrix a )
  {
    x = new Vector( a.x );
    y = new Vector( a.y );
    z = new Vector( a.z );
  }

  float maxAbsValue()
  {
    double mx = x.maxAbsValue();
    double my = y.maxAbsValue();
    double mz = z.maxAbsValue();
    return (float)( ( mx > my )? ( ( mx > mz )? mx : mz )
                               : ( ( my > mz )? my : mz ) );
  }

  void plusEqual( Matrix b )
  {
    x.plusEqual( b.x );
    y.plusEqual( b.y );
    z.plusEqual( b.z );
  }

  Matrix plus( Matrix b )
  {
    return new Matrix( x.plus(b.x), y.plus(b.y), z.plus(b.z) );
  }

  Matrix minus( Matrix b )
  {
    return new Matrix( x.minus(b.x), y.minus(b.y), z.minus(b.z) );
  }

  void timesEqual( float b )
  {
    x.timesEqual( b );
    y.timesEqual( b );
    z.timesEqual( b );
  }

  Matrix timesF( float b )
  {
    return new Matrix( x.times(b), y.times(b), z.times(b) );
  }

  Vector timesV( Vector b )
  {
    return new Vector( x.dot(b), y.dot(b), z.dot(b) );
  }

  // multiplication with the transposed: this * B^t
  Matrix timesT( Matrix b )
  {
    return new Matrix( b.timesV(x), b.timesV(y), b.timesV(z) );
  }

  Matrix timesM( Matrix b )
  {
    return this.timesT( b.Transposed() );
  }

  // inverse of the transposed: (this^t)^-1
  Matrix InverseT()
  {
    Matrix ad = new Matrix( y.cross(z), z.cross(x), x.cross(y) );
    float inv_det = 1.0f / ( x.dot( ad.x ) );
    ad.timesEqual( inv_det );
    return ad;
  }

  // inverse 
  Matrix InverseM()
  {
    Matrix at = this.Transposed();
    return at.InverseT();
  }

  Matrix Transposed()
  {
    Matrix ret = new Matrix();
    ret.x.x = x.x;
    ret.x.y = y.x;
    ret.x.z = z.x;
    ret.y.x = x.y;
    ret.y.y = y.y;
    ret.y.z = z.y;
    ret.z.x = x.z;
    ret.z.y = y.z;
    ret.z.z = z.z;
    return ret;
  }

  float MaxDiff( Matrix b )
  {
    float dx = x.MaxDiff( b.x );
    float dy = y.MaxDiff( b.y );
    float dz = z.MaxDiff( b.z );
    if ( dx < dy ) { dx = dy; }
    if ( dx < dz ) { dx = dz; }
    return dx;
  }

}

/** @file PhotoInfo.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo info (id, station, comment)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class PhotoInfo
{
  public long sid;       // survey id
  public long id;        // photo id
  public long shotid;    // shot id
  public String mTitle;   // photo title FIXME TITLE
  String mShotName; // shot name
  // public String mName; // photo filename without extension ".jpg" and survey prefix dir = photo id
  public String mDate;
  public String mComment;

  PhotoInfo( long _sid, long _id, long _shotid, String t, String sn, String dt, String cmt )
  {
    sid    = _sid;
    id     = _id;
    shotid = _shotid;
    mTitle  = t; // FIXME TITLE
    mShotName = sn;
    mDate = dt;
    mComment = cmt;
  }

  // String getPhotoName() 
  // {
  //   return String.format( "%d-%03d", sid, id );
  // }

  public String toString()
  {
    return id 
           + " <" + ( (mShotName == null)? "-" : mShotName )
           + "> " + mComment; 
  }

}

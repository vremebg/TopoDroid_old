/** @file AudioInfo.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid Audio info (id, sid, shot_id, date )
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class AudioInfo
{
  public long sid;       // survey id
  public long id;        // audio id
  public long shotid;    // shot id
  public String mDate;

  AudioInfo( long _sid, long _id, long _shotid, String dt )
  {
    sid    = _sid;
    id     = _id;
    shotid = _shotid;
    mDate = dt;
  }

  // String getAudioName() 
  // {
  //   return String.format( "%d-%03d", sid, id );
  // }

  public String toString()
  {
    return id + " <" + mDate  + "> ";
  }

}

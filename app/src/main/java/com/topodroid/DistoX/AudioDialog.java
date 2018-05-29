/* @file AudioDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid audio dialog to register a comment for a shot
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;

// import android.app.Dialog;
import android.os.Bundle;
// import android.os.Environment;

import android.content.Context;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.widget.LinearLayout;
import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.Window;
// import android.view.WindowManager;
// import android.view.ViewGroup.LayoutParams;

import android.widget.Button;
// import android.widget.Toast;

public class AudioDialog extends MyDialog
                         implements View.OnClickListener
{
  private static int ACTION_NONE = 0;
  private static int ACTION_DELETE = 1;
  private static int ACTION_OVERWRITE = 2;
  private int mAction = 0;

  private MediaPlayer   mMP;
  private MediaRecorder mMR;

  private MyStateBox mBtnPlay;
  private MyStateBox mBtnRec;
  private MyStateBox mBtnDelete;

  private Button mBtnConfirm;
  private Button mBtnClose;

  TopoDroidApp mApp;
  IAudioInserter mParent;
  private long mBid;
  private String mFilepath;
  private boolean hasFile;
  private boolean canRec;
  private boolean canPlay;
  // AudioInfo mAudio;

  AudioDialog( Context ctx, TopoDroidApp app, IAudioInserter parent, long bid )
  {
    super( ctx, R.string.AudioDialog );

    mApp = app;
    mParent = parent;
    mBid = bid;
    // mAudio = mApp.mData.getAudio( mApp.mSID, mBid );
    mFilepath = TDPath.getSurveyAudioFile( mApp.mySurvey, Long.toString(mBid) );
    File file = new File( mFilepath );
    hasFile = file.exists();
  }


  @Override
  protected void onCreate( Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.audio_dialog, R.string.title_audio );

    LinearLayout layout2 = (LinearLayout) findViewById( R.id.layout2 );
    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    layout2.setMinimumHeight( size + 40 );
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 
      LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp.setMargins( 10, 10, 20, 20 );

    // mBtnDelete = (Button) findViewById( R.id.audio_delete );
    mBtnClose = (Button) findViewById( R.id.audio_close );
    mBtnClose.setOnClickListener( this );

    mBtnRec    = new MyStateBox( mContext, R.drawable.iz_audio_rec, R.drawable.iz_audio_rec_on );
    mBtnPlay   = new MyStateBox( mContext, R.drawable.iz_audio_play_off, R.drawable.iz_audio_play, R.drawable.iz_audio_stop );
    mBtnDelete = new MyStateBox( mContext, R.drawable.iz_audio_delete, R.drawable.iz_audio_delete );
    // mBtnConfirm = new Button( mContext );
    mBtnConfirm = (Button) findViewById( R.id.audio_confirm );

    mBtnRec.setOnClickListener( this );
    mBtnPlay.setOnClickListener( this );
    mBtnDelete.setOnClickListener( this );
    mBtnConfirm.setOnClickListener( this );
    mBtnConfirm.setText( R.string.audio_paused );
    mAction = ACTION_NONE;

    canRec  = true;
    canPlay = hasFile;
    mBtnRec.setState( 0 );
    mBtnPlay.setState( hasFile ? 1 : 0 );

    layout2.addView( mBtnPlay, lp );
    layout2.addView( mBtnRec, lp );
    layout2.addView( mBtnDelete, lp );
    // layout2.addView( mBtnConfirm, lp );
    layout2.invalidate();
  }

  public void onClick(View v) 
  {
    try {
      MyStateBox b = (MyStateBox)v;
      if ( b == mBtnDelete ) {
        if ( hasFile ) { // delete audio file
          mAction = ACTION_DELETE;
          mBtnConfirm.setText(  R.string.audio_delete );
          return;
          // File file = new File( mFilepath );
          // file.delete();
          // mApp.mData.dropAudio( mApp.mSID, mBid );
        }
      } else if ( b == mBtnPlay ) {
        mAction = ACTION_NONE;
        mBtnConfirm.setText( R.string.audio_paused );
        if ( canPlay ) {
          int sp = mBtnPlay.getState();
          if ( sp == 2 ) {
            stopPlay();
          } else if ( sp == 1 ) {
            startPlay();
          }
        }
        return;
      } else if ( b == mBtnRec ) {
        if ( canRec ) {
          int sr = mBtnRec.getState();
          if ( sr == 1 ) {
            stopRec();
          } else if ( sr == 0 ) {
            if ( hasFile ) {
              mAction = ACTION_OVERWRITE;
              mBtnConfirm.setText( R.string.audio_overwrite );
            } else {
              startRec();
            }
          }
        }
        return;
      }
    } catch ( ClassCastException e ) { }
    if ( mAction > ACTION_NONE ) {
      try {
        if ( (Button)v == mBtnConfirm ) {
          if ( mAction == ACTION_DELETE ) {
            deleteAudio();
          } else if ( mAction == ACTION_OVERWRITE ) {
            startRec();
            return;
          }
        }
      } catch ( ClassCastException e ) { }
    }
    dismiss();
  }

  private void deleteAudio()
  {
    File file = new File( mFilepath );
    file.delete();
    TopoDroidApp.mData.deleteAudio( mApp.mSID, mBid );
    if ( mParent != null ) mParent.deletedAudio( mBid );
  }

  private void startRec()
  {
    try {
      if ( mParent != null ) mParent.startRecordAudio( mBid );
      mMR = new MediaRecorder();
      mMR.setAudioSource(MediaRecorder.AudioSource.MIC);
      mMR.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
      mMR.setAudioEncoder(MediaRecorder.OutputFormat.DEFAULT); // AMR_NB
      mMR.setOutputFile( mFilepath );
      mMR.prepare();
      canPlay = false;
      mBtnRec.setState( 1 );
      mAction = ACTION_NONE;
      mBtnConfirm.setText( R.string.audio_recording );
      mMR.start();
    } catch ( IllegalStateException e ) {
    } catch ( IOException e ) {
    }
  }

  private void stopRec()
  {
    try {
      mMR.stop();
      mMR.release();
      mMR = null;
      mBtnRec.setState( 0 );
      mBtnPlay.setState( 1 );
      mAction = ACTION_NONE;
      mBtnConfirm.setText( R.string.audio_paused );
      canPlay = true;
      hasFile = true;
      TopoDroidApp.mData.setAudio( mApp.mSID, mBid, TopoDroidUtil.currentDateTime() );
      if ( mParent != null ) mParent.stopRecordAudio( mBid );
    } catch ( IllegalStateException e ) {
    } catch ( RuntimeException e ) {
    }
  }

  private void startPlay()
  {
    try {
      mMP = new MediaPlayer();
      mMP.setDataSource( mFilepath );
      mMP.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion( MediaPlayer mp ) 
        { 
          mp.release();
          canRec = true;
          mBtnPlay.setState( 1 );
          mBtnConfirm.setText(  R.string.audio_paused );
        }
      } );
      mMP.prepare();
      canRec = false;
      mBtnPlay.setState( 2 );
      mAction = ACTION_NONE;
      mBtnConfirm.setText(  R.string.audio_playing );
      mMP.start();
    } catch ( IllegalStateException e ) {
    } catch ( IOException e ) {
    }
  }

  private void stopPlay()
  {
    try {
      canRec = true;
      if ( mMP != null ) {
        mMP.stop();
	mMP.release();
	mMP = null;
      }
      mAction = ACTION_NONE;
      mBtnConfirm.setText( R.string.audio_paused );
      mBtnPlay.setState( 1 );
    } catch ( IllegalStateException e ) {
    } catch ( RuntimeException e ) {
    }
  }

}


/* @file DistoXProtocol.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid TopoDroid-DistoX communication protocol
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;
// import java.io.FilterInputStream;
import java.io.File;
// import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.UUID;
import java.util.List;
// import java.util.Locale;
// import java.lang.reflect.Field;
import java.net.Socket;

// import android.os.CountDownTimer;

import java.nio.channels.ClosedByInterruptException;
// import java.nio.ByteBuffer;

// import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothServerSocket;
// import android.bluetooth.BluetoothSocket;

// import android.util.Log;

// import android.widget.Toast;

class DistoXProtocol
{
  private Device mDevice;
  // private DistoX mDistoX;
  // private BluetoothDevice  mBTDevice;
  // private BluetoothSocket  mSocket = null;
  private Socket  mSocket = null;
  private DataInputStream  mIn;
  private DataOutputStream mOut;
  // private byte[] mHeadTailA3;  // head/tail for Protocol A3
  private byte[] mAddr8000;
  private byte[] mAddress;   // request-reply address
  private byte[] mRequestBuffer;   // request buffer
  private byte[] mReplyBuffer;     // reply data
  private byte[] mAcknowledge;
  private byte   mSeqBit;          // sequence bit: 0x00 or 0x80

  private static final UUID MY_UUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );

  static final int DISTOX_PACKET_NONE   = 0;
  static final int DISTOX_PACKET_DATA   = 1;
  static final int DISTOX_PACKET_G      = 2;
  static final int DISTOX_PACKET_M      = 3;
  static final int DISTOX_PACKET_VECTOR = 4;
  static final int DISTOX_PACKET_REPLY  = 5;

  static final int DISTOX_ERR_HEADTAIL     = -1;
  static final int DISTOX_ERR_HEADTAIL_IO  = -2;
  static final int DISTOX_ERR_HEADTAIL_EOF = -3;
  static final int DISTOX_ERR_CONNECTED    = -4;
  static final int DISTOX_ERR_OFF          = -5; // distox has turned off

  double mDistance;
  double mBearing;
  double mClino;
  double mRoll;
  double mAcceleration;
  double mMagnetic;
  double mDip; // magnetic dip
  private byte mRollHigh; // high byte of roll
  long mGX, mGY, mGZ;
  long mMX, mMY, mMZ;

  final private byte[] mBuffer = new byte[8];
  int mMaxTimeout = 8;
  

  byte[] getAddress() { return mAddress; }
  byte[] getReply()   { return mReplyBuffer; }

  // FIXME the record of written calibration is not used
  // boolean writtenCalib = false;
  // public void setWrittenCalib( boolean b ) { writtenCalib = b; } 

//------------------------------------------------------

  /** 
   * @param timeout    joining timeout
   * @param maxtimeout max number of join attempts
   * @return number of data to read
   */
  private int getAvailable( long timeout, int maxtimeout ) throws IOException
  {
    mMaxTimeout = maxtimeout;
    final int[] dataRead = {0};
    final int[] toRead = {8};
    final int[] count = {0};
    final IOException[] maybeException = { null };
    final Thread reader = new Thread() {
      public void run() {
        // TDLog.Log( TDLog.LOG_PROTO, "reader run " + dataRead[0] + "/" + toRead[0] );
        try {
          // synchronized( dataRead ) 
          {
            count[0] = mIn.read( mBuffer, dataRead[0], toRead[0] );
            toRead[0]   -= count[0];
            dataRead[0] += count[0];
          }
        } catch ( ClosedByInterruptException e ) {
          TDLog.Error( "reader closed by interrupt");
        } catch ( IOException e ) {
          maybeException[0] = e;
        }
        // TDLog.Log( TDLog.LOG_PROTO, "reader done " + dataRead[0] + "/" + toRead[0] );
      }
    };
    reader.start();

    for ( int k=0; k<mMaxTimeout; ++k) {
      // Log.v("DistoX", "interrupt loop " + k + " " + dataRead[0] + "/" + toRead[0] );
      try {
        reader.join( timeout );
      } catch ( InterruptedException e ) { TDLog.Log(TDLog.LOG_DEBUG, "reader join-1 interrupted"); }
      if ( ! reader.isAlive() ) break;
      {
        Thread interruptor = new Thread() { public void run() {
          // Log.v("DistoX", "interruptor run " + dataRead[0] );
          for ( ; ; ) {
            // synchronized ( dataRead ) 
            {
              if ( dataRead[0] > 0 && toRead[0] > 0 ) { // FIXME
                try { wait( 100 ); } catch ( InterruptedException e ) { }
              } else {
                if ( reader.isAlive() ) reader.interrupt(); 
                break;
              }
            }
          }
          // Log.v("DistoX", "interruptor done " + dataRead[0] );
        } };
        interruptor.start();

        try {
          interruptor.join( 200 );
        } catch ( InterruptedException e ) { TDLog.Log(TDLog.LOG_DEBUG, "interruptor join interrupted"); }
      }
      try {
        reader.join( 200 );
      } catch ( InterruptedException e ) { TDLog.Log(TDLog.LOG_DEBUG, "reader join-2 interrupted"); }
      if ( ! reader.isAlive() ) break; 
    }
    if ( maybeException[0] != null ) throw maybeException[0];
    return dataRead[0];
  }

//-----------------------------------------------------

  DistoXProtocol( DataInputStream in, DataOutputStream out, Device device )
  {
    mDevice = device;
    // mSocket = socket;
    // mDistoX = distox;
    mSeqBit = (byte)0xff;

    // mHeadTailA3 = new byte[3];   // to read head/tail for Protocol A3
    // mHeadTailA3[0] = 0x38;
    // mHeadTailA3[1] = 0x20;       // address 0xC020
    // mHeadTailA3[2] = (byte)0xC0;

    mAddr8000 = new byte[3];
    mAddr8000[0] = 0x38;
    mAddr8000[1] = 0x00; // address 0x8000
    mAddr8000[2] = (byte)0x80;

    mAddress = new byte[2];
    mReplyBuffer   = new byte[4];
    mRequestBuffer = new byte[8];

    mAcknowledge = new byte[1];
    // mAcknowledge[0] = ( b & 0x80 ) | 0x55;

    // mBuffer = new byte[8];
  
    // try {
    //   if ( mSocket != null ) {
    //     // mIn  = new DataInputStream( extractCoreInputStream( mSocket.getInputStream() ) );
    //     mIn  = new DataInputStream( mSocket.getInputStream() );
    //     mOut = new DataOutputStream( mSocket.getOutputStream() );
    //   }
    // } catch ( IOException e ) {
    //   // NOTE socket is null there is nothing we can do
    //   TDLog.Error( "Proto cstr conn failed " + e.getMessage() );
    // }
    mIn  = in;
    mOut = out;
  }

  void closeIOstreams()
  {
    if ( mIn != null ) {
      try { mIn.close(); } catch ( IOException e ) { }
      mIn = null;
    }
    if ( mOut != null ) {
      try { mOut.close(); } catch ( IOException e ) { }
      mOut = null;
    }
  }

  private int handlePacket( ) 
  {
    byte type = (byte)(mBuffer[0] & 0x3f);
    // if ( TDLog.LOG_PROTO ) {
    //   TDLog.Log( TDLog.LOG_PROTO,
    //     "packet type " + type + " " + 
    //     String.format("%02x %02x %02x %02x %02x %02x %02x %02x", mBuffer[0], mBuffer[1], mBuffer[2],
    //     mBuffer[3], mBuffer[4], mBuffer[5], mBuffer[6], mBuffer[7] ) );
    // }

    int high, low;
    switch ( type ) {
      case 0x01: // data
        int dhh = (int)( mBuffer[0] & 0x40 );
        double d =  dhh * 1024.0 + MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
        double b = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
        double c = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
        // X31--ready
        mRollHigh = mBuffer[7];

        int r7 = (int)(mBuffer[7] & 0xff); if ( r7 < 0 ) r7 += 256;
        // double r = (mBuffer[7] & 0xff);
        double r = r7;

        if ( mDevice.mType == Device.DISTO_A3 || mDevice.mType == Device.DISTO_X000) {
          mDistance = d / 1000.0;
        } else if ( mDevice.mType == Device.DISTO_X310 ) {
          if ( d < 99999 ) {
            mDistance = d / 1000.0;
          } else {
            mDistance = 100 + (d-100000) / 100.0;
          }
        }

        mBearing  = b * 180.0 / 32768.0; // 180/0x8000;
        mClino    = c * 90.0  / 16384.0; // 90/0x4000;
        if ( c >= 32768 ) { mClino = (65536 - c) * (-90.0) / 16384.0; }
        mRoll = r * 180.0 / 128.0;

        // if ( TDLog.LOG_PROTO ) {
        //   TDLog.Log( TDLog.LOG_PROTO, "Proto packet data " + 
        //     String.format(Locale.US, " %7.2f %6.1f %6.1f", mDistance, mBearing, mClino ) );
        // }

        return DISTOX_PACKET_DATA;
      case 0x02: // g
        mGX = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
        mGY = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
        mGZ = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );

        if ( mGX > TopoDroidUtil.ZERO ) mGX = mGX - TopoDroidUtil.NEG;
        if ( mGY > TopoDroidUtil.ZERO ) mGY = mGY - TopoDroidUtil.NEG;
        if ( mGZ > TopoDroidUtil.ZERO ) mGZ = mGZ - TopoDroidUtil.NEG;
        // TDLog.Log( TDLog.LOG_PROTO, "handle Packet G " + String.format(" %x %x %x", mGX, mGY, mGZ ) );
        return DISTOX_PACKET_G;
      case 0x03: // m
        mMX = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
        mMY = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
        mMZ = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );

        if ( mMX > TopoDroidUtil.ZERO ) mMX = mMX - TopoDroidUtil.NEG;
        if ( mMY > TopoDroidUtil.ZERO ) mMY = mMY - TopoDroidUtil.NEG;
        if ( mMZ > TopoDroidUtil.ZERO ) mMZ = mMZ - TopoDroidUtil.NEG;
        // TDLog.Log( TDLog.LOG_PROTO, "handle Packet M " + String.format(" %x %x %x", mMX, mMY, mMZ ) );
        return DISTOX_PACKET_M;
      case 0x04: // vector data packet
        if ( mDevice.mType == Device.DISTO_X310 ) {
          double acc = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
          double mag = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
          double dip = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
          double rh = MemoryOctet.toInt( mRollHigh, mBuffer[7] );
          mAcceleration = acc;
          mMagnetic = mag;
          mDip = dip * 90.0  / 16384.0; // 90/0x4000;
          if ( dip >= 32768 ) { mDip = (65536 - dip) * (-90.0) / 16384.0; }
          mRoll  = rh * 180.0 / 32768.0; // 180/0x8000;
        }
        return DISTOX_PACKET_VECTOR;
      case 0x38: 
        mAddress[0] = mBuffer[1];
        mAddress[1] = mBuffer[2];
        mReplyBuffer[0] = mBuffer[3];
        mReplyBuffer[1] = mBuffer[4];
        mReplyBuffer[2] = mBuffer[5];
        mReplyBuffer[3] = mBuffer[6];
        // TDLog.Log( TDLog.LOG_PROTO, "handle Packet mReplyBuffer" );
        return DISTOX_PACKET_REPLY;
      default:
        TDLog.Error( 
          "packet error. type " + type + " " + 
          String.format("%02x %02x %02x %02x %02x %02x %02x %02x", mBuffer[0], mBuffer[1], mBuffer[2],
          mBuffer[3], mBuffer[4], mBuffer[5], mBuffer[6], mBuffer[7] ) );
      //   return DISTOX_PACKET_NONE;
    }
    return DISTOX_PACKET_NONE;
  } 

  int readPacket( boolean no_timeout )
  {
    int min_available = ( mDevice.mType == Device.DISTO_X000)? 8 : 1; // FIXME 8 should work in every case

    // TDLog.Log( TDLog.LOG_PROTO, "Proto read packet no-timeout " + (no_timeout?"no":"yes") );
    // Log.v( "DistoX", "VD Proto read packet no-timeout " + (no_timeout?"no":"yes") );
    try {
      final int maxtimeout = 8;
      int timeout = 0;
      int available = 0;

      if ( no_timeout ) {
        available = 8;
      } else { // do timeout
        if ( TDSetting.mZ6Workaround ) {
          available = getAvailable( 200, 2*maxtimeout );
        } else {
          // while ( ( available = mIn.available() ) == 0 && timeout < maxtimeout ) 
          while ( ( available = mIn.available() ) < min_available && timeout < maxtimeout ) {
            ++ timeout;
            try {
              // TDLog.Log( TDLog.LOG_PROTO, "Proto read packet sleep " + timeout + "/" + maxtimeout );
              // Log.v( "DistoX", "VD Proto read packet sleep " + timeout + "/" + maxtimeout );
              Thread.sleep( 100 );
            } catch (InterruptedException e ) {
              TDLog.Error( "Proto read packet InterruptedException" + e.toString() );
            }
          }
        }
      }
      // TDLog.Log( TDLog.LOG_PROTO, "Proto read packet available " + available );
      // Log.v( "DistoX", "VD Proto read packet available " + available );
      // if ( available > 0 ) 
      if ( available >= min_available ) {
        if ( no_timeout || ! TDSetting.mZ6Workaround ) {
          mIn.readFully( mBuffer, 0, 8 );
        }
        byte seq  = (byte)(mBuffer[0] & 0x80); // sequence bit
        // Log.v( "DistoX", "VD read packet seq bit " + String.format("%02x %02x %02x", mBuffer[0], seq, mSeqBit ) );
        boolean ok = ( seq != mSeqBit );
        mSeqBit = seq;
        // if ( (mBuffer[0] & 0x0f) != 0 ) // ack every packet
        { 
          mAcknowledge[0] = (byte)(( mBuffer[0] & 0x80 ) | 0x55);
          // if ( TDLog.LOG_PROTO ) {
          //   TDLog.Log( TDLog.LOG_PROTO,
          //     "read packet byte " + String.format(" %02x", mBuffer[0] ) + " ... writing ack" );
          // }
          mOut.write( mAcknowledge, 0, 1 );
        }
        if ( ok ) return handlePacket();
      } // else timedout with no packet
    } catch ( EOFException e ) {
      TDLog.Log( TDLog.LOG_PROTO, "Proto read packet EOFException" + e.toString() );
    } catch (ClosedByInterruptException e ) {
      TDLog.Error( "Proto read packet ClosedByInterruptException" + e.toString() );
    } catch (IOException e ) {
      // this is OK: the DistoX has been turned off
      TDLog.Debug( "Proto read packet IOException " + e.toString() + " OK distox turned off" );
      return DISTOX_ERR_OFF;
    }
    return DISTOX_PACKET_NONE;
  }

  boolean sendCommand( byte cmd )
  {
    // TDLog.Log( TDLog.LOG_PROTO, "sendCommand " + String.format("Send command %02x", cmd ) );

    try {
      mRequestBuffer[0] = (byte)(cmd);
      mOut.write( mRequestBuffer, 0, 1 );
      mOut.flush();
    } catch (IOException e ) {
      TDLog.Error( "sendCommand failed" );
      return false;
    }
    return true;
  }

  int readToRead( byte[] command, boolean a3 ) // number of data-packet to read
  {
    int ret = 0;
    try {
      mOut.write( command, 0, 3 );
      mIn.readFully( mBuffer, 0, 8 );
      if ( mBuffer[0] != (byte)( 0x38 ) ) { return DISTOX_ERR_HEADTAIL; }
      if ( mBuffer[1] != command[1] ) { return DISTOX_ERR_HEADTAIL; }
      if ( mBuffer[2] != command[2] ) { return DISTOX_ERR_HEADTAIL; }
      int head = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
      int tail = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
      if ( a3 ) {
        ret = ( head >= tail )? (head-tail)/8 : ((DeviceA3Details.MAX_ADDRESS_A3 - tail) + head)/8; 
      } else {
        // head = head segment index
        // tail = tail packet index
        int hp = 2 * head; // head packet index
        ret = ( hp >= tail )? (hp - tail) : (hp + (DeviceX310Details.MAX_INDEX_X310 - tail) );
        // ret can be odd
      }

      // DEBUG
      // if ( TDLog.LOG_PROTO ) {
      //   TDLog.Log( TDLog.LOG_PROTO, 
      //     "Proto readToRead Head-Tail " + 
      //     String.format("%02x%02x-%02x%02x", mBuffer[4], mBuffer[3], mBuffer[6], mBuffer[5] )
      //     + " " + head + " - " + tail + " = " + ret );
      // }
      return ret;
    } catch ( EOFException e ) {
      TDLog.Error( "Proto readToRead Head-Tail read() failed" );
      return DISTOX_ERR_HEADTAIL_EOF;
    } catch (IOException e ) {
      TDLog.Error( "Proto readToRead Head-Tail read() failed" );
      return DISTOX_ERR_HEADTAIL_IO;
    }
  }

  boolean swapHotBit( int addr ) // only A3
  {
    try {
      mBuffer[0] = (byte) 0x38;
      mBuffer[1] = (byte)( addr & 0xff );
      mBuffer[2] = (byte)( (addr>>8) & 0xff );
      mOut.write( mBuffer, 0, 3 );
      mIn.readFully( mBuffer, 0, 8 );
      if ( mBuffer[0] != (byte)0x38 ) { 
        TDLog.Error( "HotBit-38 wrong reply packet addr " + addr );
        return false;
      }

      int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
      // Log.v( TopoDroidApp.TAG, "proto read ... addr " + addr + " reply addr " + reply_addr );
      if ( reply_addr != addr ) {
        TDLog.Error( "HotBit-38 wrong reply addr " + reply_addr + " addr " + addr );
        return false;
      }
      mBuffer[0] = (byte)0x39;
      // mBuffer[1] = (byte)( addr & 0xff );
      // mBuffer[2] = (byte)( (addr>>8) & 0xff );
      if ( mBuffer[3] == 0x00 ) {
        TDLog.Error( "HotBit refusing to swap addr " + addr );
        return false;
      }  

      mBuffer[3] |= (byte)0x80; // RESET HOT BIT
      mOut.write( mBuffer, 0, 7 );
      mIn.readFully( mBuffer, 0, 8 );
      if ( mBuffer[0] != (byte)0x38 ) {
        TDLog.Error( "HotBit-39 wrong reply packet addr " + addr );
        return false;
      }
      reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1] );
      // Log.v( TopoDroidApp.TAG, "proto reset ... addr " + addr + " reply addr " + reply_addr );
      if ( reply_addr != addr ) {
        TDLog.Error( "HotBit-39 wrong reply addr " + reply_addr + " addr " + addr );
        return false;
      }
    } catch ( EOFException e ) {
      TDLog.Error( "HotBit EOF failed addr " + addr );
      return false;
    } catch (IOException e ) {
      TDLog.Error( "HotBit IO failed addr " + addr );
      return false;
    }
    return true;
  }

  // @param command head-tail command with the memory address of head-tail words
  String readHeadTail( byte[] command, int[] head_tail )
  {
    try {
      mOut.write( command, 0, 3 );
      mIn.readFully( mBuffer, 0, 8 );
      if ( mBuffer[0] != (byte)( 0x38 ) ) { return null; }
      if ( mBuffer[1] != command[1] ) { return null; }
      if ( mBuffer[2] != command[2] ) { return null; }
      // TODO value of Head-Tail in byte[3-7]
      head_tail[0] = MemoryOctet.toInt( mBuffer[4], mBuffer[3] );
      head_tail[1] = MemoryOctet.toInt( mBuffer[6], mBuffer[5] );
      return String.format("%02x%02x-%02x%02x", mBuffer[4], mBuffer[3], mBuffer[6], mBuffer[5] );
      // TDLog.Log( TDLog.LOG_PROTO, "read Head Tail " + res );
    } catch ( EOFException e ) {
      TDLog.Error( "read Head Tail read() EOF failed" );
      return null;
    } catch (IOException e ) {
      TDLog.Error( "read Head Tail read() IO failed" );
      return null;
    }
  }

  // X310    
  private static int DATA_PER_BLOCK = 56;
  private static int BYTE_PER_DATA  = 18;
  // note 56*18 = 1008
  // next there are 16 byte padding for each 1024-byte block (0x400 byte block)
  //
  // address space = 0x0000 - 0x7fff
  // blocks: 0000 - 03ff =    0 -   55
  //         0400 - 07ff =   56 -  111
  //         0800 - 0bff =  112 -  167
  //         0c00 - 0fff =  168 -  223
  //         1000 - 13ff =  224 -  279
  //         ...
  //         2000 - 23ff =  448 -  503
  //         ...
  //         3000 - 33ff =  672 -  727
  //         4000 - 43ff =  896 -  951
  //         5000 - 53ff = 1120 - 1175
  //         6000 - 63ff = 1344 - 1399
  //         7000 - 73ff = 1568 - 1623
  //         ...
  //         7c00 - 7fff = 1736 - 1791
  // 

  private int index2addrX310( int index )
  {
    if ( index < 0 ) index = 0;
    if ( index > 1792 ) index = 1792;
    int addr = 0;
    while ( index >= DATA_PER_BLOCK ) {
      index -= DATA_PER_BLOCK;
      addr += 0x400;
    }
    addr += BYTE_PER_DATA * index;
    return addr;
  }

  int addr2indexX310( int addr )
  {
    int index = 0;
    addr = addr - ( addr % 8 );
    while ( addr >= 0x400 ) {
      addr -= 0x400;
      index += DATA_PER_BLOCK;
    }
    index += (int)(addr/BYTE_PER_DATA);
    return index;
  }

  // memory layout
  // byte 0-7  first packet
  // byte 8-15 second packet
  // byte 16   hot-flag for the first packet
  // byte 17   hot-flag for the second packet
  //
  // X310 data address space: 0x0000 - 0x7fff
  // each data takes 18 bytes


  int readX310Memory( int start, int end, List< MemoryOctet > data )
  {
    // Log.v( "DistoX", "start " + start + " end " + end );
    int cnt = 0;
    while ( start < end ) {
      int addr = index2addrX310( start );
      // Log.v( "DistoX", start + " addr " + addr );
      int endaddr = addr + BYTE_PER_DATA;
      MemoryOctet result = new MemoryOctet( start );
      // read only bytes 0-7 and 16-17
      int k = 0;
      for ( ; addr < endaddr && k < 8; addr += 4, k+=4 ) {
        mBuffer[0] = (byte)( 0x38 );
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        // TODO write and read
        try {
          mOut.write( mBuffer, 0, 3 );
          mIn.readFully( mBuffer, 0, 8 );
        } catch ( IOException e ) {
          TDLog.Error( "readMemory() IO failed" );
          break;
        }
        if ( mBuffer[0] != (byte)( 0x38 ) ) break;
        int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1]);
        if ( reply_addr != addr ) break;
        for (int i=3; i<7; ++i) result.data[k+i-3] = mBuffer[i];
      }
      if ( k == 8 ) {
        addr = index2addrX310( start ) + 16;
        mBuffer[0] = (byte)( 0x38 );
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        try {
          mOut.write( mBuffer, 0, 3 );
          mIn.readFully( mBuffer, 0, 8 );
        } catch ( IOException e ) {
          TDLog.Error( "readMemory() IO failed" );
          break;
        }
        if ( mBuffer[0] != (byte)( 0x38 ) ) break;
        if ( mBuffer[3] == (byte)( 0xff ) ) result.data[0] |= (byte)( 0x80 ); 
        data.add( result );
        // Log.v( TopoDroidApp.TAG, "memory " + result.toString() + " " + mBuffer[3] );
        ++ cnt;
      } else {
        break;
      }
      ++start;
    }
    return cnt;
  }

  // X310 data memory is read-only
  // // return number of memory slots that have been reset
  // public int resetX310Memory( int start, int end )
  // {
  //   int cnt = start;
  //   while ( start < end ) {
  //     int addr = index2addrX310( start ) + 16;
  //     mBuffer[0] = (byte)( 0x38 );
  //     mBuffer[1] = (byte)( addr & 0xff );
  //     mBuffer[2] = (byte)( (addr>>8) & 0xff );
  //     TDLog.Error( "resetMemory() address " + mBuffer[1] + " " + mBuffer[2] );

  //     // TODO write and read
  //     try {
  //       mOut.write( mBuffer, 0, 3 );
  //       mIn.readFully( mBuffer, 0, 8 );
  //     } catch ( IOException e ) {
  //       TDLog.Error( "resetMemory() IO nr. 1 failed" );
  //       break;
  //     }
  //     if ( mBuffer[0] != (byte)( 0x38 ) ||
  //          mBuffer[1] != (byte)( addr & 0xff ) ||
  //          mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) {
  //       TDLog.Error( "resetMemory() bad read reply " + mBuffer[0] + " addr " + mBuffer[1] + " " + mBuffer[2] );
  //       break;
  //     }
  //     TDLog.Error( "resetMemory() ok read reply " + mBuffer[3] + " " + mBuffer[4] + " " + mBuffer[5] + " " + mBuffer[6] );

  //     mBuffer[0] = (byte)( 0x39 );
  //     mBuffer[1] = (byte)( addr & 0xff );
  //     mBuffer[2] = (byte)( (addr>>8) & 0xff );
  //     mBuffer[3] = (byte)( 0xff );
  //     mBuffer[4] = (byte)( 0xff );
  //     try {
  //       mOut.write( mBuffer, 0, 7 );
  //       mIn.readFully( mBuffer, 0, 8 );
  //     } catch ( IOException e ) {
  //       TDLog.Error( "resetMemory() IO nr. 2 failed" );
  //       break;
  //     }
  //     if ( mBuffer[0] != (byte)( 0x38 ) ||
  //          mBuffer[1] != (byte)( addr & 0xff ) ||
  //          mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) {
  //       TDLog.Error( "resetMemory() bad write reply " + mBuffer[0] + " addr " + mBuffer[1] + " " + mBuffer[2] );
  //       break;
  //     }
  //     TDLog.Error( "resetMemory() ok write reply " + mBuffer[3] + " " + mBuffer[4] + " " + mBuffer[5] + " " + mBuffer[6] );
  //     ++ start;
  //   }
  //   return start - cnt;
  // }

  byte[] readMemory( int addr )
  {
    mBuffer[0] = (byte)( 0x38 );
    mBuffer[1] = (byte)( addr & 0xff );
    mBuffer[2] = (byte)( (addr>>8) & 0xff );
    try {
      mOut.write( mBuffer, 0, 3 );
      mIn.readFully( mBuffer, 0, 8 );
    } catch ( IOException e ) {
      TDLog.Error( "readMemory() IO failed" );
      return null;
    }
    if ( mBuffer[0] != (byte)( 0x38 ) ) return null;
    int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1]);
    if ( reply_addr != addr ) return null;
    byte[] ret = new byte[4];
    for (int i=3; i<7; ++i) ret[i-3] = mBuffer[i];
    return ret;
  }

  int readMemory( int start, int end, List< MemoryOctet > data )
  {
    if ( start < 0 ) start = 0;
    if ( end > 0x8000 ) end = 0x8000;
    start = start - start % 8;
    end   = end - ( end % 8 );
    if ( start >= end ) return -1;
    int cnt = 0; // number of data read
    for ( ; start < end; start += 8 ) {
      MemoryOctet result = new MemoryOctet( start/8 );
      int k = 0;
      for ( ; k<8; k+=4 ) {
        int addr = start+k;
        mBuffer[0] = (byte)( 0x38 );
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        // TODO write and read
        try {
          mOut.write( mBuffer, 0, 3 );
          mIn.readFully( mBuffer, 0, 8 );
        } catch ( IOException e ) {
          TDLog.Error( "readMemory() IO failed" );
          break;
        }
        if ( mBuffer[0] != (byte)( 0x38 ) ) break;
        int reply_addr = MemoryOctet.toInt( mBuffer[2], mBuffer[1]);
        if ( reply_addr != addr ) break;
        for (int i=3; i<7; ++i) result.data[k+i-3] = mBuffer[i];
      }
      if ( k == 8 ) {
        data.add( result );
        // Log.v( TopoDroidApp.TAG, "memory " + result.toString() );
        ++ cnt;
      } else {
        break;
      }
    }
    return cnt;
  }

  boolean read8000( byte[] result )
  {
    try {
      mOut.write( mAddr8000, 0, 3 );
      mIn.readFully( mBuffer, 0, 8 );
      if ( mBuffer[0] != (byte)( 0x38 ) ) { return false; }
      if ( mBuffer[1] != mAddr8000[1] ) { return false; }
      if ( mBuffer[2] != mAddr8000[2] ) { return false; }
      result[0] = mBuffer[3];
      result[1] = mBuffer[4];
      result[2] = mBuffer[5];
      result[3] = mBuffer[6];
    } catch ( EOFException e ) {
      TDLog.Error( "read8000 read() EOF failed" );
      return false;
    } catch (IOException e ) {
      TDLog.Error( "read8000 read() IO failed" );
      return false;
    }
    return true;
  }

  boolean writeCalibration( byte[] calib )
  { 
    if ( calib == null ) return false;
    int  len  = calib.length;
    // Log.v("DistoX", "writeCalibration length " + len );
    long addr = 0x8010;
    // long end  = addr + len;
    try {
      int k = 0;
      while ( k < len ) {
        mBuffer[0] = 0x39;
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        mBuffer[3] = calib[k]; ++k;
        mBuffer[4] = calib[k]; ++k;
        mBuffer[5] = calib[k]; ++k;
        mBuffer[6] = calib[k]; ++k;
        mOut.write( mBuffer, 0, 7 );
        mIn.readFully( mBuffer, 0, 8 );
        // TDLog.Log( TDLog.LOG_PROTO, "writeCalibration " + 
        //   String.format("%02x %02x %02x %02x %02x %02x %02x %02x", mBuffer[0], mBuffer[1], mBuffer[2],
        //   mBuffer[3], mBuffer[4], mBuffer[5], mBuffer[6], mBuffer[7] ) );
        if ( mBuffer[0] != 0x38 ) { return false; }
        if ( mBuffer[1] != (byte)( addr & 0xff ) ) { return false; }
        if ( mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) { return false; }
        addr += 4;
      }
    } catch ( EOFException e ) {
      // TDLog.Error( "writeCalibration EOF failed" );
      return false;
    } catch (IOException e ) {
      // TDLog.Error( "writeCalibration IO failed" );
      return false;
    }
    return true;  
  }

  // called only by DistoXComm.readCoeff (TopoDroidComm.readCoeff)
  boolean readCalibration( byte[] calib )
  {
    if ( calib == null ) return false;
    int  len  = calib.length;
    if ( len > 52 ) len = 52; // FIXME force max length of calib coeffs
    int addr = 0x8010;
    // int end  = addr + len;
    try {
      int k = 0;
      while ( k < len ) { 
        mBuffer[0] = 0x38;
        mBuffer[1] = (byte)( addr & 0xff );
        mBuffer[2] = (byte)( (addr>>8) & 0xff );
        mOut.write( mBuffer, 0, 3 );
        mIn.readFully( mBuffer, 0, 8 );
        if ( mBuffer[0] != 0x38 ) { return false; }
        if ( mBuffer[1] != (byte)( addr & 0xff ) ) { return false; }
        if ( mBuffer[2] != (byte)( (addr>>8) & 0xff ) ) { return false; }
        calib[k] = mBuffer[3]; ++k;
        calib[k] = mBuffer[4]; ++k;
        calib[k] = mBuffer[5]; ++k;
        calib[k] = mBuffer[6]; ++k;
        addr += 4;
      }
    } catch ( EOFException e ) {
      // TDLog.Error( "readCalibration EOF failed" );
      return false;
    } catch (IOException e ) {
      // TDLog.Error( "readCalibration IO failed" );
      return false;
    }
    return true;  
  }

  int uploadFirmware( String filepath )
  {
    TDLog.LogFile( "Firmware upload: protocol starts" );
    byte[] buf = new byte[259];
    buf[0] = (byte)0x3b;
    buf[1] = (byte)0;
    buf[2] = (byte)0;

    boolean ok = true;
    int cnt = 0;
    try {
      File fp = new File( filepath );
      FileInputStream fis = new FileInputStream( fp );
      DataInputStream dis = new DataInputStream( fis );
      // int end_addr = (fp.size() + 255)/256;

      for ( int addr = 0; /* addr < end_addr */; ++ addr ) {
        TDLog.LogFile( "Firmware upload: addr " + addr + " count " + cnt );
        // memset(buf+3, 0, 256)
        for (int k=0; k<256; ++k) buf[3+k] = (byte)0xff;
        try {
          int nr = dis.read( buf, 3, 256 );
          if ( nr <= 0 ) {
            TDLog.LogFile( "Firmware upload: file read failure. Result " + nr );
            break;
          }
          cnt += nr;
          if ( addr >= 0x08 ) {
            buf[0] = (byte)0x3b;
            buf[1] = (byte)( addr & 0xff );
            buf[2] = 0; // not necessary
            mOut.write( buf, 0, 259 );
            mIn.readFully( mBuffer, 0, 8 );
            int reply_addr = ( ((int)(mBuffer[2]))<<8 ) + ((int)(mBuffer[1]));
            if ( mBuffer[0] != (byte)0x3b || addr != reply_addr ) {
              TDLog.LogFile( "Firmware upload: fail at " + cnt + " buffer[0]: " + mBuffer[0] + " reply_addr " + reply_addr );
              ok = false;
              break;
            } else {
              TDLog.LogFile( "Firmware upload: reply address ok");
            }
          } else {
            TDLog.LogFile( "Firmware upload: skip address " + addr );
          }
        } catch ( EOFException e ) { // OK
          TDLog.LogFile( "Firmware update: EOF " + e.getMessage() );
          break;
        } catch ( IOException e ) { 
          TDLog.LogFile( "Firmware update: IO error " + e.getMessage() );
          ok = false;
          break;
        }
      }
    } catch ( FileNotFoundException e ) {
      TDLog.LogFile( "Firmware update: Not Found error " + e.getMessage() );
      return 0;
    }
    TDLog.LogFile( "Firmware update: result is " + (ok? "OK" : "FAIL") + " count " + cnt );
    return ( ok ? cnt : -cnt );
  }

  int dumpFirmware( String filepath )
  {
    TDLog.LogFile( "Firmware dump: output filepath " + filepath );
    byte[] buf = new byte[256];

    boolean ok = true;
    int cnt = 0;
    try {
      TDPath.checkPath( filepath );
      File fp = new File( filepath );
      FileOutputStream fos = new FileOutputStream( fp );
      DataOutputStream dos = new DataOutputStream( fos );
      for ( int addr = 0; ; ++ addr ) {
        TDLog.LogFile( "Firmware dump: addr " + addr + " count " + cnt );
        try {
          buf[0] = (byte)0x3a;
          buf[1] = (byte)( addr & 0xff );
          buf[2] = 0; // not necessary
          mOut.write( buf, 0, 3 );

          mIn.readFully( mBuffer, 0, 8 );
          int reply_addr = ( ((int)(mBuffer[2]))<<8 ) + ((int)(mBuffer[1]));
          if ( mBuffer[0] != (byte)0x3a || addr != reply_addr ) {
            TDLog.LogFile( "Firmware dump: fail at " + cnt + " buffer[0]: " + mBuffer[0] + " reply_addr " + reply_addr );
            ok = false;
            break;
          } else {
            TDLog.LogFile( "Firmware dump: reply addr ok");
          }

          mIn.readFully( buf, 0, 256 );
          boolean last = true;
          for ( int k=0; last && k<256; ++k ) {
            if ( buf[k] != (byte)0xff ) last = false;
          }
          if ( last ) break;
          dos.write( buf, 0, 256 );
          cnt += 256;
        } catch ( EOFException e ) { // OK
          break;
        } catch ( IOException e ) { 
          ok = false;
          break;
        }
      }
    } catch ( FileNotFoundException e ) {
      return 0;
    }
    TDLog.LogFile( "Firmware dump: result is " + (ok? "OK" : "FAIL") + " count " + cnt );
    return ( ok ? cnt : -cnt );
  }

};

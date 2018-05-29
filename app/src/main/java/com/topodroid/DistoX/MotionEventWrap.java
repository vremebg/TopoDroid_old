/***
 * Excerpted from "Hello, Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
 *
 * This class has been taken from "Hello, Android" source code
***/
package com.topodroid.DistoX;

import android.os.Build;
import android.view.MotionEvent;
// import android.util.Log;

class MotionEventWrap
{
   protected MotionEvent event;

   MotionEventWrap( MotionEvent event )
   {
      this.event = event;
   }

   static MotionEventWrap wrap(MotionEvent event)
   {
      // FIXME NFE 

      // Use Build.VERSION.SDK_INT if you don't have to support Cupcake
      if (Integer.parseInt(Build.VERSION.SDK) >= Build.VERSION_CODES.ECLAIR) {
         // Log.d("MotionEventWrap", "Using Eclair version");
         return new MotionEventEclair(event);
      } else {
         // Log.d("MotionEventWrap", "Using Cupcake/Donut version");
         return new MotionEventWrap(event);
      }
   }
   
   int getAction() { return event.getAction(); }

   // this is the X coord (pixels, from the center ?) adjusted for containing window and views
   // to get the real X use getRawX()
   //
   float getX() { return event.getX(); }
   float getX(int pointerIndex)
   {
      verifyPointerIndex(pointerIndex);
      return getX();
   }
   float getY() { return event.getY(); }
   float getY(int pointerIndex)
   {
      verifyPointerIndex(pointerIndex);
      return getY();
   }
   int getPointerCount() { return 1; }
   int getPointerId(int pointerIndex)
   {
      verifyPointerIndex(pointerIndex);
      return 0;
   }
   private void verifyPointerIndex(int pointerIndex) 
   {
      if (pointerIndex > 0) {
         throw new IllegalArgumentException(
               "Invalid pointer index for Donut/Cupcake");
      }
   }
   
}


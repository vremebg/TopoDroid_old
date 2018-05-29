/** @file SketchDef.java
 *
 * @author marco corvi
 * @date mar 2013
 *
 * @brief TopoDroid 3d sketch: defines
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class SketchDef
{
    // static final float LINE_STEP = 0.5f; // 0.5 m between line 3d points
    // static final float INNER_BORDER_STEP = 0.2f; // 0.2 m between border-line 3d points
    // static final float SECTION_STEP = 0.5f;  
    static final float CLOSE_GAP  = 1.0f;
    static final int POINT_MIN    =  4; //  4 minimum number of 3D points on a line
    // static final int POINT_MAX    = 20; // 12 maximum number of 3D points on a line. UNUSED
    static final float MIN_DISTANCE = 20.0f; // minimum closeness distance (select at)

    // public static final float ZOOM_INC = 1.4f;
    // public static final float ZOOM_DEC = 1.0f/ZOOM_INC;

    final static int DISPLAY_NGBH = 0;
    final static int DISPLAY_SINGLE = 1;
    final static int DISPLAY_ALL  = 2;
    final static int DISPLAY_NONE = 3;
    final static int DISPLAY_MAX  = 4;

    static final int MODE_MOVE    = 0;
    static final int MODE_DRAW    = 1;
    static final int MODE_EDIT    = 2;  // change the surface as a whole
    static final int MODE_SELECT  = 3;  // select a point to edit
    // static final int MODE_STEP    = 4;  // step to another leg
    // static final int MODE_HEAD = 5;
    // static final int MODE_JOIN = 6;

    static final int TOUCH_NONE = 0;
    static final int TOUCH_MOVE = 2;
    static final int TOUCH_ZOOM = 5;

    // static final int VIEW_NONE  = 0;
    // static final int VIEW_3D    = 1;

    static final int EDIT_NONE = 0;
    static final int EDIT_CUT = 1;
    static final int EDIT_STRETCH = 2;
    static final int EDIT_EXTRUDE = 3;

    static final int SELECT_NONE = 0;
    static final int SELECT_SECTION = 1;
    static final int SELECT_STEP = 2;
    static final int SELECT_SHOT = 3;
    static final int SELECT_JOIN = 4;
    
    static final int LINE_SECTION = -1; // section line type

    static final String[] mode_name = { "none", "draw", "move", "item" };
    // static final String[] view_type = { "none", "3d" };
    static final String[] edit_name = { "none", "cut", "stretch", "extrude" };

}

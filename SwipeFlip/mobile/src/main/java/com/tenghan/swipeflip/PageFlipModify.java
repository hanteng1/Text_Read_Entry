package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLUtils;
import android.util.Log;
import android.widget.Scroller;

import com.eschao.android.widget.pageflip.FoldBackVertexProgram;
import com.eschao.android.widget.pageflip.FoldBackVertexes;
import com.eschao.android.widget.pageflip.GLPoint;
import com.eschao.android.widget.pageflip.GLViewRect;
import com.eschao.android.widget.pageflip.OnPageFlipListener;
import com.eschao.android.widget.pageflip.Page;
import com.eschao.android.widget.pageflip.PageFlipException;
import com.eschao.android.widget.pageflip.PageFlipState;
import com.eschao.android.widget.pageflip.PageFlipUtils;
import com.eschao.android.widget.pageflip.ShadowVertexProgram;
import com.eschao.android.widget.pageflip.ShadowVertexes;
import com.eschao.android.widget.pageflip.ShadowWidth;
import com.eschao.android.widget.pageflip.VertexProgram;
import com.eschao.android.widget.pageflip.Vertexes;
import com.eschao.android.widget.pageflip.modify.PageModify;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_TEST;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glClearDepthf;
import static android.opengl.GLES20.glDepthRangef;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glViewport;

/**
 * Created by hanteng on 2017-06-01.
 * a modified version of pageflip
 * to hold many pages, without drawing secodary textures
 */

public class PageFlipModify {

    //global variables and parameters


    final static String TAG = "PageFlipModify";

    // default pixels of mesh vertex
    private final static int DEFAULT_MESH_VERTEX_PIXELS = 10;
    //private final static int MESH_COUNT_THRESHOLD = 20;

    /*
    // The min page curl angle (5 degree)
    private final static int MIN_PAGE_CURL_ANGLE = 5;
    // The max page curl angle (5 degree)
    private final static int MAX_PAGE_CURL_ANGLE = 65;
    private final static int PAGE_CURL_ANGEL_DIFF = MAX_PAGE_CURL_ANGLE -
            MIN_PAGE_CURL_ANGLE;
    private final static float MIN_PAGE_CURL_RADIAN =
            (float)(Math.PI * MIN_PAGE_CURL_ANGLE / 180);
    private final static float MAX_PAGE_CURL_RADIAN=
            (float)(Math.PI * MAX_PAGE_CURL_ANGLE / 180);
    private final static float MIN_PAGE_CURL_TAN_OF_ANGLE =
            (float)Math.tan(MIN_PAGE_CURL_RADIAN);
    private final static float MAX_PAGE_CURL_TAN_OF_ANGEL =
            (float)Math.tan(MAX_PAGE_CURL_RADIAN);
    private final static float MAX_PAGE_CURL_ANGLE_RATIO =
            MAX_PAGE_CURL_ANGLE / 90f;
    private final static float MAX_TAN_OF_FORWARD_FLIP =
            (float)Math.tan(Math.PI / 6);
    private final static float MAX_TAN_OF_BACKWARD_FLIP =
            (float)Math.tan(Math.PI / 20);
    */


    // width ratio of clicking to flip
    private final static float WIDTH_RATIO_OF_CLICK_TO_FLIP = 0.5f;

    // width ratio of triggering restore flip
    //1.0 means it is always restoring when finger's up
    private final static float WIDTH_RATIO_OF_RESTORE_FLIP = 1.0f;

    /*
    // folder page shadow color buffer size
    private final static int FOLD_TOP_EDGE_SHADOW_VEX_COUNT = 22;

    // fold edge shadow color
    private final static float FOLD_EDGE_SHADOW_START_COLOR = 0.1f;
    private final static float FOLD_EDGE_SHADOW_START_ALPHA = 0.25f;
    private final static float FOLD_EDGE_SHADOW_END_COLOR = 0.3f;
    private final static float FOLD_EDGE_SHADOW_END_ALPHA = 0f;

    // fold base shadow color
    private final static float FOLD_BASE_SHADOW_START_COLOR = 0.05f;
    private final static float FOLD_BASE_SHADOW_START_ALPHA = 0.4f;
    private final static float FOLD_BASE_SHADOW_END_COLOR = 0.3f;
    private final static float FOLD_BASE_SHADOW_END_ALPHA = 0f;

*/
    // pages and index
    private final static int FIRST_PAGE = 0;
    private int pageIndex;
    public final static int PAGE_SIZE = 5; // 2 for testing

    // view size
    private GLViewRect mViewRect;

    // the pixel size for each mesh
    private int mPixelsOfMesh;

    // gradient shadow texture id
    private int mGradientShadowTextureID;

    // touch point and last touch point
    private PointF mTouchP;
    // the last touch point (could be deleted?)
    private PointF mLastTouchP;
    // the first touch point when finger down on the screen
    private PointF mStartTouchP;



    // the middle point between touch point and origin point
    //private PointF mMiddleP;


    //========================================================================================================
    //variables for each single page
    //some variables can be put to the page class
    //this class is just a controller, that corresponds to user input

    // from 2D perspective, the line will intersect Y axis and X axis that being
    // through middle point and perpendicular to the line which is from touch
    // point to origin point, The point on Y axis is mYFoldP, the mXFoldP is on
    // X axis. The mY{X}FoldP1 is up mY{X}FoldP, The mY{X}FoldP0 is under
    // mY{X}FoldP
    //
    //        -----> Flip in reverse way
    //                          ^ Y
    //                          |
    //                          + mYFoldP1
    //                        / |
    //                       /  |
    //                      /   |
    //                     /    |
    //                    /     |
    //                   /      |
    //                  /       + mYFoldP
    //    mTouchP      /      / |
    //       .        /      /  |
    //               /      /   |
    //              /      /    |
    //             /      /     |
    //            /   .  /      + mYFoldP0
    //           /      /      /|
    //          /      /      / |
    //         /      /      /  |
    //X <-----+------+------+---+ originP
    //   mXFoldP1 mXFoldP mXFoldP0
    //

    /*
    private PointF mYFoldPc;
    private PointF mYFoldP0c;
    private PointF mYFoldP1c;
    private PointF mXFoldPc;
    private PointF mXFoldP0c;
    private PointF mXFoldP1c;
    */

    //private float mTransOffX[];  //<0 means translate towards left


    /*
    //            ^ Y
    //   mTouchP  |
    //        +   |
    //         \  |
    //          \ |
    //       A ( \|
    // X <--------+ originP
    //
    // A is angle between X axis and line from mTouchP to originP
    // the max curling angle between line from touchP to originP and X axis
    private float mMaxT2OAngleTan;
    // another max curling angle when finger moving causes the originP change
    // from (x, y) to (x, -y) which means mirror based on Y axis.
    private float mMaxT2DAngleTan;
    // the tan value of current curling angle
    // mKValue = (touchP.y - originP.y) / (touchP.x - originP.x)
    private float mKValue;
    // the length of line from mTouchP to originP
    private float mLenOfTouchOrigin;
    // the cylinder radius
    private float mR;
    // the perimeter ratio of semi-cylinder based on mLenOfTouchOrigin;
    private float mSemiPerimeterRatio;
    // Mesh count
    private int mMeshCount;

    // edges shadow width of back of fold page
    private ShadowWidth mFoldEdgesShadowWidth;
    // base shadow width of front of fold page
    private ShadowWidth mFoldBaseShadowWidth;

    // fold page and shadow vertexes
    private Vertexes mFoldFrontVertexes;
    private FoldBackVertexes mFoldBackVertexes;
    private ShadowVertexes mFoldEdgesShadow;
    private ShadowVertexes mFoldBaseShadow;


*/

    // Shader program for openGL drawing
    //one for all page instances to keep efficiency
    private VertexProgram mVertexProgram;
    private FoldBackVertexProgram mFoldBackVertexProgram;
    private ShadowVertexProgram mShadowVertexProgram;


    // is vertical page flip
    private boolean mIsVertical;
    private PageFlipState mFlipState;

    // use for flip animation
    private Scroller mScroller;
    private Context mContext;

    // pages and page mode, many pages, default 10
    public PageModify mPages[];  //now  try with 10

    // is clicking to flip page
    private boolean mIsClickToFlip;
    // width ration of clicking to flip
    private float mWidthRationOfClickToFlip;

    // listener for page flipping
    private OnPageFlipListener mListener;

    /**
     * Constructor
     */
    public PageFlipModify(Context context) {
        mContext = context;
        mScroller = new Scroller(context);

        mFlipState = PageFlipState.END_FLIP;

        mIsVertical = false;
        mViewRect = new GLViewRect();
        mPixelsOfMesh = DEFAULT_MESH_VERTEX_PIXELS;

        //mSemiPerimeterRatio = 0.8f;

        mIsClickToFlip = true;
        mListener = null;
        mWidthRationOfClickToFlip = WIDTH_RATIO_OF_CLICK_TO_FLIP;

        // init pages
        mPages = new PageModify[PAGE_SIZE];  //10 for now

        //init parameters
        //mTransOffX = new float[PAGE_SIZE];

        // key points
        //these points can be the same for all

        /*
        mMiddleP = new PointF();
        mYFoldPc = new PointF();
        mYFoldP0c = new PointF();
        mYFoldP1c = new PointF();
        mXFoldPc = new PointF();
        mXFoldP0c = new PointF();
        mXFoldP1c = new PointF();
        */


        mTouchP = new PointF();
        mLastTouchP = new PointF();
        mStartTouchP = new PointF();

        // init shadow width
        //mFoldEdgesShadowWidth = new ShadowWidth(5, 30, 0.25f);
        //mFoldBaseShadowWidth = new ShadowWidth(2, 40, 0.4f);

        // init shader program
        mVertexProgram = new VertexProgram();
        mFoldBackVertexProgram = new FoldBackVertexProgram();
        mShadowVertexProgram = new ShadowVertexProgram();

        // init vertexes
        /*
        mFoldFrontVertexes = new Vertexes();
        mFoldBackVertexes = new FoldBackVertexes();
        mFoldEdgesShadow = new ShadowVertexes(FOLD_TOP_EDGE_SHADOW_VEX_COUNT,
                FOLD_EDGE_SHADOW_START_COLOR,
                FOLD_EDGE_SHADOW_START_ALPHA,
                FOLD_EDGE_SHADOW_END_COLOR,
                FOLD_EDGE_SHADOW_END_ALPHA);
        mFoldBaseShadow = new ShadowVertexes(0,
                FOLD_BASE_SHADOW_START_COLOR,
                FOLD_BASE_SHADOW_START_ALPHA,
                FOLD_BASE_SHADOW_END_COLOR,
                FOLD_BASE_SHADOW_END_ALPHA);
        */

    }


    /**
     * Set listener for page flip
     * <p>
     * Set a page flip listener to determine if page can flip forward or
     * backward
     * </p>
     *
     * @param listener a listener for page flip
     * @return self
     */
    public PageFlipModify setListener(OnPageFlipListener listener) {
        mListener = listener;
        return this;
    }

    /**
     * Sets pixels of each mesh
     * <p>The default value is 10 pixels for each mesh</p>
     *
     * @param pixelsOfMesh pixel amount of each mesh
     * @return self
     */
    public PageFlipModify setPixelsOfMesh(int pixelsOfMesh) {
        mPixelsOfMesh = pixelsOfMesh > 0 ? pixelsOfMesh :
                DEFAULT_MESH_VERTEX_PIXELS;

        for(int itrp = 0; itrp < PAGE_SIZE; itrp++)
        {
            mPages[itrp].mPixelsOfMesh = mPixelsOfMesh;
        }

        return this;
    }

    /**
     * Get pixels of mesh vertex
     *
     * @return pixels of each mesh:w
     */
    public int getPixelsOfMesh() {
        return mPixelsOfMesh;
    }


    /**
     * Set ratio of semi-perimeter of fold cylinder
     * <p>
     * When finger is clicking and moving on page, the page from touch point to
     * original point will be curled like as a cylinder, the radius of cylinder
     * is determined by line length from touch point to original point. You can
     * give a ratio of this line length to set cylinder radius, the default
     * value is 0.8
     * </p>
     *
     * @param ratio ratio of line length from touch point to original point. Its
     *              value is (0..1]
     * @return self
     */


    public PageFlipModify setSemiPerimeterRatio(float ratio) {
        if (ratio <= 0 || ratio > 1) {
            throw new IllegalArgumentException("Invalid ratio value: " + ratio);
        }

        //mSemiPerimeterRatio = ratio;
        return this;
    }

    /**
     * Set mask alpha for back of fold page
     * <p>Mask alpha will be invalid in double pages</p>
     *
     * @param alpha alpha value is in [0..255]
     * @return self
     */
    public PageFlipModify setMaskAlphaOfFold(int alpha) {
        //mFoldBackVertexes.setMaskAlpha(alpha);
        return this;
    }

    /**
     * Sets edge shadow color of fold page
     *
     * @param startColor shadow start color: [0..1]
     * @param startAlpha shadow start alpha: [0..1]
     * @param endColor shadow end color: [0..1]
     * @param endAlpha shadow end alpha: [0..1]
     * @return self
     */
    public PageFlipModify setShadowColorOfFoldEdges(float startColor,
                                              float startAlpha,
                                              float endColor,
                                              float endAlpha) {
        //mFoldEdgesShadow.mColor.set(startColor, startAlpha, endColor, endAlpha);
        return this;
    }

    /**
     * Sets base shadow color of fold page
     *
     * @param startColor shadow start color: [0..1]
     * @param startAlpha shadow start alpha: [0..1]
     * @param endColor shadow end color: [0..1]
     * @param endAlpha shadow end alpha: [0..1]
     * @return self
     */
    public PageFlipModify setShadowColorOfFoldBase(float startColor,
                                             float startAlpha,
                                             float endColor,
                                             float endAlpha) {
       // mFoldBaseShadow.mColor.set(startColor, startAlpha, endColor, endAlpha);
        return this;
    }

    /**
     * Set shadow width of fold edges
     *
     * @param min minimal width
     * @param max maximum width
     * @param ratio width ratio based on fold cylinder radius. It is in (0..1)
     * @return self
     */
    public PageFlipModify setShadowWidthOfFoldEdges(float min,
                                              float max,
                                              float ratio) {
        //mFoldEdgesShadowWidth.set(min, max, ratio);
        return this;
    }

    /**
     * Set shadow width of fold base
     *
     * @param min minimal width
     * @param max maximum width
     * @param ratio width ratio based on fold cylinder radius. It is in (0..1)
     * @return self
     */
    public PageFlipModify setShadowWidthOfFoldBase(float min,
                                             float max,
                                             float ratio) {
        //mFoldBaseShadowWidth.set(min, max, ratio);
        return this;
    }

    /**
     * Get surface width
     *
     * @return surface width
     */
    public int getSurfaceWidth() {
        return (int)mViewRect.surfaceW;
    }

    /**
     * Get surface height
     *
     * @return surface height
     */
    public int getSurfaceHeight() {
        return (int)mViewRect.surfaceH;
    }

    /**
     * Get page flip state
     *
     * @return page flip state
     */
    public PageFlipState getFlipState() {
        return mFlipState;
    }

    /**
     * Handle surface creation event
     *
     * @throws PageFlipException if failed to compile and link OpenGL shader
     */
    public void onSurfaceCreated() throws PageFlipException {
        glClearColor(0, 0, 0, 1f);
        glClearDepthf(1.0f);
        glEnable(GL_DEPTH_TEST);

        try {
            mVertexProgram.init(mContext);
            mFoldBackVertexProgram.init(mContext);
            mShadowVertexProgram.init(mContext);

            // create gradient shadow texture
            createGradientShadowTexture();
        }
        catch (PageFlipException e) {

            mVertexProgram.delete();
            mFoldBackVertexProgram.delete();
            mShadowVertexProgram.delete();
            throw e;
        }

    }

    /**
     * Handle surface changing event
     *
     * @param width surface width
     * @param height surface height
     * @throws PageFlipException if failed to compile and link OpenGL shader
     */
    public void onSurfaceChanged(int width, int height) throws
            PageFlipException {


        mViewRect.set(width, height);
        glViewport(0, 0, width, height);

        mVertexProgram.initMatrix(-mViewRect.halfW, mViewRect.halfW, -mViewRect.halfH, mViewRect.halfH);

        createPages();
        computeMaxMeshCount();

    }

    /**
     * Create pages
     */
    private void createPages() {

        for(int itrp = 0;  itrp < PAGE_SIZE; itrp++)
        {
            // release textures hold in pages
            if(mPages[itrp] != null) {
                mPages[itrp].deleteAllTextures();
            }

            //create new pages
            //test
            /*
            if(itrp == 0)
            {
                mPages[itrp] = new PageModify(-100, mViewRect.right,
                        mViewRect.top, mViewRect.bottom);

                mPages[itrp].mViewRect.set(mViewRect.width/2 + 100, mViewRect.height);
            }else
            {
                mPages[itrp] = new PageModify(mViewRect.left, 100,
                        mViewRect.top, mViewRect.bottom);

                mPages[itrp].mViewRect.set(mViewRect.width/2 + 100, mViewRect.height);
            }*/

            mPages[itrp] = new PageModify(mViewRect.left, mViewRect.right,
                    mViewRect.top, mViewRect.bottom);
            mPages[itrp].indexOfPage = itrp;
            mPages[itrp].mViewRect.set(mViewRect.width, mViewRect.height);

        }

        setSemiPerimeterRatio(0.8f);
        setShadowWidthOfFoldEdges(5, 60, 0.3f);
        setShadowWidthOfFoldBase(5, 80, 0.4f);
        int pixelsOfMesh = 10;
        setPixelsOfMesh(pixelsOfMesh);
    }

    /**
     * Handle finger down event
     *
     * @param touchX x of finger down point
     * @param touchY y of finger down point
     */
    public void onFingerDown(float touchX, float touchY) {
        // covert to OpenGL coordinate
        touchX = mViewRect.toOpenGLX(touchX);
        touchY = mViewRect.toOpenGLY(touchY);

        // check if touch point is contained in page?
        //the contain function is nice
        boolean isContained = false;
        if (mPages[FIRST_PAGE].contains(touchX, touchY)) {
            isContained = true;
        }

        // point is contained, ready to flip
        if (isContained) {
            //mMaxT2OAngleTan = 0f;
            //mMaxT2DAngleTan = 0f;
            mLastTouchP.set(touchX, touchY);
            mStartTouchP.set(touchX, touchY);
            mTouchP.set(touchX, touchY);

            mFlipState = PageFlipState.BEGIN_FLIP;

            for(int itrp = 0; itrp < PAGE_SIZE; itrp++)
            {
                mPages[itrp].mFakeTouchP.set(mTouchP.x, mTouchP.y);
            }

            //mFlipState = PageFlipState.BEGIN_TRANSLATE;  //get ready to translate

        }
    }

    /**
     * Handle finger moving event
     *
     * this is set the drawing state, but not drawing
     * @param touchX x of finger moving point
     * @param touchY y of finger moving point
     * @return true if moving will trigger to draw a new frame for page flip,
     *         False means the movement should be ignored.
     */
    public boolean onFingerMove(float touchX, float touchY) {
        touchX = mViewRect.toOpenGLX(touchX);
        touchY = mViewRect.toOpenGLY(touchY);

        float dy = (touchY - mStartTouchP.y);
        float dx = (touchX - mStartTouchP.x);

        //Log.d(TAG, "dx " + dx + ", dy " + dy);



        //final PageModify page = mPages[FIRST_PAGE];
        //final GLPoint originP = page.originP;
        //final GLPoint diagonalP = page.diagonalP;

        /*
        //begin translating
        if(mFlipState == PageFlipState.BEGIN_TRANSLATE &&
                (Math.abs(dx) > mViewRect.width * 0.05f))  //finger move threshold
        {
            //is ready to translate
            page.setOriginAndDiagonalPoints(1); //set bottom left as original point,, for now

            // determine if it is moving backward or forward[
            if(dx < 0 && originP.x > 0 || dx > 0 && originP.x < 0){
                //has to be moving from right to left
                mFlipState = PageFlipState.FORWARD_TRANSLATE;
            }
        }

        //in translating
        if(mFlipState == PageFlipState.FORWARD_TRANSLATE)
        {
            //using sequential
            dy = (touchY - mLastTouchP.y);
            dx = (touchX - mLastTouchP.x);

            if(dx <= 0)
            {
                //set the value of translate offset, used for drawing process
                //mTransOffX = dx * 1.2f;   //has to be negative
                for(int itrp = 0; itrp < PAGE_SIZE; itrp++)
                {
                    //mTransOffX[itrp] = dx * (1.2f - 0.1f * itrp);
                    mPages[itrp].mTransOffX = dx * (1.2f - 0.1f * itrp);
                }

                mLastTouchP.set(touchX, touchY);
                return true;
            }else if(Math.abs(dx) > mViewRect.width * 0.05f)
            {
                //switch to flip animation
                setAutoFlip();

            }

        }

        */

        // begin to flip
        if (mFlipState == PageFlipState.BEGIN_FLIP &&
                (Math.abs(dx) > mViewRect.width * 0.05f)) {

            for (int itrp = 0; itrp < PAGE_SIZE; itrp++) {
                PageModify page = mPages[itrp];
                GLPoint originP = page.originP;
                GLPoint diagonalP = page.diagonalP;

                // set OriginP and DiagonalP points
                page.setOriginAndDiagonalPoints(dy);
                // compute max degree between X axis and line from TouchP to OriginP
                // and max degree between X axis and line from TouchP to
                // (OriginP.x, DiagonalP.Y)
                float y2o = Math.abs(mStartTouchP.y - originP.y);
                float y2d = Math.abs(mStartTouchP.y - diagonalP.y);
                page.mMaxT2OAngleTan = page.computeTanOfCurlAngle(y2o);
                page.mMaxT2DAngleTan = page.computeTanOfCurlAngle(y2d);

                // moving at the top and bottom screen have different tan value of
                // angle
                if ((originP.y < 0 && page.right > 0) ||
                        (originP.y > 0 && page.right <= 0)) {
                    page.mMaxT2OAngleTan = -page.mMaxT2OAngleTan;
                } else {
                    page.mMaxT2DAngleTan = -page.mMaxT2DAngleTan;
                }

                if (itrp == FIRST_PAGE)  //first page
                {
                    // determine if it is moving backward or forward
                    if (dx > 0 &&
                            mListener != null &&
                            mListener.canFlipBackward()) {
                        mStartTouchP.x = originP.x;
                        dx = (touchX - mStartTouchP.x);
                        mFlipState = PageFlipState.BACKWARD_FLIP;
                        //Log.d(TAG, "back FLIP");
                    } else if (mListener != null &&
                            mListener.canFlipForward() &&
                            (dx < 0 && originP.x > 0 || dx > 0 && originP.x < 0)) {
                        mFlipState = PageFlipState.FORWARD_FLIP;
                        //Log.d(TAG, "forward FLIP");
                    }
                }

            } // end of for loop
        }


         // in moving, compute the TouchXY
         if (mFlipState == PageFlipState.FORWARD_FLIP ||
                 mFlipState == PageFlipState.BACKWARD_FLIP ||
                 mFlipState == PageFlipState.RESTORE_FLIP) {

             mIsVertical = Math.abs(dy) <= 1f;
             // multiply a factor to make sure the touch point is always head of
             // finger point


             for (int itrp = 0; itrp < PAGE_SIZE; itrp++) {

                 //differentiate dx
                 dx = (touchX * (float)Math.pow(0.9, itrp) - mStartTouchP.x);  //forwards or backwards
                 dy = (touchY - mStartTouchP.y);  //upwards or downwards

                 if (PageFlipState.FORWARD_FLIP == mFlipState) {
                     dx *= 1.2f;
                 }
                 else {
                     dx *= 1.1f;
                 }

                 PageModify page = mPages[itrp];
                 GLPoint originP = page.originP;
                 GLPoint diagonalP = page.diagonalP;

                 // moving direction is changed:
                 // 1. invert max curling angle
                 // 2. invert Y of original point and diagonal point
                 if ((dy < 0 && originP.y < 0) || (dy > 0 && originP.y > 0)) {
                     float t = page.mMaxT2DAngleTan;
                     page.mMaxT2DAngleTan = page.mMaxT2OAngleTan;
                     page.mMaxT2OAngleTan = t;
                     page.invertYOfOriginPoint();
                 }

                 // compute new TouchP.y
                 float maxY = dx * page.mMaxT2OAngleTan;
                 if (Math.abs(dy) > Math.abs(maxY)) {
                     dy = maxY;
                 }

                 // check if XFoldX1 is outside page width, if yes, recompute new
                 // TouchP.y to assure the XFoldX1 is in page width
                 float t2oK = dy / dx;
                 float xTouchX = dx + dy * t2oK;
                 float xRatio = (1 + page.mSemiPerimeterRatio) * 0.5f;
                 float xFoldX1 = xRatio * xTouchX;
                 if (Math.abs(xFoldX1) + 2 >= page.width) {
                     float dy2 = ((diagonalP.x - originP.x) / xRatio - dx) * dx;
                     // ignore current moving if we can't get a valid dy, for example
                     // , in double pages mode, when finger is moving from the one
                     // page to another page, the dy2 is negative and should be
                     // ignored
                     if (dy2 < 0) {
                         return false;
                     }

                     double t = Math.sqrt(dy2);
                     if (originP.y > 0) {
                         t = -t;
                         dy = (int)Math.ceil(t);
                     }
                     else {
                         dy = (int)Math.floor(t);
                     }
                 }

                 // set touchP(x, y) and middleP(x, y)
                 if(itrp == FIRST_PAGE)
                 {
                     mLastTouchP.set(dx + originP.x, dy + originP.y);  //used to store the value temporarily
                 }
                 mTouchP.set(dx + originP.x, dy + originP.y);
                 page.mFakeTouchP.set(mTouchP.x, mTouchP.y);
                 page.mMiddleP.x = (mTouchP.x + originP.x) * 0.5f;
                 page.mMiddleP.y = (mTouchP.y + originP.y) * 0.5f;

             }  //end of for loop

             mTouchP.set(mLastTouchP.x, mLastTouchP.y);
             mLastTouchP.set(touchX, touchY);

             // continue to compute points to drawing flip
             computeVertexesAndBuildPage();
             return true;

         }

        return false;
    }

    /**
     * Setup for auto flip
     * now its forward flip
     */
    public void setAutoFlip()
    {
        final PageModify page = mPages[FIRST_PAGE];
        onFingerDown(mStartTouchP.x, mStartTouchP.y);
        mFlipState = PageFlipState.BEGIN_FLIP;

        //trigger the finger up event from the MainActivity
        MainActivity.getSharedInstance().mPageFlipView.autoFingerUp(mStartTouchP.x, mStartTouchP.y);

    }

    /**
     * Handle finger up event
     *
     * @param touchX x of finger moving point
     * @param touchY y of finger moving point
     * @param duration millisecond for page flip animation
     * @return true if animation is started or animation is not triggered
     */
    public boolean onFingerUp(float touchX, float touchY, int duration) {
        touchX = mViewRect.toOpenGLX(touchX);
        touchY = mViewRect.toOpenGLY(touchY);

        final PageModify page = mPages[FIRST_PAGE];
        final GLPoint originP = page.originP;
        final GLPoint diagonalP = page.diagonalP;

        Point start = new Point((int)mTouchP.x, (int)mTouchP.y);
        Point end = new Point(0, 0);

        // forward flipping
        if (mFlipState == PageFlipState.FORWARD_FLIP) {
            // can't go forward, restore current page
            if (page.isXInRange(touchX, WIDTH_RATIO_OF_RESTORE_FLIP)) {
                end.x = (int)originP.x;
                mFlipState = PageFlipState.RESTORE_FLIP;
            }
            else if (originP.x < 0) {
                end.x = (int)(diagonalP.x + page.width);
            }
            else {
                end.x = (int)(diagonalP.x - page.width);
            }
            end.y = (int)(originP.y);
        }
        // backward flipping
        else if (mFlipState == PageFlipState.BACKWARD_FLIP) {
            // if not over middle x, change from backward to forward to restore
            if (!page.isXInRange(touchX, 0.5f)) {
                mFlipState = PageFlipState.FORWARD_FLIP;
                end.set((int)(diagonalP.x - page.width), (int)originP.y);
            }
            else {
                page.mMaxT2OAngleTan = (mTouchP.y - originP.y) /
                        (mTouchP.x - originP.x);
                end.set((int) originP.x, (int) originP.y);
            }
        }
        // ready to flip
        else if (mFlipState == PageFlipState.BEGIN_FLIP) {
            mIsVertical = false;
            mFlipState = PageFlipState.END_FLIP;
            page.setOriginAndDiagonalPoints(-touchY);

            // if enable clicking to flip, compute scroller points for animation
            if (mIsClickToFlip && Math.abs(touchX - mStartTouchP.x) < 2) {
                computeScrollPointsForClickingFlip(touchX, start, end);

            }
        }

        // start scroller for animating
        if (mFlipState == PageFlipState.FORWARD_FLIP ||
                mFlipState == PageFlipState.BACKWARD_FLIP ||
                mFlipState == PageFlipState.RESTORE_FLIP) {
            mScroller.startScroll(start.x, start.y,
                    end.x - start.x, end.y - start.y,
                    duration);
            return true;
        }

        return false;
    }



    /**
     * Check finger point to see if it can trigger a flip animation
     *
     * @param touchX x of finger point
     * @param touchY y of finger point
     * @return true if the point can trigger a flip animation
     */
    public boolean canAnimate(float touchX, float touchY) {

        //the finger is outside of the view

        return (mFlipState == PageFlipState.FORWARD_FLIP &&
                !mPages[FIRST_PAGE].contains(mViewRect.toOpenGLX(touchX),
                        mViewRect.toOpenGLY(touchY)));
    }

    /**
     * Compute scroller points for animating
     *
     * @param x x of clicking point
     * @param start start point of scroller will be set
     * @param end end point of scroller will be set
     */


    private void computeScrollPointsForClickingFlip(float x,
                                                    Point start,
                                                    Point end) {
        PageModify page = mPages[FIRST_PAGE];
        GLPoint originP = page.originP;
        GLPoint diagonalP = page.diagonalP;

        // forward and backward flip have different degree
        float tanOfForwardAngle = page.MAX_TAN_OF_FORWARD_FLIP;
        float tanOfBackwardAngle = page.MAX_TAN_OF_BACKWARD_FLIP;
        if ((originP.y < 0 && originP.x > 0) ||
                (originP.y > 0 && originP.x < 0)) {
            tanOfForwardAngle = -tanOfForwardAngle;
            tanOfBackwardAngle = -tanOfBackwardAngle;
        }

        // backward flip
        if (
                x < diagonalP.x + page.width * mWidthRationOfClickToFlip &&
                mListener != null &&
                mListener.canFlipBackward()) {
            mFlipState = PageFlipState.BACKWARD_FLIP;
            page.mKValue = tanOfBackwardAngle;
            start.set((int)diagonalP.x,
                    (int)(originP.y + (start.x - originP.x) * page.mKValue));
            end.set((int)originP.x - 5, (int)originP.y);
        }
        // forward flip
        else if (mListener != null &&
                mListener.canFlipForward() &&
                page.isXInRange(x, mWidthRationOfClickToFlip)) {
            mFlipState = PageFlipState.FORWARD_FLIP;
            page.mKValue = tanOfForwardAngle;

            // compute start.x
            if (originP.x < 0) {
                start.x = (int)(originP.x + page.width * 0.25f);
            }
            else {
                start.x = (int)(originP.x - page.width * 0.25f);
            }

            // compute start.y
            start.y = (int)(originP.y + (start.x - originP.x) * page.mKValue);

            // compute end.x
            // left page in double page mode
            if (originP.x < 0) {
                end.x = (int)(diagonalP.x + page.width);
            }
            // right page in double page mode
            else {
                end.x = (int)(diagonalP.x - page.width);
            }
            end.y = (int)(originP.y);
        }
    }



    /**
     * Compute animating and check if it can continue
     *
     * @return true animating is continue or it is stopped
     */
    public boolean animating() {

        final PageModify page = mPages[FIRST_PAGE];
        final GLPoint originP = page.originP;
        final GLPoint diagonalP = page.diagonalP;

        // is to end animating?
        boolean isAnimating = !mScroller.isFinished();
        if (isAnimating) {
            // get new (x, y)
            mScroller.computeScrollOffset();
            mTouchP.set(mScroller.getCurrX(), mScroller.getCurrY());
            page.mFakeTouchP.set(mTouchP.x, mTouchP.y);

            // for backward and restore flip, compute x to check if it can
            // continue to flip
            if (mFlipState == PageFlipState.BACKWARD_FLIP ||
                    mFlipState == PageFlipState.RESTORE_FLIP) {
                mTouchP.y = (mTouchP.x - originP.x) * page.mKValue + originP.y;
                page.mFakeTouchP.y = mTouchP.y;
                isAnimating = Math.abs(mTouchP.x - originP.x) > 10;
            }
            // check if flip is vertical
            else {
                mIsVertical = Math.abs(mTouchP.y - originP.y) < 1f;
            }

            // compute middle point
            page.mMiddleP.set((mTouchP.x + originP.x) * 0.5f,
                    (mTouchP.y + originP.y) * 0.5f);

            // compute key points
            if (mIsVertical) {
                page.computeKeyVertexesWhenVertical();
            }
            else {
                page.computeKeyVertexesWhenSlope();
            }

            // in single page mode, check if the whole fold page is outside the
            // screen and animating should be stopped
            if (mFlipState == PageFlipState.FORWARD_FLIP) {
                float r = (float)(page.mLenOfTouchOrigin * page.mSemiPerimeterRatio /
                        Math.PI);
                float x = (page.mYFoldP1c.y - diagonalP.y) * page.mKValue + r;
                isAnimating = x > (diagonalP.x - originP.x);
            }
        }

        // animation is stopped
        if (!isAnimating) {
            abortAnimating();
        }
        // continue animation and compute vertexes
        else if (mIsVertical) {
            page.computeVertexesWhenVertical();
        }
        else {
            page.computeVertexesWhenSlope();
        }

        return isAnimating;
    }

    /**
     * Is animating ?
     *
     * @return true if page is flipping
     */
    public boolean isAnimating() {
        return !mScroller.isFinished();
    }

    /**
     * Abort animating
     */
    public void abortAnimating() {
        mScroller.abortAnimation();
        if (mFlipState == PageFlipState.FORWARD_FLIP) {
            mFlipState = PageFlipState.END_WITH_FORWARD;
        }
        else if (mFlipState == PageFlipState.BACKWARD_FLIP) {
            mFlipState = PageFlipState.END_WITH_BACKWARD;
        }
        else if (mFlipState == PageFlipState.RESTORE_FLIP) {
            mFlipState = PageFlipState.END_WITH_RESTORE;
        }
    }

    /**
     * Is animation stated?
     *
     * @return true if flip is started
     */
    public boolean isStartedFlip() {
        return mFlipState == PageFlipState.BACKWARD_FLIP ||
                mFlipState == PageFlipState.FORWARD_FLIP ||
                mFlipState == PageFlipState.RESTORE_FLIP;
    }

    /**
     * The moving is ended?
     *
     * @return true if flip is ended
     */
    public boolean isEndedFlip() {
        return mFlipState == PageFlipState.END_FLIP ||
                mFlipState == PageFlipState.END_WITH_RESTORE ||
                mFlipState == PageFlipState.END_WITH_BACKWARD ||
                mFlipState == PageFlipState.END_WITH_FORWARD;
    }

    /**
     * Get the first page
     * First page is currently operating page which means it is the page user
     * finger is clicking or moving
     *
     * @return flip state, See {@link PageFlipState}
     */
    public PageModify getFirstPage() {
        return mPages[FIRST_PAGE];
    }

    public PageModify[] getPages(){
        return mPages;
    }


    /**
     * Delete unused textures
     */
    public void deleteUnusedTextures() {
        mPages[FIRST_PAGE].deleteUnusedTextures();
    }

    /**
     * Draw flipping frame
     */
    public void drawFlipFrame() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //make it for each page
        //the drawing order is very important

        // 1. draw back of fold page
        glUseProgram(mFoldBackVertexProgram.mProgramRef);
        glActiveTexture(GL_TEXTURE0);
        for(int itrp= PAGE_SIZE - 1; itrp >= 0; itrp--) {
            mPages[itrp].mFoldBackVertexes.draw(mFoldBackVertexProgram,
                    mPages[itrp],
                    mGradientShadowTextureID);
        }

        // 2. draw unfold page and front of fold page
        //z is set to 0
        glUseProgram(mVertexProgram.mProgramRef);
        glActiveTexture(GL_TEXTURE0);
        for(int itrp=0; itrp<PAGE_SIZE; itrp++) {
            mPages[itrp].drawFrontPage(mVertexProgram,
                    mPages[itrp].mFoldFrontVertexes);
        }

        // 3. draw edge and base shadow of fold parts
        glUseProgram(mShadowVertexProgram.mProgramRef);
        for(int itrp= PAGE_SIZE - 1; itrp>=0; itrp--) {

            mPages[itrp].mFoldBaseShadow.draw(mShadowVertexProgram);
            mPages[itrp].mFoldEdgesShadow.draw(mShadowVertexProgram);
        }
    }

    /**
     * Draw frame with full page
     */
    public void drawPageFrame() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(mVertexProgram.mProgramRef);
        glActiveTexture(GL_TEXTURE0);

        for(int itrp=0; itrp<PAGE_SIZE; itrp++)
        //for(int itrp= PAGE_SIZE - 1; itrp>=0; itrp--)
        {
            //Matrix.translateM(mVertexProgram.MVPMatrix, 0, -50.0f, 0.0f, 0.0f);

            glUniformMatrix4fv(mVertexProgram.mMVPMatrixLoc, 1, false, mVertexProgram.MVPMatrix, 0);
            mPages[itrp].drawFullPage(mVertexProgram);
        }
    }

    /**
     * Draw frame with position translated page
     */
    public void drawTranslateFrame(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        /*
        for(int itrp = 2; itrp < PAGE_SIZE; itrp++)
        {
            glUseProgram( mPages[itrp].mVertexProgram.mProgramRef);

            Matrix.translateM(mPages[itrp].mVertexProgram.MVPMatrix, 0, mPages[itrp].mTransOffX, 0.0f, 0.0f);
            glUniformMatrix4fv(mPages[itrp].mVertexProgram.mMVPMatrixLoc, 1, false,
                    mPages[itrp].mVertexProgram.MVPMatrix, 0);

            glActiveTexture(GL_TEXTURE0);
            mPages[itrp].drawFullPage(mPages[itrp].mVertexProgram);

        }*/

    }

    /**
     * Draw frame with roated frame
     */
    public void drawRotateFrame()
    {

    }

    /**
     * Draw frame with zoomed frame
     */
    public void drawZoomFrame()
    {

    }


    /**
     * Compute max mesh count and allocate vertexes buffer
     */
    private void computeMaxMeshCount() {
        // compute max mesh count
        int maxMeshCount = (int)mViewRect.minOfWH() / mPixelsOfMesh;

        // make sure the vertex count is even number
        if (maxMeshCount % 2 != 0) {
            maxMeshCount++;
        }

        // init vertexes buffers
        for(int itrp = 0; itrp < PAGE_SIZE; itrp++)
        {
            mPages[itrp].mFoldBackVertexes.set(maxMeshCount + 2);
            mPages[itrp].mFoldFrontVertexes.set((maxMeshCount << 1) + 8, 3, true);
            mPages[itrp].mFoldEdgesShadow.set(maxMeshCount + 2);
            mPages[itrp].mFoldBaseShadow.set(maxMeshCount + 2);
        }
    }

    /**
     * Create gradient shadow texture for lighting effect
     */
    private void createGradientShadowTexture() {
        int textureIDs[] = new int[1];
        glGenTextures(1, textureIDs, 0);
        glActiveTexture(GL_TEXTURE0);
        mGradientShadowTextureID = textureIDs[0];

        // gradient shadow texture
        Bitmap shadow = PageFlipUtils.createGradientBitmap();
        glBindTexture(GL_TEXTURE_2D, mGradientShadowTextureID);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, shadow, 0);
        shadow.recycle();
    }

    /**
     * Compute vertexes of page
     */
    private void computeVertexesAndBuildPage() {
        //Log.d(TAG, "called");

        for(int itrp = 0; itrp < PAGE_SIZE; itrp++)
        {
            if (mIsVertical) {
                mPages[itrp].computeKeyVertexesWhenVertical();
                mPages[itrp].computeVertexesWhenVertical();
            }
            else {
                mPages[itrp].computeKeyVertexesWhenSlope();
                mPages[itrp].computeVertexesWhenSlope();
            }
        }


    }

}

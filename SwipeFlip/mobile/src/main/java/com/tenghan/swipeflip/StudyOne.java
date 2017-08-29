package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.PointF;
import android.util.Log;

import com.eschao.android.widget.pageflip.GLPoint;
import com.eschao.android.widget.pageflip.PageFlipState;
import com.eschao.android.widget.pageflip.modify.PageModify;

import java.util.ArrayList;

/**
 * Created by hanteng on 2017-08-28.
 * Used for Study 1
 */

public class StudyOne extends PageFlipModifyAbstract{

    private final static String TAG = "Study One";
    private final static int pageSize = 2;

    //conditions
    private ArrayList<Integer> testingCorner;
    private int cornerCount;
    private ArrayList<Integer> testingAngleSeperation;
    private int angleCount;
    private ArrayList<Integer> testingDistanceSerperation;
    private int distanceCount;

    public ArrayList<int[]> conditions;
    public int currentCondition;


    public StudyOne(Context context)
    {
        super(context, pageSize);

        testingAngleSeperation = new ArrayList<Integer>();
        testingCorner = new ArrayList<Integer>();
        testingDistanceSerperation = new ArrayList<Integer>();

        //set study conditions
        testingAngleSeperation.add(2);
        testingAngleSeperation.add(3);
        testingAngleSeperation.add(4);
        testingAngleSeperation.add(5);
        angleCount = 4;

        testingDistanceSerperation.add(3);
        testingDistanceSerperation.add(4);
        testingDistanceSerperation.add(5);
        testingDistanceSerperation.add(6);
        distanceCount = 4;

        /**
         *    ---------
         *   |0       1|
         *   |         |
         *   |         |
         *   |3       2|
         *    ---------
         */
        testingCorner.add(0);
        testingCorner.add(1);
        testingCorner.add(2);
        testingCorner.add(3);
        cornerCount = 4;

        conditions = new ArrayList<int[]>();
        for(int itrc = 0; itrc < cornerCount; itrc++)
        {
            for(int itra = 0; itra < angleCount; itra++)
            {
                for(int itrt = 0; itrt < distanceCount; itrt++)
                {
                    conditions.add(new int[]{testingCorner.get(itrc), testingAngleSeperation.get(itra), testingDistanceSerperation.get(itrt)});
                }
            }
        }

        currentCondition = -1;

    }

    public int[] obtainNextCondition()
    {
        currentCondition++;

        if(currentCondition == conditions.size())
        {
            //end the test
            currentCondition = 0;
        }

        return conditions.get(currentCondition);
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

        // begin to flip
        if (mFlipState == PageFlipState.BEGIN_FLIP
            // && (Math.abs(dx) > mViewRect.width * 0.05f)
                ) {

            //for (int itrp = 0; itrp < PAGE_SIZE; itrp++) {
            PageModify page = mPages[currentPageLock];
            GLPoint originP = page.originP;
            GLPoint diagonalP = page.diagonalP;

            // set OriginP and DiagonalP points
            //page.setOriginAndDiagonalPoints(dy);
            page.setOriginAndDiagonalPoints(dx, dy, mStartTouchP.x, mStartTouchP.y);

            //clear the variables
            MainActivity.getSharedInstance().mGestureService.reset();
            //starting from one page
            singlePageMode = true;
            //set origin
            MainActivity.getSharedInstance().mGestureService.setOrigin(new float[]{touchX, touchY});
            MainActivity.getSharedInstance().mGestureService.handleData(new float[]{touchX, touchY});

            //reload the first page texture
            MainActivity.getSharedInstance().mStudyView.mPageRender.ReloadFirstPageTexture();


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

            if (0 == FIRST_PAGE)  //first page
            {
                // determine if it is moving backward or forward

                if(Math.abs(dx) > Math.abs(dy))
                {
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
                }else
                {
                    if(mListener != null)
                    {
                        mFlipState = PageFlipState.UPWARD_FLIP;
                    }
                }


            }

            //} // end of for loop
        }

        // in moving, compute the TouchXY
        if (mFlipState == PageFlipState.FORWARD_FLIP ||
                mFlipState == PageFlipState.BACKWARD_FLIP ||
                mFlipState == PageFlipState.UPWARD_FLIP ||
                mFlipState == PageFlipState.RESTORE_FLIP) {

            MainActivity.getSharedInstance().mGestureService.handleData(new float[]{touchX, touchY});

            //temporary solution
            if(Math.abs(dy) <= 0.1f)
                dy = dy > 0 ? 0.11f : -0.11f;
            if(Math.abs(dx) <= 0.1f)
                dx = dx > 0 ? 0.11f : -0.11f;

            mIsVertical = Math.abs(dy) <= 0.1f;
            if(mIsVertical == false)
            {
                //Log.d(TAG, "abs dx " + Math.abs(dx));
                mIsHorizontal = Math.abs(dx) <= 0.1f;
            }
            //skip the calculation when the flip is either horizontal or vertical
            if(mIsHorizontal || mIsVertical) {
                Log.d(TAG, "skipping a frame ..............");
                return false;
            }
            // multiply a factor to make sure the touch point is always head of
            // finger point

            //there could be two conditions
            //second page flip only
            //all previous page flip togeter

            if(singlePageMode)
            {
                dx = (touchX - mStartTouchP.x) * (float)Math.pow(TOUCH_DIFF_COEFFICIENT, FIRST_PAGE);  //forwards or backwards
                dy = (touchY - mStartTouchP.y) * (float)Math.pow(TOUCH_DIFF_COEFFICIENT, FIRST_PAGE);  //upwards or downwards

                if (PageFlipState.FORWARD_FLIP == mFlipState || PageFlipState.BACKWARD_FLIP == mFlipState
                        || PageFlipState.UPWARD_FLIP == mFlipState) {
                    dx *= 1.2f;
                    dy *= 1.2f;
                }
                else {
                    dx *= 1.1f;
                    dy *= 1.1f;
                }

                PageModify page = mPages[currentPageLock];
                GLPoint originP = page.originP;
                GLPoint diagonalP = page.diagonalP;

                // moving direction is changed:
                // 1. invert max curling angle
                // 2. invert Y of original point and diagonal point

                if(mFlipState == PageFlipState.FORWARD_FLIP || mFlipState == PageFlipState.BACKWARD_FLIP)
                {
                    if ((dy < 0 && originP.y < 0) || (dy > 0 && originP.y > 0)
                            ) {
                        float t = page.mMaxT2DAngleTan;
                        page.mMaxT2DAngleTan = page.mMaxT2OAngleTan;
                        page.mMaxT2OAngleTan = t;
                        page.invertYOfOriginPoint();
                    }
                }else if(mFlipState == PageFlipState.UPWARD_FLIP)
                {
                    if((dx < 0 && originP.x < 0) || (dx > 0 && originP.x > 0))
                    {
                        //invert the x of original point and diagonal point
                        page.invertXOfOriginPoint();
                    }
                }

                // set touchP(x, y) and middleP(x, y)
                if(0 == FIRST_PAGE)
                {
                    mLastTouchP.set(dx + originP.x, dy + originP.y);  //used to store the value temporarily
                }
                mTouchP.set(dx + originP.x, dy + originP.y);

                //Log.d(TAG, "origin point" + originP.x);
                //Log.d(TAG, "moving touch point " + mTouchP.x);

                page.mFakeTouchP.set(mTouchP.x, mTouchP.y);
                page.mMiddleP.x = (mTouchP.x + originP.x) * 0.5f;
                page.mMiddleP.y = (mTouchP.y + originP.y) * 0.5f;


                //detect whether the back page has reached front page
                float travelDis = calDistance(dx, dy);
                if(travelDis > maxTravelDis)
                {
                    maxTravelDis = travelDis;
                    //Log.d(TAG, "max travel " + maxTravelDis);

                    singlePageMode = false;
                }

            }else
            {
                for (int itrp = 0; itrp < currentPageLock + 1; itrp++) {

                    //differentiate dx
                    //the 0.9 can be adjusted
                    dx = (touchX - mStartTouchP.x) * (float)Math.pow(TOUCH_DIFF_COEFFICIENT, itrp);  //forwards or backwards
                    dy = (touchY - mStartTouchP.y) * (float)Math.pow(TOUCH_DIFF_COEFFICIENT, itrp);  //upwards or downwards

                    if (PageFlipState.FORWARD_FLIP == mFlipState || PageFlipState.BACKWARD_FLIP == mFlipState
                            || PageFlipState.UPWARD_FLIP == mFlipState) {
                        dx *= 1.2f;
                        dy *= 1.2f;
                    }
                    else {
                        dx *= 1.1f;
                        dy *= 1.1f;
                    }

                    PageModify page = mPages[itrp];
                    GLPoint originP = page.originP;
                    GLPoint diagonalP = page.diagonalP;

                    if(mFlipState == PageFlipState.FORWARD_FLIP || mFlipState == PageFlipState.BACKWARD_FLIP)
                    {
                        if ((dy < 0 && originP.y < 0) || (dy > 0 && originP.y > 0)
                                ) {
                            float t = page.mMaxT2DAngleTan;
                            page.mMaxT2DAngleTan = page.mMaxT2OAngleTan;
                            page.mMaxT2OAngleTan = t;
                            page.invertYOfOriginPoint();
                        }
                    }else if(mFlipState == PageFlipState.UPWARD_FLIP)
                    {
                        if((dx < 0 && originP.x < 0) || (dx > 0 && originP.x > 0))
                        {
                            //invert the x of original point and diagonal point
                            page.invertXOfOriginPoint();
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

                    if(itrp == FIRST_PAGE)
                    {
                        //detect whether the back page has reached front page
                        float travelDis = calDistance(dx, dy);
                        if(travelDis > maxTravelDis)
                        {
                            maxTravelDis = travelDis;

                            //Log.d(TAG, "new max travel " + maxTravelDis);
                        }
                    }

                }  //end of for loop

            }

            mTouchP.set(mLastTouchP.x, mLastTouchP.y);
            mLastTouchP.set(touchX, touchY);

            // continue to compute points to drawing flip
            computeVertexesAndBuildPage();

            return true;

        }

        return false;
    }

    //calculate a distance from dx and dy
    private float calDistance(float disx, float disy)
    {
        return (float)Math.sqrt(disx * disx + disy * disy);
    }

    /**
     * Compute vertexes of page
     */
    public void computeVertexesAndBuildPage() {
        //Log.d(TAG, "called");

        if(singlePageMode)
        {
            mPages[currentPageLock].computeKeyVertexesWhenSlope();
            mPages[currentPageLock].computeVertexesWhenSlope();
        }else
        {
            for(int itrp = 0; itrp < (currentPageLock + 1); itrp++)
            {

                if (mIsVertical) {
                    //mPages[itrp].computeKeyVertexesWhenVertical();
                    //mPages[itrp].computeVertexesWhenVertical();
                }
                else if (mIsHorizontal)
                {
                    //mPages[itrp].computeKeyVertexesWhenHorizontal();
                    //mPages[itrp].computeVertexesWhenHorizontal();
                }
                else {
                    mPages[itrp].computeKeyVertexesWhenSlope();
                    mPages[itrp].computeVertexesWhenSlope();
                }

            }
        }

        //grab some key values of the page
        PointF xfoldpc = mPages[FIRST_PAGE].mXFoldPcR;
        PointF yfoldpc = mPages[FIRST_PAGE].mYFoldPcR;
        
        GLPoint originer = mPages[FIRST_PAGE].originP;
        PointF corner = mPages[FIRST_PAGE].mFakeTouchP;

        //translate to canvas cordinate
        MainActivity.getSharedInstance().mDemoUIView.peelOne.set(fromOpenGLX(xfoldpc.x), fromOpenGLY(xfoldpc.y));
        MainActivity.getSharedInstance().mDemoUIView.peelTwo.set(fromOpenGLX(yfoldpc.x), fromOpenGLY(yfoldpc.y));
        MainActivity.getSharedInstance().mDemoUIView.origin.set(fromOpenGLX(originer.x), fromOpenGLY(originer.y));
        MainActivity.getSharedInstance().mDemoUIView.corner.set(fromOpenGLX(corner.x), fromOpenGLY(corner.y));
        MainActivity.getSharedInstance().mDemoUIView.invalidate();

    }

    private float fromOpenGLX(float x) {
        return x + 160.0f;
    }

    private float fromOpenGLY(float y) {
        return 160.0f - y;
    }

}

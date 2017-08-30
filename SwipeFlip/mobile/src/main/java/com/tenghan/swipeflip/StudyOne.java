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
    private ArrayList<Integer> testingEdge;
    private int edgeCount;
    private ArrayList<Integer> testingAngleSeperation;
    private int angleCount;
    private ArrayList<Integer> testingDistanceSerperation;
    private int distanceCount;

    public ArrayList<int[]> conditions;
    public int currentCondition;

    //cursor position
    public PointF cursor = new PointF();


    public StudyOne(Context context)
    {
        super(context, pageSize);

        testingAngleSeperation = new ArrayList<Integer>();
        testingCorner = new ArrayList<Integer>();
        testingEdge = new ArrayList<Integer>();
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
         *        4
         *    ---------
         *   |0       1|
         *   |         |
         * 7 |         |  5
         *   |3       2|
         *    ---------
         *        6
         */
        testingCorner.add(0);
        testingCorner.add(1);
        testingCorner.add(2);
        testingCorner.add(3);
        cornerCount = 4;

        testingEdge.add(4);
        testingEdge.add(5);
        testingEdge.add(6);
        testingEdge.add(7);
        edgeCount = 4 ;

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

        for(int itrc = 0; itrc < edgeCount; itrc++)
        {
            for(int itrt = 0; itrt < distanceCount; itrt++)
            {
                conditions.add(new int[]{testingEdge.get(itrc), 1, testingDistanceSerperation.get(itrt)});
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

//        //translate to canvas cordinate
//        MainActivity.getSharedInstance().mDemoUIView.peelOne.set(fromOpenGLX(xfoldpc.x), fromOpenGLY(xfoldpc.y));
//        MainActivity.getSharedInstance().mDemoUIView.peelTwo.set(fromOpenGLX(yfoldpc.x), fromOpenGLY(yfoldpc.y));
//        MainActivity.getSharedInstance().mDemoUIView.origin.set(fromOpenGLX(originer.x), fromOpenGLY(originer.y));
//        MainActivity.getSharedInstance().mDemoUIView.corner.set(fromOpenGLX(corner.x), fromOpenGLY(corner.y));
//        MainActivity.getSharedInstance().mDemoUIView.invalidate();

        if(conditions.get(currentCondition)[0] < 4)
        {
            cursor = calIntersection(originer.x, originer.y, corner.x, corner.y,
                    xfoldpc.x, xfoldpc.y, yfoldpc.x, yfoldpc.y);
        }else
        {
            cursor = calMiddle(xfoldpc.x, xfoldpc.y, yfoldpc.x, yfoldpc.y);
        }


        MainActivity.getSharedInstance().mStudyView.mPageRender.selectedSegment(cursor);
    }

    private float fromOpenGLX(float x) {
        return x + 160.0f;
    }

    private float fromOpenGLY(float y) {
        return 160.0f - y;
    }

    //calcuate the intersection point of line 1-2 and 3-4
    private PointF calIntersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4)
    {
        PointF cross = new PointF();

        x1 = fromOpenGLX(x1);
        y1 = fromOpenGLY(y1);
        x2 = fromOpenGLX(x2);
        y2 = fromOpenGLY(y2);
        x3 = fromOpenGLX(x3);
        y3 = fromOpenGLY(y3);
        x4 = fromOpenGLX(x4);
        y4 = fromOpenGLY(y4);

        if(x1 == x2 || x3 == x4)
        {
            return cross;
        }

        //y = ax + b
        float a1 = (y2-y1) / (x2 - x1);
        float b1 = y1 - a1 * x1;
        float a2 = (y4 - y3) / (x4 - x3);
        float b2 = y3 - a2*x3;

        //parallel
        if(a1 == a2)
        {
            return cross;
        }

        float x0 = -(b1-b2) / (a1 - a2);

        if(Math.min(x1, x2) < x0 && x0 < Math.max(x1, x2) &&
                Math.min(x3, x4) < x0  && x0 < Math.max(x3, x4))
        {
            cross.set(x0, a1*x0 + b1);

            Log.d(TAG, "x " + cross.x + " , y " + cross.y);

            return cross;
        }else
        {
            return cross;
        }

    }

    //calculate the middle point of two peel
    private PointF calMiddle(float x1, float y1, float x2, float y2)
    {
        PointF middlep = new PointF();
        x1 = fromOpenGLX(x1);
        y1 = fromOpenGLY(y1);
        x2 = fromOpenGLX(x2);
        y2 = fromOpenGLY(y2);

        //it's not the middle point
        //float width = getSurfaceWidth();
        //float height = getSurfaceHeight();

        if(x1 == x2)
        {
            middlep.set(x1, (y1 + y2 )/ 2);
            return middlep;
        }


        float a = (y1 - y2) / (x1 - x2);
        float b = y1 - a * x1;


        if(conditions.get(currentCondition)[0] == 4 ||
                conditions.get(currentCondition)[0] == 6)
        {
            float xm = getSurfaceWidth() / 2;
            float ym = a * xm + b;

            middlep.set(xm, ym);


        }else if(conditions.get(currentCondition)[0] == 5 ||
                conditions.get(currentCondition)[0] == 7)
        {
            float xtop = (0 - b) / a;
            float xbot = (getSurfaceHeight() - b) / a;
            float xm = (xtop + xbot) / 2;
            float ym = a * xm + b;

            middlep.set(xm, ym);
        }



        return middlep;
    }

}

package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.eschao.android.widget.pageflip.GLPoint;
import com.eschao.android.widget.pageflip.PageFlipState;
import com.eschao.android.widget.pageflip.modify.PageModify;

/**
 * Created by hanteng on 2017-08-31.
 */

public class DemoNotification extends PageFlipModifyAbstract {

    private final static String TAG = "DemoNotification";

    private final static int pageSize = 2;

    private final static int FRONT_PAGE = 0;

    private PointF mAutoStartP;
    private PointF mAutoEndP;

    //task schedule
    Handler handler;

    private boolean isForwardingAutoFlip = true;

    public DemoNotification(Context context)
    {
        super(context, pageSize);

        mAutoStartP = new PointF();
        mAutoEndP = new PointF();

        Log.d(TAG, "time " + System.currentTimeMillis());

        //start a timer
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "" + System.currentTimeMillis());

                //start the
                //set origin
                PageModify page = mPages[FRONT_PAGE];

                //define the start and end point
                Point start = new Point();
                Point end = new Point();

                mIsVertical = false;
                mIsHorizontal = false;
                mFlipState = PageFlipState.END_FLIP;
                page.autoSetOriginAndDiagonalPoints();

                //activate
                computeScrollPointsForAutoFlip(true, start, end);
                mAutoStartP.set(start.x, start.y);
                mAutoEndP.set(end.x, end.y);

                if (mFlipState == PageFlipState.FORWARD_FLIP ||
                        mFlipState == PageFlipState.BACKWARD_FLIP ||
                        mFlipState == PageFlipState.UPWARD_FLIP ||
                        mFlipState == PageFlipState.RESTORE_FLIP) {

                    //activiate the job sheduling

                    Log.d(TAG, "scroll starts");
                    mScroller.startScroll(start.x, start.y,
                            end.x - start.x, end.y - start.y,
                            10000);

                    try {
                        MainActivity.getSharedInstance().mDemoView.mDrawLock.lock();
                        if (MainActivity.getSharedInstance().mDemoView.mPageRender != null &&
                                MainActivity.getSharedInstance().mDemoView.mPageRender.onAutoFlip() ) {

                            //indicating this is auto flip
                            flipType = 2;

                            MainActivity.getSharedInstance().mDemoView.requestRender();
                        }
                    }
                    finally {
                        MainActivity.getSharedInstance().mDemoView.mDrawLock.unlock();
                    }

                }
            }
        }, 5000);

    }


    public void computeScrollPointsForAutoFlip(boolean isForward, Point start, Point end)
    {
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
                !isForward &&
                        mListener != null &&
                        mListener.canFlipBackward()) {
            mFlipState = PageFlipState.BACKWARD_FLIP;
            page.mKValue = tanOfBackwardAngle;
            start.set((int)diagonalP.x,
                    (int)(originP.y + (start.x - originP.x) * page.mKValue));
            end.set((int)originP.x - 5, (int)originP.y);

            Log.d(TAG, "backward auto flip");

        }
        // forward flip
        else if (mListener != null &&
                mListener.canFlipForward() &&
                isForward) {
            mFlipState = PageFlipState.FORWARD_FLIP;
            page.mKValue = tanOfForwardAngle;


            Log.d(TAG, "forawrd auto flip");

            // compute start.x
            if (originP.x < 0) {
                start.x = (int)(originP.x + page.width * 0.05f);
            }
            else {
                start.x = (int)(originP.x - page.width * 0.05f);
            }

            // compute start.y
            start.y = (int)(originP.y + (start.x - originP.x) * page.mKValue);


            //this is to peel the whole page over

//            // compute end.x
//            // left page in double page mode
//            if (originP.x < 0) {
//                end.x = (int)(diagonalP.x + page.width);
//            }
//            // right page in double page mode
//            else {
//                end.x = (int)(diagonalP.x - page.width);
//            }
//            end.y = (int)(originP.y);
//

            //this is to peel just a corner

            if (originP.x < 0) {
                end.x = (int)(originP.x + page.width * 0.25f);
            }
            else {
                end.x = (int)(originP.x - page.width * 0.25f);
            }

            // compute start.y
            end.y = (int)(originP.y + (end.x - originP.x) * page.mKValue);

        }

    }

    public boolean onFingerMove(float touchX, float touchY)
    {
        //later
        touchX = mViewRect.toOpenGLX(touchX);
        touchY = mViewRect.toOpenGLY(touchY);

        float dy = (touchY - mStartTouchP.y);
        float dx = (touchX - mStartTouchP.x);


        if(flipType == 2)
        {
            //probably after the auto flip
            //there is auto start and end points
            //continue flip to move on, and reverse flip to cancel
            if(mFlipState == PageFlipState.BEGIN_FLIP
//                    && ((Math.abs(dx)  > mViewRect.width * 0.05f) ||
//                    (Math.abs(dy) > mViewRect.height * 0.05f))
                    )
            {
                PageModify page = mPages[FRONT_PAGE];
                GLPoint originP = page.originP;
                GLPoint diagonalP = page.diagonalP;

                //cause the page original and diagonal point are already set
                //check the original point and diagonal point to see if they are matched
                //if(!page.checkOriginAndDiagonalPoints(dx, dy, mStartTouchP.x, mStartTouchP.y))
                //{
                //    return false;
                //}

                //origin match, continue
                //clear the variables
                MainActivity.getSharedInstance().mGestureService.reset();
                //starting from one page
                singlePageMode = true;
                //set origin
                MainActivity.getSharedInstance().mGestureService.setOrigin(new float[]{touchX, touchY});
                MainActivity.getSharedInstance().mGestureService.handleData(new float[]{touchX, touchY});

                // compute max degree between X axis and line from TouchP to OriginP
                // and max degree between X axis and line from TouchP to
                // (OriginP.x, DiagonalP.Y)
                float y2o = Math.abs(mAutoStartP.y - originP.y);
                float y2d = Math.abs(mAutoStartP.y - diagonalP.y);
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

                //keep / resume the moving direction
               // if(mFlipState == )

//                if(Math.abs(dx) > Math.abs(dy))
//                {
//                    if (dx > 0 &&
//                            mListener != null &&
//                            mListener.canFlipBackward()) {
//                        mStartTouchP.x = originP.x;
//                        dx = (touchX - mStartTouchP.x);
//                        mFlipState = PageFlipState.BACKWARD_FLIP;
//                        //Log.d(TAG, "back FLIP");
//                    } else if (mListener != null &&
//                            mListener.canFlipForward() &&
//                            (dx < 0 && originP.x > 0 || dx > 0 && originP.x < 0)) {
//                        mFlipState = PageFlipState.FORWARD_FLIP;
//                        //Log.d(TAG, "forward FLIP");
//                    }
//                }else
//                {
//                    if(mListener != null)
//                    {
//                        mFlipState = PageFlipState.UPWARD_FLIP;
//                    }
//                }

                    mFlipState = PageFlipState.FORWARD_FLIP;


            }

            if (mFlipState == PageFlipState.FORWARD_FLIP ||
                    mFlipState == PageFlipState.BACKWARD_FLIP ||
                    mFlipState == PageFlipState.UPWARD_FLIP ||
                    mFlipState == PageFlipState.RESTORE_FLIP)
            {
                //compute the finger moving and flipping..
                //in the state of auto flip
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

                PageModify page = mPages[FRONT_PAGE];
                GLPoint originP = page.originP;
                GLPoint diagonalP = page.diagonalP;

                // moving direction is changed:
                // 1. invert max curling angle
                // 2. invert Y of original point and diagonal point

//                if(mFlipState == PageFlipState.FORWARD_FLIP || mFlipState == PageFlipState.BACKWARD_FLIP)
//                {
//                    if ((dy < 0 && originP.y < 0) || (dy > 0 && originP.y > 0)
//                            ) {
//                        float t = page.mMaxT2DAngleTan;
//                        page.mMaxT2DAngleTan = page.mMaxT2OAngleTan;
//                        page.mMaxT2OAngleTan = t;
//                        page.invertYOfOriginPoint();
//
//                    }
//                }else if(mFlipState == PageFlipState.UPWARD_FLIP)
//                {
//                    if((dx < 0 && originP.x < 0) || (dx > 0 && originP.x > 0))
//                    {
//                        //invert the x of original point and diagonal point
//                        page.invertXOfOriginPoint();
//                    }
//                }

                // set touchP(x, y) and middleP(x, y)

                mLastTouchP.set(dx + (mAutoEndP.x - mAutoStartP.x) + originP.x, dy + (mAutoEndP.y - mAutoStartP.y) + originP.y);  //used to store the value temporarily
                mTouchP.set(dx + (mAutoEndP.x - mAutoStartP.x) +  originP.x, dy + (mAutoEndP.y - mAutoStartP.y) + originP.y);

                //Log.d(TAG, "origin point" + originP.x);
                //Log.d(TAG, "moving touch point " + mTouchP.x);

                page.mFakeTouchP.set(mTouchP.x, mTouchP.y);
                page.mMiddleP.x = (mTouchP.x + originP.x) * 0.5f;
                page.mMiddleP.y = (mTouchP.y + originP.y) * 0.5f;

                mTouchP.set(mLastTouchP.x, mLastTouchP.y);
                mLastTouchP.set(touchX, touchY);

                // continue to compute points to drawing flip
                computeVertexesAndBuildPage();

                return true;

            }


        }else
        {
            // begin to flip
            if (mFlipState == PageFlipState.BEGIN_FLIP
                // && (Math.abs(dx) > mViewRect.width * 0.05f)
                    ) {

                //for (int itrp = 0; itrp < PAGE_SIZE; itrp++) {
                PageModify page = mPages[FRONT_PAGE];
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

                PageModify page = mPages[FRONT_PAGE];
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

                mLastTouchP.set(dx + originP.x, dy + originP.y);  //used to store the value temporarily
                mTouchP.set(dx + originP.x, dy + originP.y);

                //Log.d(TAG, "origin point" + originP.x);
                //Log.d(TAG, "moving touch point " + mTouchP.x);

                page.mFakeTouchP.set(mTouchP.x, mTouchP.y);
                page.mMiddleP.x = (mTouchP.x + originP.x) * 0.5f;
                page.mMiddleP.y = (mTouchP.y + originP.y) * 0.5f;

                mTouchP.set(mLastTouchP.x, mLastTouchP.y);
                mLastTouchP.set(touchX, touchY);

                // continue to compute points to drawing flip
                computeVertexesAndBuildPage();

                return true;

            }
        }




        return true;
    }

    public void computeVertexesAndBuildPage()
    {
        mPages[FRONT_PAGE].computeKeyVertexesWhenSlope();
        mPages[FRONT_PAGE].computeVertexesWhenSlope();
    }

    private float calDistance(float disx, float disy)
    {
        return (float)Math.sqrt(disx * disx + disy * disy);
    }
}

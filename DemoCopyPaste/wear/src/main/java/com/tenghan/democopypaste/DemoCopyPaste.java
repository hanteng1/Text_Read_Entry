package com.tenghan.democopypaste;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.eschao.android.widget.pageflip.GLPoint;
import com.eschao.android.widget.pageflip.PageFlipState;
import com.eschao.android.widget.pageflip.modify.PageModify;

/**
 * Created by hanteng on 2017-09-17.
 */

public class DemoCopyPaste extends PageFlipModifyAbstract{

    private final static String TAG = "DemoCopyPaste";

    private final static int pageSize = 2;

    private final static int FRONT_PAGE = 0;
    private final static int SECOND_PAGE = 1;

    public DemoCopyPaste(Context context)
    {
        super(context, pageSize);
    }


    public boolean onFingerMove(float touchX, float touchY)
    {
        //later
        touchX = mViewRect.toOpenGLX(touchX);
        touchY = mViewRect.toOpenGLY(touchY);

        float dy = (touchY - mStartTouchP.y);
        float dx = (touchX - mStartTouchP.x);


        // begin to flip
        if (mFlipState == PageFlipState.BEGIN_FLIP
                && (Math.abs(dx) > mViewRect.width * 0.02f)
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

            MainActivity.getSharedInstance().mDemoUIView.setVisibility(View.INVISIBLE);

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

        return false;
    }

    public void computeVertexesAndBuildPage(){
        mPages[FRONT_PAGE].computeKeyVertexesWhenSlope();
        mPages[FRONT_PAGE].computeVertexesWhenSlope();
    }
}

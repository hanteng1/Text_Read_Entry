package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Point;
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



    //task schedule
    Handler handler;

    public DemoNotification(Context context)
    {
        super(context, pageSize);

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
                GLPoint originP = page.originP;
                GLPoint diagonalP = page.diagonalP;
                page.autoSetOriginAndDiagonalPoints();

                //define the start and end point
                Point start = new Point();
                Point end = new Point();

                //start.set((int)originP.x,(int)originP.y);
                //end.set((int) (originP.x + diagonalP.x) / 2, (int)(originP.y + diagonalP.y));


                //activate
                computeScrollPointsForAutoFlip(true, start, end);

                if (mFlipState == PageFlipState.FORWARD_FLIP ||
                        mFlipState == PageFlipState.BACKWARD_FLIP ||
                        mFlipState == PageFlipState.UPWARD_FLIP ||
                        mFlipState == PageFlipState.RESTORE_FLIP) {

                    //activiate the job sheduling

                    Log.d(TAG, "scroll starts");
                    mScroller.startScroll(start.x, start.y,
                            end.x - start.x, end.y - start.y,
                            10000);
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
        }
        // forward flip
        else if (mListener != null &&
                mListener.canFlipForward() &&
                isForward) {
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

    public boolean onFingerMove(float touchX, float touchY)
    {
        //later

        return true;
    }

    public void computeVertexesAndBuildPage()
    {
        mPages[FRONT_PAGE].computeKeyVertexesWhenSlope();
        mPages[FRONT_PAGE].computeVertexesWhenSlope();
    }
}

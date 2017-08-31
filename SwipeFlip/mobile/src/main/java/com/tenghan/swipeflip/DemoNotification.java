package com.tenghan.swipeflip;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

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
            }
        }, 1000);

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

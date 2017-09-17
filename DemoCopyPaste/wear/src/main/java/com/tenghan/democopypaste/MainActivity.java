package com.tenghan.democopypaste;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

public class MainActivity extends WearableActivity implements GestureDetector.OnGestureListener {

    public static String TAG = "WatchActivity";
    public static MainActivity instance;
    public static MainActivity getSharedInstance()
    {
        if(instance == null)
        {
            instance = new MainActivity();
        }
        return instance;
    }

    public DemoView mDemoView;
    public DemoUIView mDemoUIView;
    public GestureDetector mGestureDetector;
    public GestureService mGestureService;

    public boolean isDoubleTapping = false;
    public Handler tapHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rect_activity_main);

        instance = this;

        mDemoView = (DemoView)findViewById(R.id.demo_view);
        mDemoUIView = (DemoUIView)findViewById(R.id.demo_ui_view);
        mDemoUIView.setDimension(320, 320);
        mGestureDetector = new GestureDetector(this, this);
        mGestureService = new GestureService();

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LoadBitmapTask.get(this).start();
        mDemoView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        mDemoView.onPause();
        LoadBitmapTask.get(this).stop();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            if(isDoubleTapping)
            {

            }else
            {
                mDemoView.onFingerUp(event.getX(), event.getY());
            }

            return true;  // there is no event detectale afterwards
        }else if(event.getAction() == MotionEvent.ACTION_MOVE)
        {
            //do nothing for now
        }

        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {

        if(isDoubleTapping)
        {
            //yes.. it's a double tap
            Log.d(TAG, "double tap");

            //indicate the mdemo that it's doing double tap task
            mDemoView.mDemo.isDoubleTappingTask = true;
            MainActivity.getSharedInstance().mDemoUIView.isdrawing = true;
            mDemoUIView.onDoubleTap(e.getX(), e.getY());
            isDoubleTapping = false;
        }else
        {
            //single tap, just no finger up event in 300 ms
            mDemoView.onFingerDown(e.getX(), e.getY());

            isDoubleTapping = true;
        }
        //activate a count down

        if(isDoubleTapping == true)
        {
            tapHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isDoubleTapping = false;
                }
            }, 300);
        }

        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {

        return false;
    }


    public boolean isreseting = false;
    public int isexisting = 0;

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        if(mDemoView.mDemo.isDoubleTappingTask == true)
        {
            mDemoUIView.onTapMove(e2.getX(), e2.getY());
        }else
        {
            mDemoView.onFingerMove(e2.getX(), e2.getY());
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
}

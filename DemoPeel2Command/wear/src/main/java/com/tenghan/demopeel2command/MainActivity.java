package com.tenghan.demopeel2command;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rect_activity_main);

        instance = this;

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

            if(isreseting)
            {
                isreseting = false;
                return false;
            }

            mDemoView.onFingerUp(event.getX(), event.getY());

            return true;  // there is no event detectale afterwards
        }else if(event.getAction() == MotionEvent.ACTION_MOVE)
        {
            //do nothing for now
        }

        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {

        mDemoView.onFingerDown(e.getX(), e.getY());
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

        isexisting++;
        if(isexisting == 2)
        {
            //exist
            finish();
            moveTaskToBack(true);
        }
        isreseting = true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        isexisting = 0;
        mDemoView.onFingerMove(e2.getX(), e2.getY());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }


}

package com.tenghan.swipeflip;

/*
a good demo to follow: https://www.youtube.com/watch?v=rVyBwz1-AiE
 */

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    PageFlipView mPageFlipView;
    DemoView mDemoView;
    GestureDetector mGestureDetector;

    private final static String TAG = "MainActivity";

    public static MainActivity instance;
    public static MainActivity getSharedInstance()
    {
        if(instance == null)
        {
            instance = new MainActivity();
        }
        return instance;
    }

    /**
     * 1 - function test
     * 2 - demo
     * 3 - study
     */
    private int activityIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

        /**
         * function test group
         */

        /*
        activityIndex = 1;
        mPageFlipView = new PageFlipView(this);
        setContentView(mPageFlipView);

        //set size and position
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mPageFlipView.getLayoutParams();
        layoutParams.width = 320;
        layoutParams.height = 320;
        //layoutParams.setMargins(100, 200, 0, 0);

        mPageFlipView.setLayoutParams(layoutParams);
        mGestureDetector = new GestureDetector(this, this);

        if(Build.VERSION.SDK_INT < 16)
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else
        {
            mPageFlipView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }*/


        /**
         * demo group
         */
        activityIndex = 2;
        mDemoView = new DemoView(this);
        setContentView(mDemoView);

        //set size and position
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mDemoView.getLayoutParams();
        layoutParams.width = 320;
        layoutParams.height = 320;
        //layoutParams.setMargins(100, 200, 0, 0);

        mDemoView.setLayoutParams(layoutParams);
        mGestureDetector = new GestureDetector(this, this);

        if(Build.VERSION.SDK_INT < 16)
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else
        {
            mDemoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }


        /**
         * study group
         */
        /*
        activityIndex = 3;

         */


    }

    @Override
    protected void onResume()
    {
        super.onResume();

        LoadBitmapTask.get(this).start();
        if(activityIndex == 1)
        {
            mPageFlipView.onResume();
        }else if(activityIndex == 2)
        {
            mDemoView.onResume();
        }else if(activityIndex == 3)
        {

        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(activityIndex == 1)
        {
            mPageFlipView.onPause();
        }else if(activityIndex == 2)
        {
            mDemoView.onPause();
        }else if(activityIndex == 3)
        {

        }

        LoadBitmapTask.get(this).stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if(activityIndex == 1)
            {
                mPageFlipView.onFingerUp(event.getX(), event.getY());
            }else if(activityIndex == 2)
            {
                mDemoView.onFingerUp(event.getX(), event.getY());
            }else if(activityIndex == 3)
            {

            }

            return true;
        }

        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if(activityIndex == 1) {
            mPageFlipView.onFingerDown(e.getX(), e.getY());
        }else if(activityIndex == 2)
        {
            mDemoView.onFingerDown(e.getX(), e.getY());
        }else if(activityIndex == 3)
        {

        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        return false;
    }


    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        if(activityIndex == 1)
        {
            mPageFlipView.onFingerMove(e2.getX(), e2.getY());
        }else if(activityIndex == 2)
        {
            mDemoView.onFingerMove(e2.getX(), e2.getY());
        }else if(activityIndex == 3)
        {

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

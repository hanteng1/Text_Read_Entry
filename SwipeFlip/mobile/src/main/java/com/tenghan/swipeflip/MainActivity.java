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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;

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
        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        LoadBitmapTask.get(this).start();
        mPageFlipView.onResume();
    }

    @Override
    protected void onPause(){
        super.onPause();
        mPageFlipView.onPause();
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
            mPageFlipView.onFingerUp(event.getX(), event.getY());
            return true;
        }

        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mPageFlipView.onFingerDown(e.getX(), e.getY());
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
        mPageFlipView.onFingerMove(e2.getX(), e2.getY());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

}

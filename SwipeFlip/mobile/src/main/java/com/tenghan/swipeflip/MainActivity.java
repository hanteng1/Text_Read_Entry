package com.tenghan.swipeflip;

/*
a good demo to follow: https://www.youtube.com/watch?v=rVyBwz1-AiE
 */

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
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
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    public PageFlipView mPageFlipView;

    public DemoView mDemoView;

    public DemoUIView mDemoUIView;

    public StudyView mStudyView;

    public GestureDetector mGestureDetector;  //handle touch screen events
    //intent for gesture recognition service
    public GestureService mGestureService;


    //android view parameters
    //540 * 960 on s4 mini
    public int watchedge =  320;//640;///320;
    public int offsetx = 110;//400;  //110;
    public int offsety =  320;//960; ///320;

    //storage
    public DataStorage storage;

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
    public int activityIndex = 0;

    /**
     * 1 - peel2command
     * 2 - notification
     * 3 - copy and paste
     */
    public int demoIndex = 0;

    /**
     * 1 - study 1
     * 2 - study 2
     */
    public int studyIndex = 0;

    //to assist touch gesture detection
    public boolean isReseting = false;
    public boolean isDoubleTapping = false;
    public Handler tapHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
        setContentView(R.layout.activity_main);

        instance = this;

        mGestureService = new GestureService();

        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frame_layout);
        //ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) frameLayout.getLayoutParams();
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) frameLayout.getLayoutParams();

        layoutParams.width = watchedge;
        layoutParams.height = watchedge;
        layoutParams.setMargins(offsetx, offsety, 0, 0);

        frameLayout.setLayoutParams(layoutParams);

        storage = DataStorage.getInstance();
        storage.clearData();

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

//        activityIndex = 2;
//        demoIndex = 3;
//        mDemoView = (DemoView)findViewById(R.id.demo_view);
//        mDemoUIView = (DemoUIView)findViewById(R.id.demo_ui_view);
//        mDemoUIView.setDimension(320, 320);
//        mDemoUIView.demoIndex = demoIndex;
//
//        mGestureDetector = new GestureDetector(this, this);
//
//        if(Build.VERSION.SDK_INT < 16)
//        {
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        }else
//        {
//            mDemoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                    View.SYSTEM_UI_FLAG_IMMERSIVE |
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        }


        /**
         * study group
         */

        activityIndex = 3;
        studyIndex = 2;
        mStudyView = (StudyView)findViewById(R.id.study_view);
        mGestureDetector = new GestureDetector(this, this);

        mDemoUIView = (DemoUIView)findViewById(R.id.demo_ui_view);
        mDemoUIView.setDimension(watchedge, watchedge);

        if(Build.VERSION.SDK_INT < 16)
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else
        {
            mStudyView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
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
        if(activityIndex == 1)
        {
            mPageFlipView.onResume();
        }else if(activityIndex == 2)
        {
            mDemoView.onResume();
        }else if(activityIndex == 3)
        {
            mStudyView.onResume();
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
            mStudyView.onPause();
        }

        if(activityIndex == 3)
        {
            if(studyIndex == 1)
            {
                storage.save();
            }else if(studyIndex == 2)
            {
                storage.save2();
            }
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

            if(isReseting)
            {
                isReseting = false;
                return false;
            }

            if(activityIndex == 1)
            {
                mPageFlipView.onFingerUp(event.getX() - offsetx, event.getY() - offsety);
            }else if(activityIndex == 2)
            {
                if(isDoubleTapping)
                {

                }else
                {
                    //Log.d(TAG, "on finger up");

                    //could be normal finger up
                    //could be double tap up
                    mDemoView.onFingerUp(event.getX()- offsetx, event.getY() - offsety);
                }


            }else if(activityIndex == 3)
            {
                mStudyView.onFingerUp(event.getX() - offsetx, event.getY() - offsety);
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
        if(activityIndex == 1) {
            mPageFlipView.onFingerDown(e.getX() - offsetx, e.getY() - offsety);
        }else if(activityIndex == 2)
        {


            if(isDoubleTapping)
            {
                //yes.. it's a double tap
                Log.d(TAG, "double tap");

                //indicate the mdemo that it's doing double tap task
                mDemoView.mDemo.isDoubleTappingTask = true;
                MainActivity.getSharedInstance().mDemoUIView.isdrawing = true;
                mDemoUIView.onDoubleTap(e.getX() - offsetx, e.getY() - offsety);

                isDoubleTapping = false;
            }else
            {
                //single tap, just no finger up event in 300 ms
                mDemoView.onFingerDown(e.getX() - offsetx, e.getY() - offsety);

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




        }else if(activityIndex == 3)
        {
            mStudyView.onFingerDown(e.getX() - offsetx, e.getY() - offsety);
        }
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {

        //Log.d(TAG, "finger flying");
        //Log.d(TAG, "velocity " + Math.sqrt(velocityX * velocityX + velocityY * velocityY));

        /*
        //set 800 as a threshold for flicking gesture
        if(Math.sqrt(velocityX * velocityX + velocityY * velocityY) > 800)
        {
            if(activityIndex == 1) {
                //mPageFlipView.onFingerFlick();
            }else if(activityIndex == 2)
            {
                mDemoView.onFingerFlick();
            }else if(activityIndex == 3)
            {

            }
        }*/

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
            mPageFlipView.onFingerMove(e2.getX() - offsetx, e2.getY() - offsety);
        }else if(activityIndex == 2)
        {
            if(mDemoView.mDemo.isDoubleTappingTask == true)
            {
                mDemoUIView.onTapMove(e2.getX() - offsetx, e2.getY() - offsety);
            }else
            {
                mDemoView.onFingerMove(e2.getX() - offsetx, e2.getY() - offsety);
            }


        }else if(activityIndex == 3)
        {
            mStudyView.onFingerMove(e2.getX() - offsetx, e2.getY() - offsety);
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

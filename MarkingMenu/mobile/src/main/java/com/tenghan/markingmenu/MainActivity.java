package com.tenghan.markingmenu;


/**
 * marking menu is compound
 * first layer discrete
 * second layer continuous
 */

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{

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

    //public DemoUIView mDemoUIView;
    public StudyTwoUIView mStudyUIView;

    public ContentView mContentView;
    public GestureDetector mGestureDetetor;  //handle touch screen events

    public int offsetX = 110;
    public int offsetY = 320;

    //storage
    public DataStorage storage;


    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frame_layout);
        //ViewGroup.LayoutParams layoutParams = (ViewGroup.LayoutParams) frameLayout.getLayoutParams();
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) frameLayout.getLayoutParams();

        layoutParams.width = 320;
        layoutParams.height = 320;
        layoutParams.leftMargin = offsetX;
        layoutParams.topMargin = offsetY;
        frameLayout.setLayoutParams(layoutParams);

        storage = DataStorage.getInstance();
        storage.clearData();

        mContentView = (ContentView)findViewById(R.id.content_view);
        mContentView.setDimension(320, 320);

//        mDemoUIView = (DemoUIView)findViewById(R.id.demo_ui_view);
//        mDemoUIView.setDimension(320, 320);

        mStudyUIView = (StudyTwoUIView)findViewById(R.id.study_two_ui_view);
        mStudyUIView.setDimension(320, 320);

        mGestureDetetor = new GestureDetector(this, this);

        if(Build.VERSION.SDK_INT < 16)
        {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else
        {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }


        button = (Button)findViewById(R.id.start_button);
        button.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                mStudyUIView.ReloadTrial();
                mStudyUIView.invalidate();
                button.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    protected void onResume()
    {
        super.onResume();

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        storage.save2();
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
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction() == MotionEvent.ACTION_UP)
        {
            //Log.d(TAG, "up");
            //mDemoUIView.onFingerUp(event.getX(), event.getY());
            mStudyUIView.onFingerUp(event.getX()- offsetX, event.getY()- offsetY);
        }else if(event.getAction() == MotionEvent.ACTION_MOVE)
        {
            //Log.d(TAG, "move");
            //mDemoUIView.onFingerMove(event.getX() - offsetX, event.getY() - offsetY);
            mStudyUIView.onFingerMove(event.getX()- offsetX, event.getY()- offsetY);
        }

        return mGestureDetetor.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e)
    {
        //Log.d(TAG, "down");
        //mDemoUIView.onFingerDown(e.getX(), e.getY());
        mStudyUIView.onFingerDown(e.getX()- offsetX, e.getY()- offsetY);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {

        //Log.d(TAG, "flying");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

        //mDemoUIView.onLongPressed(e.getX() - offsetX, e.getY() - offsetY);
        mStudyUIView.onLongPressed(e.getX() - offsetX, e.getY() - offsetY);
        //Log.d(TAG, "long press");
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY)
    {
        //Log.d(TAG, "scroll");
        //mDemoUIView.onFingerMove(e2.getX(), e2.getY());
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(TAG, "show press");
    }

    public boolean onSingleTapUp(MotionEvent e) {
        //Log.d(TAG, "single tap up");
        return false;
    }
}

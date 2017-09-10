package com.tenghan.markingmenu;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends WearableActivity implements GestureDetector.OnGestureListener  {

    public GestureDetector mGestureDetetor;

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

    public DataStorage storage;

    public StudyTwoUIView mStudyUIView;

    public ContentView mContentView;

    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rect_activity_main);

        setAmbientEnabled();
        instance = this;

        storage = DataStorage.getInstance();
        storage.clearData();


        mContentView = (ContentView)findViewById(R.id.content_view);
        mContentView.setDimension(320, 320);

        mStudyUIView = (StudyTwoUIView)findViewById(R.id.study_two_ui_view);
        mStudyUIView.setDimension(320, 320);

        mGestureDetetor = new GestureDetector(this, this);

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
        storage.save2(); // whether this will work?
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            mStudyUIView.onFingerUp(event.getX(), event.getY());
            return true;

        }else if(event.getAction() == MotionEvent.ACTION_MOVE)
        {
            //do nothing for now
            mStudyUIView.onFingerMove(event.getX(), event.getY());
        }

        return mGestureDetetor.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {

        mStudyUIView.onFingerDown(e.getX(), e.getY());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

        mStudyUIView.onLongPressed(e.getX(), e.getY());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

}

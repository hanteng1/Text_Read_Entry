package com.tenghan.swipestudytwo;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

public class MainActivity extends WearableActivity implements GestureDetector.OnGestureListener {

    public StudyView mStudyView;

    public GestureDetector mGestureDetector;
    public GestureService mGestureService;

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
    /**
     * 1 - function test
     * 2 - demo
     * 3 - study
     */
    public int activityIndex = 0;

    public int offsetx = 0;//400;  //110;
    public int offsety =  0;//960; ///320;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rect_activity_main);
        setAmbientEnabled();
        instance = this;

        //setAmbientEnabled();

        //Log.d(TAG, "" + Environment.getExternalStorageDirectory().getAbsolutePath());
        //Log.d(TAG, "" + Environment.getDataDirectory());

        mGestureService = new GestureService();

        storage = DataStorage.getInstance();
        storage.clearData();

        activityIndex = 3;
        mStudyView = (StudyView)findViewById(R.id.watch_study_view);
        mGestureDetector = new GestureDetector(this, this);

    }

//    @Override
//    public void onEnterAmbient(Bundle ambientDetails) {
//        super.onEnterAmbient(ambientDetails);
//    }
//
//    @Override
//    public void onExitAmbient() {
//        super.onExitAmbient();
//    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LoadBitmapTask.get(this).start();

        mStudyView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        mStudyView.onPause();

        storage.save();

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

            mStudyView.onFingerUp(event.getX() - offsetx, event.getY() - offsety);

            return true;  // there is no event detectale afterwards
        }else if(event.getAction() == MotionEvent.ACTION_MOVE)
        {
            //do nothing for now
        }

        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {

        mStudyView.onFingerDown(e.getX() - offsetx, e.getY() - offsety);
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

        //could do something
        isexisting++;

        if(isexisting == 2)
        {
            //exist
            finish();
            moveTaskToBack(true);
        }
        //save the data and count -1
        if(activityIndex == 3)
        {
            //storage.save();
            isreseting = true;
        }


    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        isexisting = 0;
        mStudyView.onFingerMove(e2.getX() - offsetx, e2.getY() - offsety);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
}

package com.tenghan.swipestudytwo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by hanteng on 2017-09-10.
 */

public class StudyView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private final static String TAG = "StudyView";

    int mPageNo;
    int mDuration;
    Handler mHandler;

    //public StudyOne mStudy;
    //public StudyOneRender mPageRender;


    public  StudyTwo mStudy;
    public StudyTwoRender mPageRender;


    ReentrantLock mDrawLock;

    public StudyView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        newHandler();

        mDuration = 1000;  //an interesting number to try, 1000 is fine
        int pixelsOfMesh = 10;
        boolean isAuto = false;

//        //create pageflip
//        mStudy = new StudyOne(context);
//        setEGLContextClientVersion(2);
//        // create render
//        mPageNo = mStudy.PAGE_SIZE;  //need to change, should equal to Page_Size in mPageFlip
//        mDrawLock = new ReentrantLock();
//        // init others
//        mPageNo = 1;
//        mDrawLock = new ReentrantLock();
//
//        mPageRender = new StudyOneRender(context, mStudy, mHandler, mPageNo);



        //create pageflip
        mStudy = new StudyTwo(context);
        setEGLContextClientVersion(2);
        // create render
        mPageNo = mStudy.PAGE_SIZE;  //need to change, should equal to Page_Size in mPageFlip
        mDrawLock = new ReentrantLock();
        // init others
        mPageNo = 1;
        mDrawLock = new ReentrantLock();

        mPageRender = new StudyTwoRender(context, mStudy, mHandler, mPageNo);



        // configure render
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

    }

    public int getAnimateDuration(){
        return mDuration;
    }

    public void setAnimateDuration(int duration)
    {
        mDuration = duration;
    }

    public int getPixelsOfMesh()
    {
        return mStudy.getPixelsOfMesh();
    }

    public void onFingerDown(float x, float y)
    {
        if (!mStudy.isAnimating() &&
                mStudy.getFirstPage() != null) {
            mStudy.onFingerDown(x, y);

        }
    }

    public void onFingerMove(float x, float y)
    {
        if (mStudy.isAnimating()) {
            // nothing to do during animating
        }
        else if (mStudy.canAnimate(x, y)) {
            // if the point is out of current page, try to start animating
            onFingerUp(x, y);
        }
        // move page by finger
        else if (mStudy.onFingerMove(x, y)) {
            try {
                mDrawLock.lock();
                if (mPageRender != null &&
                        mPageRender.onFingerMove(x, y)) {
                    requestRender();
                }
            }
            finally {
                mDrawLock.unlock();
            }
        }
    }

    public void onFingerUp(float x, float y) // will auto check the animation first
    {
        if (!mStudy.isAnimating()) {

            mStudy.onFingerUp(x, y, mDuration);  //test and get ready for animating

            try {
                mDrawLock.lock();
                if (mPageRender != null &&
                        mPageRender.onFingerUp(x, y)) {
                    requestRender();
                }
            }
            finally {
                mDrawLock.unlock();
            }
        }
    }

    public void autoFingerUp(float x, float y)
    {
        if (!mStudy.isAnimating()) {

            mStudy.onFingerUp(x, y, mDuration);

            try {
                mDrawLock.lock();
                if (mPageRender != null &&
                        mPageRender.onFingerUp(x, y)) {

                    requestRender();
                }
            }
            finally {
                mDrawLock.unlock();
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl)
    {
        try {
            mDrawLock.lock();
            if (mPageRender != null)
            {
                mPageRender.onDrawFrame(); //for every frame
            }
        }finally {
            mDrawLock.unlock();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)  //called when surface is created and when the surface is changed
    {
        try{
            mStudy.onSurfaceChanged(width, height);

            int pageNo = mPageRender.getPageNo();

//            if(!(mPageRender instanceof StudyOneRender)){
//                mPageRender.release();
//                mPageRender = new StudyOneRender(getContext(),
//                        mStudy,
//                        mHandler,
//                        pageNo);
//            }

            if(!(mPageRender instanceof StudyTwoRender)){
                mPageRender.release();
                mPageRender = new StudyTwoRender(getContext(),
                        mStudy,
                        mHandler,
                        pageNo);
            }

            mPageRender.onSurfaceChanged(width, height);

        }catch (PageFlipException e)
        {
            Log.e(TAG, "Failed to run DemoRender:onSurfaceChanged");
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {  //called when the gl thread starts
        try {
            mStudy.onSurfaceCreated();
        }
        catch (PageFlipException e) {
            Log.e(TAG, "Failed to run DemoRender:onSurfaceCreated");
        }
    }

    private void newHandler()
    {
        mHandler = new Handler()
        {
            public void handleMessage(Message msg)
            {
                switch (msg.what){
                    case PageRender.MSG_ENDED_DRAWING_FRAME:
                        try{
                            mDrawLock.lock();
                            if(mPageRender != null && mPageRender.onEndedDrawing(msg.arg1)){
                                requestRender();  //this is not called until the animating is done
                            }
                        }
                        finally {
                            mDrawLock.unlock();

                            //this is after the initial render
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }
}

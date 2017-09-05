package com.tenghan.swipeflip;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;

import com.eschao.android.widget.pageflip.PageFlipException;

import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by hanteng on 2017-08-18.
 */

public class DemoView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private final static String TAG = "DemoView";

    int mPageNo;
    int mDuration;
    Handler mHandler;


    public PageFlipModifyAbstract mDemo;



    //this needs to be optimized

//    public DemoPeel2CommandRender mPageRender;

//    public DemoNotificationRender mPageRender;

    public DemoCopyPasteRender mPageRender;


    ReentrantLock mDrawLock;

    public DemoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        newHandler();

        //change this value based on demos as well
        mDuration = 2000;  //an interesting number to try, 1000 is fine
        int pixelsOfMesh = 10;
        boolean isAuto = false;

        //create pageflip


        //for general demo
//        mDemo = new DemoPeel2Command(context);
//        setEGLContextClientVersion(2);
//        // create render
//        mPageNo = mDemo.PAGE_SIZE;  //need to change, should equal to Page_Size in mPageFlip
//        mDrawLock = new ReentrantLock();
//        // init others
//        mPageNo = 1;
//        mPageRender = new DemoPeel2CommandRender(context, mDemo, mHandler, mPageNo);


        //for notification demo
//        mDemo = new DemoNotification(context);
//        setEGLContextClientVersion(2);
//        // create render
//        mPageNo = mDemo.PAGE_SIZE;  //need to change, should equal to Page_Size in mPageFlip
//        mDrawLock = new ReentrantLock();
//        // init others
//        mPageNo = 1;
//        mPageRender = new DemoNotificationRender(context, mDemo, mHandler, mPageNo);



        //for copyandpaste demo
        mDemo = new DemoCopyPaste(context);
        setEGLContextClientVersion(2);
        // create render
        mPageNo = mDemo.PAGE_SIZE;  //need to change, should equal to Page_Size in mPageFlip
        mDrawLock = new ReentrantLock();
        // init others
        mPageNo = 1;
        mPageRender = new DemoCopyPasteRender(context, mDemo, mHandler, mPageNo);




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
        return mDemo.getPixelsOfMesh();
    }

    public void onFingerDown(float x, float y)
    {
        if (!mDemo.isAnimating() &&
                mDemo.getFirstPage() != null) {
            mDemo.onFingerDown(x, y);

        }
    }

    public void onFingerMove(float x, float y)
    {
        if (mDemo.isAnimating()) {
            // nothing to do during animating
        }
        else if (mDemo.canAnimate(x, y)) {
            // if the point is out of current page, try to start animating
            onFingerUp(x, y);
        }
        // move page by finger
        else if (mDemo.onFingerMove(x, y)) {
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

    public void onFingerFlick()
    {
        //lock the flipped page
    }

    public void onFingerUp(float x, float y) // will auto check the animation first
    {
        if (!mDemo.isAnimating()) {

            mDemo.onFingerUp(x, y, mDuration);  //test and get ready for animating

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
        if (!mDemo.isAnimating()) {

            mDemo.onFingerUp(x, y, mDuration);

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
            mDemo.onSurfaceChanged(width, height);

            int pageNo = mPageRender.getPageNo();

//
//            if(!(mPageRender instanceof DemoPeel2CommandRender)){
//                mPageRender.release();
//                mPageRender = new DemoPeel2CommandRender(getContext(),
//                        mDemo,
//                        mHandler,
//                        pageNo);
//            }



//
//            if(!(mPageRender instanceof DemoNotificationRender)){
//                mPageRender.release();
//                mPageRender = new DemoNotificationRender(getContext(),
//                        mDemo,
//                        mHandler,
//                        pageNo);
//            }
//


            if(!(mPageRender instanceof DemoCopyPasteRender)){
                mPageRender.release();
                mPageRender = new DemoCopyPasteRender(getContext(),
                        mDemo,
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
            mDemo.onSurfaceCreated();
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
                                requestRender();
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

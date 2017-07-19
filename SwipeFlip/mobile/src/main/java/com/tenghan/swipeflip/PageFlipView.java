package com.tenghan.swipeflip;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.eschao.android.widget.pageflip.PageFlipException;

import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by hanteng on 2017-05-30.
 */

public class PageFlipView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private final static String TAG = "PageFlipView";

    int mPageNo;
    int mDuration;
    Handler mHandler;
    PageFlipModify mPageFlip;
    PageRender mPageRender;
    ReentrantLock mDrawLock;

    public PageFlipView(Context context)
    {
        super(context);
        newHandler();

        mDuration = 10000;  //an interesting number to try
        int pixelsOfMesh = 10;
        boolean isAuto = false;

        //create pageflip
        mPageFlip = new PageFlipModify(context);

        /*
        mPageFlip.setSemiPerimeterRatio(0.8f)
                .setShadowWidthOfFoldEdges(5, 60, 0.3f)
                .setShadowWidthOfFoldBase(5, 80, 0.4f)
                .setPixelsOfMesh(pixelsOfMesh);

        */

        setEGLContextClientVersion(2);

        // create render
        mPageNo = mPageFlip.PAGE_SIZE;  //need to change, should equal to Page_Size in mPageFlip
        mDrawLock = new ReentrantLock();
        mPageRender = new SinglePageRender(context, mPageFlip,
                mHandler, mPageNo);

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
        return mPageFlip.getPixelsOfMesh();
    }

    public void onFingerDown(float x, float y)
    {
        if (!mPageFlip.isAnimating() &&
                mPageFlip.getFirstPage() != null) {
            mPageFlip.onFingerDown(x, y);
        }
    }

    public void onFingerMove(float x, float y)
    {
        if (mPageFlip.isAnimating()) {
            // nothing to do during animating
        }
        else if (mPageFlip.canAnimate(x, y)) {
            // if the point is out of current page, try to start animating
            onFingerUp(x, y);
        }
        // move page by finger
        else if (mPageFlip.onFingerMove(x, y)) {
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
        //do nothing for now
    }

    public void autoFingerUp(float x, float y)
    {
        if (!mPageFlip.isAnimating()) {


            mPageFlip.onFingerUp(x, y, mDuration);
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
            mPageFlip.onSurfaceChanged(width, height);

            int pageNo = mPageRender.getPageNo();
            if(!(mPageRender instanceof SinglePageRender)){
                mPageRender.release();
                mPageRender = new SinglePageRender(getContext(),
                        mPageFlip,
                        mHandler,
                        pageNo);
            }

            mPageRender.onSurfaceChanged(width, height);

        }catch (PageFlipException e)
        {
            Log.e(TAG, "Failed to run PageFlipRender:onSurfaceChanged");
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {  //called when the gl thread starts
        try {
            mPageFlip.onSurfaceCreated();
        }
        catch (PageFlipException e) {
            Log.e(TAG, "Failed to run PageFlipRender:onSurfaceCreated");
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

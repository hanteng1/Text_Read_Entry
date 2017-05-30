package com.tenghan.swipeflip;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;

import com.eschao.android.widget.pageflip.PageFlip;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hanteng on 2017-05-30.
 */

public class PageFlipView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private final static String TAG = "PageFlipView";

    int mPageNo;
    int mDuration;
    Handler mHandler;
    PageFlip mPageFlip;
    PageRender mPageRender;
    ReentrantLock mDrawLock;

    public PageFlipView(Context context)
    {
        super(context);
        newHandler();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        mDuration = pref.getInt(Constants.PREF_DURATION, 1000);
        int pixelsOfMesh = pref.getInt(Constants.PREF_MESH_PIXELS, 10);
        boolean isAuto = pref.getBoolean(Constants.PREF_PAGE_MODE, true);

        //create pageflip
        mPageFlip = new PageFlip(context);
        mPageFlip.setSemiPerimeterRatio(0.8f)
                .setShadowWidthOfFoldEdges(5, 60, 0.3f)
                .setShadowWidthOfFoldBase(5, 80, 0.4f)
                .setPixelsOfMesh(pixelsOfMesh)
                .enableAutoPage(isAuto);
        setEGLContextClientVersion(2);

        
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
                      }
                      break;
                  default:
                      break;
              }
          }
        };
    }

}

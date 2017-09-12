package com.tenghan.demopeel2command;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;

import com.eschao.android.widget.pageflip.OnPageFlipListener;

/**
 * Created by hanteng on 2017-09-13.
 */

public abstract class PageRender implements OnPageFlipListener {

    public final static int MSG_ENDED_DRAWING_FRAME = 1;
    private final static String TAG = "PageRender";

    final static int DRAW_MOVING_FRAME = 0;
    final static int DRAW_ANIMATING_FRAME = 1;
    final static int DRAW_FULL_PAGE = 2;

    final static int MAX_PAGES = 30;

    int mPageNo;
    int mDrawCommand;  //indicate the drawing state
    Bitmap mBitmap;
    Canvas mCanvas;
    Bitmap mBackgroundBitmap;
    Context mContext;
    Handler mHandler;
    PageFlipModifyAbstract mPageFlipAbstract;

    public PageRender(Context context, PageFlipModifyAbstract pageFlip, Handler handler, int pageNo)
    {
        mContext = context;
        mPageFlipAbstract = pageFlip;
        mPageNo = pageNo;
        //mDrawCommand = DRAW_MOVING_FRAME;
        mDrawCommand = DRAW_FULL_PAGE;
        mCanvas = new Canvas();
        mPageFlipAbstract.setListener(this);
        mHandler = handler;
    }

    public int getPageNo()
    {
        return mPageNo;
    }

    public void release()
    {
        if(mBitmap != null)
        {
            mBitmap.recycle();
            mBitmap = null;
        }

        if(mPageFlipAbstract != null)
            mPageFlipAbstract.setListener(null);
        mCanvas = null;
        mBackgroundBitmap = null;
    }

    public boolean onFingerMove(float x, float y)
    {
        mDrawCommand = DRAW_MOVING_FRAME;
        return true;
    }

    public boolean onFingerUp(float x, float y)
    {

       if(mPageFlipAbstract != null && mPageFlipAbstract.animating())
        {
            mDrawCommand = DRAW_ANIMATING_FRAME;
            return true;
        }
        return false;
    }

    public boolean onAutoFlip()
    {
        if(mPageFlipAbstract != null && mPageFlipAbstract.animating())
        {
            mDrawCommand = DRAW_ANIMATING_FRAME;
            return true;
        }
        return false;
    }

    //caculate font size
    protected int calcFontSize(int size)
    {
        return (int)(size * mContext.getResources().getDisplayMetrics().scaledDensity);
    }

    //render page frame
    abstract void onDrawFrame();

    //handle surface changing event
    abstract void onSurfaceChanged(int width, int height);

    //handle drawing ended event
    abstract boolean onEndedDrawing(int what);
}

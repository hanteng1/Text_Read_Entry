package com.tenghan.demopeel2command;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;

import com.eschao.android.widget.pageflip.PageFlipState;
import com.eschao.android.widget.pageflip.modify.PageModify;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hanteng on 2017-09-13.
 */

public abstract class DemoRender extends PageRender{

    private final static String TAG = "DemoRender";
    private final static String[] Alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    //depends on how many pages to support
    public String[][] cRIds = {{},{"Font", "Cut", "Paste", "Zoom"},
            {"Copy", "Color", "Paste", "Save"},
            {"Copy", "Color", "Paste", "Save"},
            {"Copy", "Color", "Paste", "Save"}};

    public DemoRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                      Handler handler, int pageNo) {
        super(context, pageFlipAbstract, handler, pageNo);
    }

    //what about loading textures first, then we can get rid of the mDrawCommand thing
    public abstract void LoadTextures();
    public abstract void ReloadTexture(int itrp);

    //this is calling the drawing functions
    public void onDrawFrame() {

        // 1. delete unused textures
        mPageFlipAbstract.deleteUnusedTextures();
        //PageModify page = mPageFlip.getFirstPage(); //there is only one page in single page mode
        PageModify[] pages = mPageFlipAbstract.getPages();

        // 2. handle drawing command triggered from finger moving and animating
        if (mDrawCommand == DRAW_MOVING_FRAME ||
                mDrawCommand == DRAW_ANIMATING_FRAME) {

            //is translating
            if(mPageFlipAbstract.getFlipState() == PageFlipState.FORWARD_TRANSLATE)
            {
                for(int itrp = 0; itrp < mPageFlipAbstract.PAGE_SIZE; itrp++)
                {
                    if(!pages[itrp].isFrontTextureSet())
                    {
                        loadPageWithContent(mPageNo);
                        pages[itrp].setFrontTexture(mBitmap);
                    }
                }

                mPageFlipAbstract.drawTranslateFrame();

            }else
            {

                //peel to command
                for(int itrp = 0; itrp < mPageFlipAbstract.PAGE_SIZE; itrp++)
                {
                    if(pages[itrp].waiting4TextureUpdate == true)
                    {
                        loadPageWithCommands(itrp, cRIds[itrp]);
                        pages[itrp].updateFrontTexture(mBitmap);
                    }
                }
                // Log.d(TAG, "draw flip called");
                mPageFlipAbstract.drawFlipFrameWithIndex(mPageFlipAbstract.currentPageLock);
            }

        }
        // draw stationary page without flipping
        else if (mDrawCommand == DRAW_FULL_PAGE) {

            int commandPage = MainActivity.getSharedInstance().mDemoView.mDemo.currentPageLock + 1;
            MainActivity.getSharedInstance().mGestureService.reset();
            MainActivity.getSharedInstance().mDemoView.mPageRender.ReloadTexture(commandPage);

            MainActivity.getSharedInstance().mDemoView.mDemo.releasePageLock();
            //clear the maxtravel
            MainActivity.getSharedInstance().mDemoView.mDemo.maxTravelDis = 0;

            for(int itrp = 0; itrp < mPageFlipAbstract.PAGE_SIZE; itrp++)
            {
                if (!pages[itrp].isFrontTextureSet()) {
                    loadPageWithContent(itrp);
                    pages[itrp].setFrontTexture(mBitmap);
                }

                if(pages[itrp].waiting4TextureUpdate == true)
                {
                    loadPageWithCommands(itrp, cRIds[itrp]);
                    pages[itrp].updateFrontTexture(mBitmap);
                }

            }

            mPageFlipAbstract.drawPageFrame();  //see the difference
        }

        // 3. send message to main thread to notify drawing is ended so that
        // we can continue to calculate next animation frame if need.
        // Remember: the drawing operation is always in GL thread instead of
        // main thread
        Message msg = Message.obtain();
        msg.what = MSG_ENDED_DRAWING_FRAME;
        msg.arg1 = mDrawCommand;

        //Log.d(TAG, "send message called");
        mHandler.sendMessage(msg);
    }


    public void onSurfaceChanged(int width, int height) {
        // recycle bitmap resources if need
        if (mBackgroundBitmap != null) {
            mBackgroundBitmap.recycle();
        }

        if (mBitmap != null) {
            mBitmap.recycle();
        }

        // create bitmap and canvas for page
        //mBackgroundBitmap = background;
        PageModify page = mPageFlipAbstract.getFirstPage();
        mBitmap = Bitmap.createBitmap((int)page.width(), (int)page.height(),
                Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap); //specifiy the bitmap for the canvas to draw into
        LoadBitmapTask.get(mContext).set(width, height, 1);

        LoadTextures();

    }

    public boolean onEndedDrawing(int what) {  //this is called with handler event, to examine whether the animating is done, return true only if the animating the done
        if (what == DRAW_ANIMATING_FRAME) {
            boolean isAnimating = mPageFlipAbstract.animating();

            //Log.d(TAG, "check point");

            // continue animating
            if (isAnimating) {
                mDrawCommand = DRAW_ANIMATING_FRAME;
                return true;
            } else {

                final PageFlipState state = mPageFlipAbstract.getFlipState();
                // update page number for backward flip
                if (state == PageFlipState.END_WITH_BACKWARD) {
                    // don't do anything on page number since mPageNo is always
                    //should add the page to front

                    // represents the FIRST_TEXTURE no;



                }
                // update page number and switch textures for forward flip
                else if (state == PageFlipState.END_WITH_FORWARD) {
                    //mPageFlip.getFirstPage().setFirstTextureWithSecond();

                    //should delete the front page

                    //mPageNo++;
                }

                //which whether it is auto flip , or it is restore flip
                if(MainActivity.getSharedInstance().mDemoView.mDemo.flipType == 1)
                {
                    mDrawCommand = DRAW_FULL_PAGE;
                }else
                {
                    mDrawCommand = DRAW_MOVING_FRAME;
                }

                return true;
            }
        }
        return false;
    }

    public abstract void loadPageWithCommands(int number, String[] commandIds);
    public abstract void loadPageWithContent(int pageindex);

    public boolean canFlipForward()
    {
        return (mPageNo < MAX_PAGES);
    }

    public boolean canFlipBackward()
    {
        if(mPageNo > 1) {
            //mPageFlip.getFirstPage().setSecondTextureWithFirst();
            return true;
        }
        else{
            return false;
        }
    }
}

package com.tenghan.swipestudytwo;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.eschao.android.widget.pageflip.PageFlipState;
import com.eschao.android.widget.pageflip.modify.PageModify;

/**
 * Created by hanteng on 2017-09-10.
 */

public abstract class StudyRender extends PageRender{

    private final static String TAG = "StudyRender";

    public int FIRST_PAGE = 0;
    public int SECOND_PAGE = 1;

    public StudyRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                       Handler handler, int pageNo) {
        super(context, pageFlipAbstract, handler, pageNo);
    }

    //what about loading textures first, then we can get rid of the mDrawCommand thing
    public abstract void LoadTextures();
    public abstract void ReloadFirstPageTexture();
    public abstract void ReloadSecondPageTexture();
    public abstract void ReloadTrial();

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
//
//                for(int itrp = 0; itrp < mPageFlipAbstract.PAGE_SIZE; itrp++)
//                {
//                    if(!pages[itrp].isFrontTextureSet())
//                    {
//                        loadPage(mPageNo);
//                        pages[itrp].setFrontTexture(mBitmap);
//                    }
//                }
//
//                mPageFlipAbstract.drawTranslateFrame();

            }else
            {
                //what about updating the texture here
//                for(int itrp = 0; itrp < mPageFlipAbstract.PAGE_SIZE; itrp++)
//                {
//                    if(pages[itrp].waiting4TextureUpdate == true)
//                    {
//                        loadPageWithCommands(itrp, cRIds[itrp]);
//                        pages[itrp].updateFrontTexture(mBitmap);
//
//                    }
//                }

                if(pages[FIRST_PAGE].waiting4TextureUpdate == true)
                {
                    loadPageWithoutTrialInfo();
                    pages[FIRST_PAGE].updateFrontTexture(mBitmap);
                }

                if(pages[SECOND_PAGE].waiting4TextureUpdate == true)
                {
                    loadPageWithCondition();
                    pages[SECOND_PAGE].updateFrontTexture(mBitmap);
                }

                mPageFlipAbstract.drawFlipFrameWithIndex(mPageFlipAbstract.currentPageLock);
            }

        }
        // draw stationary page without flipping
        else if (mDrawCommand == DRAW_FULL_PAGE) {

            //this is called when animation is finished
            //reload the pagelock texture
            //set to next trial
            MainActivity.getSharedInstance().mStudyView.mPageRender.ReloadTrial();

            MainActivity.getSharedInstance().mStudyView.mStudy.releasePageLock();
            //clear the maxtravel
            MainActivity.getSharedInstance().mStudyView.mStudy.maxTravelDis = 0;

            for(int itrp = 0; itrp < mPageFlipAbstract.PAGE_SIZE; itrp++)
            {
                if(pages[FIRST_PAGE].waiting4TextureUpdate)
                {
                    //update the first page
                    loadPageWithTrialInfo();
                    pages[FIRST_PAGE].updateFrontTexture(mBitmap);

                }

                if(pages[SECOND_PAGE].waiting4TextureUpdate)
                {
                    //update the second page
                    loadPageWithCondition();
                    pages[SECOND_PAGE].updateFrontTexture(mBitmap);

                }
//
//                if (!pages[itrp].isFrontTextureSet()) {
//                    loadPage(itrp);
//                    pages[itrp].setFrontTexture(mBitmap);
//                }
//
//                if(pages[itrp].waiting4TextureUpdate == true)
//                {
//                    loadPageWithCommands(itrp, cRIds[itrp]);
//                    pages[itrp].updateFrontTexture(mBitmap);
//                }


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

                mDrawCommand = DRAW_FULL_PAGE;
                return true;
            }
        }
        return false;
    }

    public abstract void loadPageWithoutTrialInfo();
    public abstract void loadPageWithTrialInfo();
    public abstract void loadPageWithCondition();
    public abstract void loadPageWithRealTimeFeedback();


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

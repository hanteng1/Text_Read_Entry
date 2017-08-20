package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.eschao.android.widget.pageflip.PageFlipState;
import com.eschao.android.widget.pageflip.modify.PageModify;

/**
 * Created by hanteng on 2017-08-18.
 *
 * Used as an abstract render class
 */

public abstract class DemoRender extends PageRender{

    private final static String TAG = "DemoRender";
    private final static String[] Alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

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
                        loadPage(mPageNo);
                        pages[itrp].setFrontTexture(mBitmap);
                    }
                }

                mPageFlipAbstract.drawTranslateFrame();

            }else
            {
                // is forward flip
                if (mPageFlipAbstract.getFlipState() == PageFlipState.FORWARD_FLIP) {
                    // check if second texture of first page is valid, if not,
                    // create new one
                    //if (!page.isSecondTextureSet()) {
                    //    drawPage(mPageNo + 1);  //the drawpage function is actually just creating texture
                    //    page.setSecondTexture(mBitmap);
                    //}
                }
                // in backward flip, check first texture of first page is valid
                //else if (!page.isFirstTextureSet()) {
                //    loadPage(--mPageNo);
                //    page.setFirstTexture(mBitmap);  //the texture on the canvas, thus the canvas in the render is used to create textures in the format of bitmap
                //}

                // draw frame for page flip
                //mPageFlipAbstract.drawFlipFrame();  //see the difference
                mPageFlipAbstract.drawFlipFrameWithIndex(mPageFlipAbstract.flipped);
            }

        }
        // draw stationary page without flipping
        else if (mDrawCommand == DRAW_FULL_PAGE) {

            for(int itrp = 0; itrp < mPageFlipAbstract.PAGE_SIZE; itrp++)
            {
                if (!pages[itrp].isFrontTextureSet()) {
                    loadPage(itrp);
                    pages[itrp].setFrontTexture(mBitmap);
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

    public void loadPage(int number) {  //create a new page texture (either first one or second one) when necessary/not set
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();
        Paint p = new Paint();
        p.setFilterBitmap(true);

        // 1. load/draw background bitmap
        Bitmap background = LoadBitmapTask.get(mContext).getBitmap();  //get the bitmap in queue
        Rect rect = new Rect(0, 0, width, height);
        mCanvas.drawBitmap(background, null, rect, p); //will this refresh the canvas? since it's using a new rect
        background.recycle();
        background = null;

        // 2. load/draw page number
        int fontSize = calcFontSize(80);
        p.setColor(Color.GRAY);
        p.setStrokeWidth(1);
        p.setAntiAlias(true);
        //p.setShadowLayer(5.0f, 8.0f, 8.0f, Color.BLACK);
        p.setTextSize(fontSize);
        String text = Alphabet[number];
        float textWidth = p.measureText(text);
        float y = height - p.getTextSize() - 20;
        mCanvas.drawText(text, (width - textWidth) / 2, y, p);
    }

    public void loadPageWithCommands(int number, String[] commandIds)
    {
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();
        Paint p = new Paint();
        p.setFilterBitmap(true);

        // 1. load/draw background bitmap
        Bitmap background = LoadBitmapTask.get(mContext).getBitmap();  //get the bitmap in queue
        Rect rect = new Rect(0, 0, width, height);
        mCanvas.drawBitmap(background, null, rect, p); //will this refresh the canvas? since it's using a new rect
        background.recycle();
        background = null;

        // 2. load/draw page number
        /*
        int fontSize = calcFontSize(80);
        p.setColor(Color.GRAY);
        p.setStrokeWidth(1);
        p.setAntiAlias(true);
        //p.setShadowLayer(5.0f, 8.0f, 8.0f, Color.BLACK);
        p.setTextSize(fontSize);
        String text = Alphabet[number];
        float textWidth = p.measureText(text);
        float y = height - p.getTextSize() - 20;
        float x = (width - textWidth) / 2;
        mCanvas.drawText(text, x, y, p);
        */

        //3. load/draw commands on corners
        for(int itrc = 0; itrc < commandIds.length; itrc++)
        {
            int fontSize = calcFontSize(20);
            if(MainActivity.getSharedInstance().mGestureService.activiatedCommandIndex == itrc)
            {
                p.setColor(Color.RED);
            }else
            {
                p.setColor(Color.GRAY);
            }

            p.setStrokeWidth(1);
            p.setAntiAlias(true);
            p.setTextSize(fontSize);
            String text = commandIds[itrc];
            float textWidth = p.measureText(text);
            float x, y;

            /**
             *    ---------
             *   |0       1|
             *   |         |
             *   |         |
             *   |3       2|
             *    ---------
             */

            float offset = 20.0f;
            if(itrc == 0 || itrc == 3)
            {
                x = offset;
            }else
            {
                x = width - offset - textWidth;
            }

            if(itrc == 0 || itrc == 1)
            {
                y = offset + p.getTextSize()/2;
            }else
            {
                y = height - offset;
            }

            mCanvas.save();
            if(itrc == 0 || itrc == 2)
                mCanvas.rotate(-45f, x + textWidth/2, y - p.getTextSize()/2);
            else if(itrc == 1 || itrc == 3)
                mCanvas.rotate(45f, x + textWidth/2, y - p.getTextSize()/2);
            mCanvas.drawText(text, x, y, p);
            mCanvas.restore();
        }

    }

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

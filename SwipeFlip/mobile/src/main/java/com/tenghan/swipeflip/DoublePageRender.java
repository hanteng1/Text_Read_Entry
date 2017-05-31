package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;

import com.eschao.android.widget.pageflip.Page;
import com.eschao.android.widget.pageflip.PageFlip;
import com.eschao.android.widget.pageflip.PageFlipState;

/**
 * Created by hanteng on 2017-05-30.
 */

public class DoublePageRender extends PageRender {

    public DoublePageRender(Context context, PageFlip pageFlip,
                            Handler handler, int pageNo) {
        super(context, pageFlip, handler, pageNo);
    }

    public void onDrawFrame()
    {
        mPageFlip.deleteUnusedTextures();

        //two pages to draw for the whole screen
        final Page first = mPageFlip.getFirstPage();
        final Page second = mPageFlip.getSecondPage();

        //check first texture
        if(!first.isFirstTextureSet()){
            drawPage(first.isLeftPage() ? mPageNo : mPageNo + 1);
            first.setFirstTexture(mBitmap);
        }

        //check first texture for the second page
        if(!second.isFirstTextureSet())
        {
            drawPage(second.isLeftPage() ? mPageNo : mPageNo + 1);
            second.setFirstTexture(mBitmap);
        }

        //drawing command from finger moving and animating
        if(mDrawCommand == DRAW_MOVING_FRAME || mDrawCommand == DRAW_ANIMATING_FRAME){
            //check the back texture for first page
            if(!first.isBackTextureSet()){
                drawPage(first.isLeftPage()? mPageNo - 1 : mPageNo + 2);
                first.setBackTexture(mBitmap);
            }

            //check second texture of the first page
            if (!first.isSecondTextureSet()){
                drawPage(first.isLeftPage() ? mPageNo - 2 : mPageNo + 3);
                first.setSecondTexture(mBitmap);
            }

            //draw frame for page flip
            mPageFlip.drawFlipFrame();
        }else if(mDrawCommand == DRAW_FULL_PAGE)
        {
            mPageFlip.drawPageFrame();
        }

        //send message to main thread to notify drawing is ended
        Message msg = Message.obtain();
        msg.what = MSG_ENDED_DRAWING_FRAME;
        msg.arg1 = mDrawCommand;
        mHandler.sendMessage(msg);

    }

    public void onSurfaceChanged(int width, int height)
    {
        // recycle bitmap resources if need
        if (mBackgroundBitmap != null) {
            mBackgroundBitmap.recycle();
        }

        if (mBitmap != null) {
            mBitmap.recycle();
        }

        // create bitmap and canvas for page
        //mBackgroundBitmap = background;
        Page page = mPageFlip.getFirstPage();
        int pageW = (int)page.width();
        int pageH = (int)page.height();
        mBitmap = Bitmap.createBitmap(pageW, pageH, Bitmap.Config.ARGB_8888);
        mCanvas.setBitmap(mBitmap);
        LoadBitmapTask.get(mContext).set(pageW, pageH, 2);
    }

    public boolean onEndedDrawing(int what)
    {
        if (what == DRAW_ANIMATING_FRAME) {
            boolean isAnimating = mPageFlip.animating();
            // continue animating
            if (isAnimating) {
                mDrawCommand = DRAW_ANIMATING_FRAME;
                return true;
            }
            // animation is finished
            else {
                // should handle forward flip to update page number and exchange
                // textures between first and second pages. Don't have to handle
                // backward flip since there is no such state happened in double
                // page mode
                if (mPageFlip.getFlipState() == PageFlipState.END_WITH_FORWARD)
                {
                    final Page first = mPageFlip.getFirstPage();
                    final Page second = mPageFlip.getSecondPage();
                    second.swapTexturesWithPage(first);

                    // update page number for left page
                    if (first.isLeftPage()) {
                        mPageNo -= 2;
                    }
                    else {
                        mPageNo += 2;
                    }
                }

                mDrawCommand = DRAW_FULL_PAGE;
                return true;
            }
        }
        return false;
    }

    private void drawPage(int number)
    {
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();
        Paint p = new Paint();
        p.setFilterBitmap(true);

        //draw background bitmap
        Bitmap background = LoadBitmapTask.get(mContext).getBitmap();
        Rect rect = new Rect(0, 0, width, height);
        if(width > height)
        {
            mCanvas.rotate(90);
            mCanvas.drawBitmap(background, null, rect, p);
            mCanvas.rotate(-90);

        }else{
            mCanvas.drawBitmap(background, null, rect, p);
        }

        background.recycle();
        background = null;

        //draw page number
        int fontSize = (int)(80 * mContext.getResources().getDisplayMetrics().scaledDensity);
        p.setColor(Color.WHITE);
        p.setStrokeWidth(1);
        p.setAntiAlias(true);
        p.setShadowLayer(5.0f, 8.0f, 8.0f, Color.BLACK);
        p.setTextSize(fontSize);

        String text = String.valueOf(number);
        if(number < 1) {
            text = "Preface";
        }else if(number > MAX_PAGES){
            text = "End";
        }

        float textWidth = p.measureText(text);
        float y = height - p.getTextSize() - 20;
        mCanvas.drawText(text, (width - textWidth)/2, y, p);

        if(number == 1){
            String firstPage = "The First Page";
            p.setTextSize(calcFontSize(16));
            float w = p.measureText(firstPage);
            float h = p.getTextSize();
            mCanvas.drawText(firstPage, (width - w)/2, y + 5 + h, p);
        }else if(number == MAX_PAGES)
        {
            String lastPage = "The Last Page";
            p.setTextSize(calcFontSize(16));
            float w = p.measureText(lastPage);
            float h = p.getTextSize();
            mCanvas.drawText(lastPage, (width - w)/2, y + 5 + h, p);
        }
    }

    public boolean canFlipForward()
    {
        final Page page = mPageFlip.getFirstPage();
        if(page.isLeftPage()){
            return (mPageNo > 1);
        }
        return (mPageNo + 2 <= MAX_PAGES );

    }

    public boolean canFlipBackward()
    {
        return false;
    }

}

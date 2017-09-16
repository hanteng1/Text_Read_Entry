package com.tenghan.demonotification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;

import com.eschao.android.widget.pageflip.modify.PageModify;

/**
 * Created by hanteng on 2017-09-17.
 */

public class DemoNotificationRender extends DemoRender{

    private final static String TAG = "DemoNotification";

    public DemoNotificationRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                                  Handler handler, int pageNo)
    {
        super(context, pageFlipAbstract, handler, pageNo);
    }

    //initial load
    public void LoadTextures(){

        mPageFlipAbstract.deleteUnusedTextures();
        PageModify[] pages = mPageFlipAbstract.getPages();

        //set the first page
        if(!pages[0].isFrontTextureSet())
        {
            loadPage(0);
            pages[0].setFrontTexture(mBitmap);
        }

        //set the second page
        if(!pages[1].isFrontTextureSet())
        {
            loadPageWithFacebook(1);
            pages[1].setFrontTexture(mBitmap);
        }

//        //set the rest, if any
        for(int itrp = 2; itrp < mPageFlipAbstract.PAGE_SIZE; itrp++)
        {
            if(!pages[itrp].isFrontTextureSet())
            {
                //commands ids
                loadPageWithCommands(itrp, cRIds[itrp]);
                pages[itrp].setFrontTexture(mBitmap);

            }
        }

    }

    public void ReloadTexture(int itrp)
    {
        PageModify page = mPageFlipAbstract.getPages()[itrp];
        page.waiting4TextureUpdate = true;
    }

    public void loadPageWithCommands(int number, String[] commandIds)
    {
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();

        //general purpose
        Paint p = new Paint();
        p.setFilterBitmap(true);

        //color panel
        Paint panelPaint = new Paint();
        panelPaint.setAntiAlias(true);
        panelPaint.setStrokeWidth(0);
        panelPaint.setColor(Color.RED);
        panelPaint.setStyle(Paint.Style.FILL);


        // 1. load/draw background bitmap
        Bitmap background = LoadBitmapTask.get(mContext).getBitmap();  //get the bitmap in queue
        Rect rect = new Rect(0, 0, width, height);
        mCanvas.drawBitmap(background, null, rect, p); //will this refresh the canvas? since it's using a new rect
        background.recycle();
        background = null;

        if(MainActivity.getSharedInstance().mGestureService.activiatedCommandIndex == -1)
        {
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
    }


    //for notification demo
    public void loadPageWithFacebook(int fbstate)
    {
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();

        //general purpose
        Paint p = new Paint();
        p.setFilterBitmap(true);

        //TEXT panel
        Paint panelPaint = new Paint();
        panelPaint.setAntiAlias(true);
        panelPaint.setStrokeWidth(1);
        panelPaint.setColor(Color.WHITE);
        panelPaint.setStyle(Paint.Style.STROKE);

        if(fbstate == 1)
        {
            //notify
            // 1. load/draw background bitmap
            Bitmap background = LoadBitmapTask.get(mContext).getFacebook(1);  //get the bitmap in queue
            Rect rect = new Rect(0, 0, width, height);
            mCanvas.drawBitmap(background, null, rect, p); //will this refresh the canvas? since it's using a new rect
            background.recycle();
            background = null;

            Bitmap global = LoadBitmapTask.get(mContext).getFacebook(3);
            mCanvas.drawBitmap(global, 0, 0, p);
        }else if (fbstate == 2)
        {
            //previewing, real-time update
            Bitmap background = LoadBitmapTask.get(mContext).getFacebook(1);  //get the bitmap in queue
            Rect rect = new Rect(0, 0, width, height);
            mCanvas.drawBitmap(background, null, rect, p); //will this refresh the canvas? since it's using a new rect
            background.recycle();
            background = null;

            //draw global icon
            Bitmap global = LoadBitmapTask.get(mContext).getFacebook(3);
            mCanvas.drawBitmap(global, 0, 0, p);

        }else if(fbstate == 3)
        {
            //whole page
            Bitmap background = LoadBitmapTask.get(mContext).getFacebook(2);  //get the bitmap in queue
            Rect rect = new Rect(0, 0, width, height);
            mCanvas.drawBitmap(background, null, rect, p); //will this refresh the canvas? since it's using a new rect
            background.recycle();
            background = null;

            //draw global icon
            Bitmap global = LoadBitmapTask.get(mContext).getFacebook(3);
            mCanvas.drawBitmap(global, 0, 0, p);
        }



    }

    public void loadPageWithCopyPaste(int itr)
    {

    }
}

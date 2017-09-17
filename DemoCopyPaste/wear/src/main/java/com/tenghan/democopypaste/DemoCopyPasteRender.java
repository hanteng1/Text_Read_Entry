package com.tenghan.democopypaste;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;

import com.eschao.android.widget.pageflip.modify.PageModify;

/**
 * Created by hanteng on 2017-09-17.
 */

public class DemoCopyPasteRender extends DemoRender {

    private final static String TAG = "DemoCopyPaste";

    private Bitmap cropBitmap;
    private PointF cropAnchor;
    private boolean isCropReady;

    private Bitmap croppedFirstPage;


    public DemoCopyPasteRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                               Handler handler, int pageNo)
    {
        super(context, pageFlipAbstract, handler, pageNo);
    }

    //initial load
    public void LoadTextures()
    {
        mPageFlipAbstract.deleteUnusedTextures();
        PageModify[] pages = mPageFlipAbstract.getPages();

        //set first pages
        if(!pages[0].isFrontTextureSet())
        {
            loadPhotoPage();
            pages[0].setFrontTexture(mBitmap);
        }

        //set the rest to blank
        for(int itrp = 1; itrp < pages.length; itrp++)
        {
            if(!pages[itrp].isFrontTextureSet())
            {
                loadBlankPage();
                pages[itrp].setFrontTexture(mBitmap);
            }
        }

    }

    public void ReloadTexture(int itrp)
    {
        PageModify page = mPageFlipAbstract.getPages()[itrp];
        page.waiting4TextureUpdate = true;
    }

    public void loadPageWithFacebook(int fbstate)
    {

    }

    public void loadPageWithCommands(int number, String[] commandIds)
    {

    }

    public void loadPageWithCopyPaste(int itr)
    {

        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();

        //general purpose
        Paint p = new Paint();
        p.setFilterBitmap(true);

        if(itr == 0 )
        {
            //first page
            //delete the cropped bitmap
            if(isCropReady == true)
            {
                if(MainActivity.getSharedInstance().mGestureService.activiatedCommandIndex == 0)
                {
                    //mBitmap = croppedFirstPage;
                    //mBitmap.eraseColor(Color.TRANSPARENT);

//                    Bitmap intermediaBitmap = Bitmap.createBitmap(mCanvas.getWidth(), mCanvas.getHeight(),
//                            Bitmap.Config.ARGB_8888);
//                    mCanvas.setBitmap(intermediaBitmap);
                    mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    mCanvas.drawBitmap(croppedFirstPage, 0, 0, p);
//
//                    mBitmap = intermediaBitmap;
//
//                    mCanvas.setBitmap(mBitmap);
                }
            }

        }else if(itr == 1)
        {

            // 1. load/draw background bitmap
            Bitmap background = LoadBitmapTask.get(mContext).getBitmap();  //get the bitmap in queue
            Rect rect = new Rect(0, 0, width, height);
            mCanvas.drawBitmap(background, null, rect, p); //will this refresh the canvas? since it's using a new rect
            background.recycle();
            background = null;


            //second page
            //draw the cropped bitmap
            if(isCropReady == true)
            {
                //detect gesture condition
                if(MainActivity.getSharedInstance().mGestureService.activiatedCommandIndex == -1)
                {
                    //load with commands
                    String commandIds[] = new String[] {"Cut", "Copy", "Unknow", "Unknown"};

                    for(int itrc = 0; itrc < 4; itrc++)
                    {
                        int fontSize = calcFontSize(20);
                        p.setColor(Color.GRAY);
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
                }else {
                    mCanvas.drawBitmap(cropBitmap, cropAnchor.x, cropAnchor.y, p);
                }
            }

        }

    }

    public void setCropImage(Bitmap cropedImg, Bitmap cropImg, PointF anchor)
    {
        //this is for first page
        croppedFirstPage = cropedImg;

        //this is for second page
        cropBitmap = cropImg;
        cropAnchor = anchor;
        isCropReady = true;
    }

}

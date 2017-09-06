package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;

import com.eschao.android.widget.pageflip.modify.PageModify;

/**
 * Created by hanteng on 2017-09-05.
 */

public class DemoCopyPasteRender extends DemoRender {

    private final static String TAG = "DemoCopyPaste";

    private Bitmap cropBitmap;
    private PointF cropAnchor;
    private boolean isCropReady;

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
            loadPage(0);
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

        // 1. load/draw background bitmap
        Bitmap background = LoadBitmapTask.get(mContext).getBitmap();  //get the bitmap in queue
        Rect rect = new Rect(0, 0, width, height);
        mCanvas.drawBitmap(background, null, rect, p); //will this refresh the canvas? since it's using a new rect
        background.recycle();
        background = null;

        if(itr == 0 )
        {
            //first page
        }else if(itr == 1)
        {
            //second page
            //draw the cropped bitmap
            if(isCropReady == true)
            {
                mCanvas.drawBitmap(cropBitmap, cropAnchor.x, cropAnchor.y, p);
            }

        }

    }

    public void setCropImage(Bitmap cropImg, PointF anchor)
    {
        cropBitmap = cropImg;
        cropAnchor = anchor;
        isCropReady = true;
    }




}

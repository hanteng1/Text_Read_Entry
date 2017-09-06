package com.tenghan.swipeflip;

import android.app.IntentService;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;

import com.eschao.android.widget.pageflip.modify.PageModify;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by hanteng on 2017-08-19.
 *
 * To customize the render for the peel2command demo
 * First page - content
 * Second page - command
 */

public class DemoPeel2CommandRender extends DemoRender{

    private final static String TAG = "DemoPeel2CommandRender";

    //for color panel
    private ArrayList<Integer> colorCode;
    private Random rand;
    private int totalColor = 100;
    private int presentedColor = 10;
    private int colorAnchor = 45;
    private int colorBandWidth = 20;
    private float sin45;

    private ArrayList<Integer> fontSizes;
    private int totalFontSize = 100;
    private int presentedFontSize = 10;
    private int fontSizeAnchor = 1;
    private int fontBandDistance = 10;

    public DemoPeel2CommandRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                                  Handler handler, int pageNo)
    {
        super(context, pageFlipAbstract, handler, pageNo);

        colorCode = new ArrayList<Integer>();
        rand = new Random();
        generateColor(totalColor);
        sin45 = (float)Math.sin(Math.PI / 4);

        fontSizes = new ArrayList<Integer>();
        generateFontSize(totalFontSize);
    }

    private void generateColor(int num)
    {
        int maxDistance = 50;

        int a = 255;
        //set first to white
        int r = 255;
        int g = 255;
        int b = 255;

        int color = Color.argb(a, r, g, b);
        colorCode.add(color);

        for(int i = 1; i < num; i++)
        {
            r = randomValue(r, maxDistance);
            g = randomValue(g, maxDistance);
            b = randomValue(b, maxDistance);

            color = Color.argb(a, r, g, b);
            colorCode.add(color);
        }
    }

    private void generateFontSize(int num)
    {
        int step = 2;
        int base = 10;

        for(int itrf = 0; itrf < num; itrf++)
        {
            int size = calcFontSize(base + step * itrf);
            fontSizes.add(size);
        }
    }

    private int randomValue(int value, int distance)
    {
        //randomly decide pos or negative
        int pos = rand.nextInt(2) > 0 ? 1 : -1;
        int newValue = value + pos * rand.nextInt(distance);

        if(newValue > 255 || newValue < 0)
        {
            return randomValue(value, distance);
        }

        return newValue;
    }

    public void LoadTextures(){
        mPageFlipAbstract.deleteUnusedTextures();
        PageModify[] pages = mPageFlipAbstract.getPages();

        //set the first page
        if(!pages[0].isFrontTextureSet())
        {
            loadPage(0);
            pages[0].setFrontTexture(mBitmap);
        }

        //set the rest
        for(int itrp = 1; itrp < mPageFlipAbstract.PAGE_SIZE; itrp++)
        {
            if(!pages[itrp].isFrontTextureSet())
            {
                //commands ids
                loadPageWithCommands(itrp, cRIds[itrp]);
                pages[itrp].setFrontTexture(mBitmap);

                //loadPage(itrp + 5);
                //pages[itrp].setBackTexture(mBitmap);
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


        //load color texture
        //this follows the coordinates of android, not opengl
        //check which page and which command
        if( MainActivity.getSharedInstance().mDemoView.mDemo.currentPageLock == 1 &&
                MainActivity.getSharedInstance().mGestureService.activiatedCommandIndex == 1)
        {
            float oriX = width - 3;
            float oriY = 3;

            Path path =new Path();
            for (int itrc = 0; itrc < presentedColor; itrc++)
            {
                path.reset();

                path.moveTo(oriX - (itrc + 5)  * colorBandWidth / sin45, oriY);
                path.lineTo(oriX - (itrc + 4) * colorBandWidth / sin45, oriY);
                path.lineTo(oriX, oriY + (itrc + 4) * colorBandWidth / sin45);
                path.lineTo(oriX, oriY + (itrc + 5) * colorBandWidth / sin45);

                panelPaint.setColor(colorCode.get(itrc + colorAnchor));

                mCanvas.drawPath(path, panelPaint);
            }

        }


        //font texture
        if (MainActivity.getSharedInstance().mDemoView.mDemo.currentPageLock == 0 &&
                MainActivity.getSharedInstance().mGestureService.activiatedCommandIndex == 0)
        {

            p.setColor(Color.GRAY);
            p.setStrokeWidth(1);
            p.setAntiAlias(true);
            String text = "Aa";
            float y = 0; //= fontBandDistance * 1 * sin45;
            float x = 0; //= fontBandDistance * 1 * sin45;

            for(int itrf = 0; itrf < (0 + presentedFontSize); itrf++)
            {
                int fontSize = calcFontSize(fontSizes.get(fontSizeAnchor + itrf));
                p.setTextSize(fontSize);

                x += (fontBandDistance + p.measureText(text)) * sin45;
                y += (fontBandDistance + p.measureText(text)) * sin45;

                mCanvas.drawText(text, x - p.measureText(text)/2, y, p);
            }

        }

        //font size zooms
        //real time update
        if(MainActivity.getSharedInstance().mDemoView.mDemo.currentPageLock == 0 &&
                MainActivity.getSharedInstance().mGestureService.activiatedCommandIndex == 3)
        {
            p.setColor(Color.GRAY);
            p.setStrokeWidth(1);
            p.setAntiAlias(true);
            String text = "Aa";
            float y = height;
            float x = 0;

            int fontSize = calcFontSize((int)MainActivity.getSharedInstance().mGestureService.curDistance);
            p.setTextSize(fontSize/2);
            mCanvas.drawText(text, x, y, p);

        }

        //list scrolling
        if(MainActivity.getSharedInstance().mDemoView.mDemo.currentPageLock == 0 &&
                MainActivity.getSharedInstance().mGestureService.activiatedCommandIndex == 1)
        {

        }

    }

    public void loadPageWithFacebook(int fbstate)
    {

    }

    public void loadPageWithCopyPaste(int itr)
    {

    }

}

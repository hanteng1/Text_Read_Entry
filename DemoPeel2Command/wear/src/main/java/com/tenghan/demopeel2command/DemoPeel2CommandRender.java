package com.tenghan.demopeel2command;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;

import com.eschao.android.widget.pageflip.GLPoint;
import com.eschao.android.widget.pageflip.modify.PageModify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by hanteng on 2017-09-13.
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

    private int mAngleNumRight = 2;
    private int mAngleNumLeft = 1;
    private int mDistanceNum = 5;

    //commands
    private int font_size;
    private ArrayList<Integer> font_color;
    private ArrayList<String> contact_name;

    //1 - choose color
    //2 - choose size
    //3 - choose name
    //4 - new page
    public int mTask = 0;
    public int mCorner = -1;
    //actual select
    public int mAngleActual = -1;
    public int mDistanceActual = -1;
    public float mContinuousActual = -1;
    private float maxAngle = (float)Math.PI / 2;
    private float maxDistance = 120;
    private float reservedDistance = 40;


    public String[] commands = {"Contact", "Font", "NewPage", "Empty"};

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

        //intial values
        font_size = 20;
        font_color = new ArrayList<Integer>();
        font_color.add(Color.rgb(200, 100, 100));
        font_color.add(Color.rgb(20, 20, 20));
        font_color.add(Color.rgb(100, 200, 100));
        font_color.add(Color.rgb(150, 150, 150));
        font_color.add(Color.rgb(100, 100, 200));

        contact_name = new ArrayList<String>();
        contact_name.add("Jason");
        contact_name.add("Mary");
        contact_name.add("Eddy");
        contact_name.add("Grace");
        contact_name.add("Hwa");

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

    public void setCorner(GLPoint point)
    {
        if(point.x > 0 && point.y > 0)
        {
            mCorner = 1;
        }else if(point.x < 0 && point.y > 0)
        {
            mCorner = 0;
        }else if(point.x < 0 && point.y < 0)
        {
            mCorner = 3;
        }else if(point.x > 0 && point.y < 0)
        {
            mCorner = 2;
        }
    }

    public void LoadTextures(){
        mPageFlipAbstract.deleteUnusedTextures();
        PageModify[] pages = mPageFlipAbstract.getPages();

        //set the first page
        if(!pages[0].isFrontTextureSet())
        {
            loadPageWithContent(0);
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

        //set based on the task
        //1 - choose color
        //2 - choose size
        //3 - choose name
        //4 - new page

        /**
         *    ---------
         *   |0       1|
         *   |         |
         *   |         |
         *   |3       2|
         *    ---------
         */

        //use the actual corner

        PageModify page = mPageFlipAbstract.getPages()[itrp];
        page.waiting4TextureUpdate = true;
    }


    public void ResetValues()
    {
        mTask = 0;
        mCorner = -1;

        mAngleActual = -1;
        mDistanceActual = -1;
        mContinuousActual = -1;
    }


    //these are drawing textures
    public void loadPageWithContent(int pageIndex)
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
        int fontSize = calcFontSize(10);
        p.setColor(Color.GRAY);
        p.setStrokeWidth(1);
        p.setAntiAlias(true);
        //p.setShadowLayer(5.0f, 8.0f, 8.0f, Color.BLACK);
        p.setTextSize(fontSize);
        //String text = Alphabet[number];

        String text = "Meeting at 10:30 AM";

        float textWidth = p.measureText(text);

        PointF textCursor = new PointF();
        textCursor.set(width / 2 - textWidth / 2, height / 2 );

        mCanvas.drawText(text, textCursor.x, textCursor.y, p);
    }

    public void loadPageWithCommands(int number, String[] commandIds)
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


        if(MainActivity.getSharedInstance().mGestureService.activiatedCommandIndex == -1)
        {
            //draw commands
            for(int itrc = 0; itrc < commandIds.length; itrc++)
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

        }else if(MainActivity.getSharedInstance().mGestureService.activiatedCommandIndex > -1)
        {
            //meaning it's state 3 and one corner has been active
            PointF origin = new PointF();
            if(mCorner == 0)
            {
                origin.set(0, 0);
            }else if(mCorner == 1)
            {
                origin.set(width, 0);
            }else if(mCorner == 2)
            {
                origin.set(width, height);
            }else if(mCorner == 3)
            {
                origin.set(0, height);
            }

            //draw tasks
            if(MainActivity.getSharedInstance().mDemoView.mDemo.currentPageLock == 0)
            {

                //1 - choose color
                //2 - choose size
                //3 - choose name
                //4 - new page

                if(mTask == 1)
                {

                    float segAngle = maxAngle / mAngleNumRight;
                    float segDis = maxDistance / mDistanceNum;

                    //choose a color on right-top
                    for(int itr = 0; itr < mDistanceNum; itr++)
                    {
                        float targetLength = segDis * (itr + 0.5f) + reservedDistance;
                        float targetAngle = segAngle * ( mAngleActual + 0.5f) + maxAngle * mCorner;
                        float targetX = origin.x + targetLength * (float)Math.cos(targetAngle);
                        float targetY = origin.y + targetLength * (float)Math.sin(targetAngle);

                        int color = font_color.get(itr);
                        float scale = 30;
                        p.setColor(color);
                        p.setStyle(Paint.Style.FILL);
                        mCanvas.drawCircle(targetX, targetY, scale*0.5f, p);

                    }

                }else if(mTask == 2)
                {
                    //choose size, continuous, always update
                    p.setColor(Color.GRAY);
                    p.setStrokeWidth(1);
                    p.setAntiAlias(true);
                    String text = "Aa";

                    int fontSize = calcFontSize((int)MainActivity.getSharedInstance().mGestureService.curDistance);
                    p.setTextSize(fontSize/2);
                    float y = p.getTextSize();
                    float x = width - p.measureText(text);
                    mCanvas.drawText(text, x, y, p);

                }else if(mTask == 3)
                {
                    //choose name


                }else if(mTask == 4)
                {
                    //none
                }


            }else if(MainActivity.getSharedInstance().mDemoView.mDemo.currentPageLock == 1)
            {
                if(mTask == 3)
                {
                    //choose a second layer name

                }
            }
        }

    }


    //determine the current selection and visualizing task
    public void selectedSegment(int pageindex,  PointF cursor)
    {
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();

        PointF origin = new PointF();
        if(mCorner == 0)
        {
            origin.set(0, 0);
        }else if(mCorner == 1)
        {
            origin.set(width, 0);
        }else if(mCorner == 2)
        {
            origin.set(width, height);
        }else if(mCorner == 3)
        {
            origin.set(0, height);
        }else
        {
            return;
        }

        //first dis
        float dis = (float)Math.sqrt((cursor.x -  origin.x) * (cursor.x -  origin.x) +
                (cursor.y - origin.y) * (cursor.y - origin.y)) ;

        float segDis = maxDistance / mDistanceNum;
        int disSegs = (int) ( (dis - reservedDistance )/ segDis);

        //then angle
        float ang = 0;
        if(mCorner == 0 || mCorner == 2)
        {
            ang = (float)Math.asin( Math.abs(cursor.y - origin.y)  / Math.abs(dis) );
        }else
        {
            ang = (float)Math.asin( Math.abs(cursor.x - origin.x)  / Math.abs(dis) );
        }

        float segAngle = 0;

        if(mCorner == 1)
        {
            segAngle = maxAngle / mAngleNumRight;
        }else
        {
            segAngle = maxAngle / mAngleNumLeft;
        }

        int angSegs = (int) (ang / segAngle);

        //determine which task and which angle actual and distanceactual
        if(angSegs != mAngleActual)
        {
            //task update
            if(mCorner == 1 && angSegs == 1)
            {
                //the only continuus change
                mTask = 2; // choose size
            }else if(mCorner == 1 && angSegs == 0)
            {
                mTask = 1; // choose color
            }else if(mCorner == 0 && angSegs == 0)
            {
                mTask = 3; //choose name
            }else if(mCorner == 2 && angSegs == 0){
                mTask = 4; //new page
            }

            mAngleActual = angSegs;

            //refresh
            ReloadTexture(pageindex);
        }


        if(mTask == 2){

            if( (2*dis /3) != mContinuousActual  )
            {
                mContinuousActual = (2*dis /3);
                //refresh
                ReloadTexture(pageindex);
            }

        }else if(mTask == 1 || mTask == 3)
        {
            if(dis >= reservedDistance && disSegs != mDistanceActual)
            {
                mAngleActual = angSegs;
                //refresh
                ReloadTexture(pageindex);
            }

        }

    }


}

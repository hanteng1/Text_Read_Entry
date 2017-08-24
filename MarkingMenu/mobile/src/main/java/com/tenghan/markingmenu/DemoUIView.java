package com.tenghan.markingmenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by hanteng on 2017-08-22.
 */

public class DemoUIView extends View {

    private final static String TAG = "DemoUIView";
    private Paint inputPaint = new Paint();
    private Paint bgPaint = new Paint();
    private Paint subMenuPaint = new Paint();
    private Paint subMenuSliderPaint = new Paint();
    private Paint strokePaint = new Paint();
    private int screenWidth, screenHeight;
    private Path touchPath = new Path();
    private int touchLength = 10;
    private ArrayList<PointF> touchPoints;

    private String[] menuItems = {"copy", "paste", "color", "size"};
    private float menuDistance = 80.0f;
    private PointF menuCenter = new PointF();

    private Runnable strokeDeleting;
    private Handler mHandler;

    public boolean isTriggered = false;
    public boolean isSubMenuing = false;
    private float subMenuWidth = 40;

    private ArrayList<Integer> colorCode;
    private Random rand;


    public DemoUIView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inputPaint.setAntiAlias(true);
        inputPaint.setStrokeWidth(0);
        inputPaint.setColor(Color.RED);
        inputPaint.setTextSize(28);
        //inputPaint.setStyle(Paint.Style.STROKE);
        inputPaint.setStrokeJoin(Paint.Join.ROUND);

        subMenuSliderPaint.setAntiAlias(true);
        subMenuSliderPaint.setStrokeWidth(2);
        subMenuSliderPaint.setColor(Color.BLACK);
        subMenuSliderPaint.setStyle(Paint.Style.STROKE);
        subMenuSliderPaint.setStrokeJoin(Paint.Join.ROUND);

        subMenuPaint.setAntiAlias(true);
        subMenuPaint.setStrokeWidth(0);
        subMenuPaint.setColor(Color.RED);
        subMenuPaint.setStyle(Paint.Style.FILL);

        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(1);
        strokePaint.setColor(Color.BLUE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);

        menuCenter.x = 200.0f;
        menuCenter.y = 200.0f;

        this.setBackgroundColor(Color.parseColor("#559B9B9B"));

        //generate color code
        colorCode = new ArrayList<Integer>();
        rand = new Random();
        generateColor(100);

        touchPoints = new ArrayList<PointF>();

        mHandler = new Handler();
        strokeDeleting = new Runnable() {
            @Override
            public void run() {

                if (touchPoints.size() > 1)
                {
                    touchPoints.remove(0);
                    invalidate();
                }

                mHandler.postDelayed(this, 50);
            }
        };

        mHandler.post(strokeDeleting);
    }


    private void generateColor(int num)
    {
        int maxDistance = 20;

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

    public void setDimension(int x, int y)
    {
        screenWidth = x;
        screenHeight = y;
    }

    private void clear()
    {
        touchPath.reset();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if(!isTriggered)
        {
            return;
        }

        //set background color

        /**
         *          1
         *          |
         *          |
         *    0 --------- 2
         *          |
         *          |
         *          3
         */
        //for the commands tags
        for(int itrc = 0; itrc < menuItems.length; itrc++)
        {
            int posX = 1;
            int posY = 1;
            if(itrc == 0 || itrc == 2)
            {
                posY = 0;
                posX = 1;
                if(itrc == 0)
                {
                    posX = -1;
                }
            }else if(itrc == 1 || itrc == 3)
            {
                posX = 0;
                posY = 1;
                if(itrc == 3)
                {
                    posY = -1;
                }
            }
            canvas.drawText(menuItems[itrc], menuCenter.x + posX * menuDistance, menuCenter.y + posY * menuDistance, inputPaint);
        }


        //draw touch path
        if (touchPoints.size() > 0)
        {
            touchPath.reset();
            for (int itrt = 0; itrt < touchPoints.size(); itrt++)
            {
                if(itrt == 0)
                {
                    touchPath.moveTo(touchPoints.get(itrt).x, touchPoints.get(itrt).y);
                }else
                {
                    touchPath.lineTo(touchPoints.get(itrt).x, touchPoints.get(itrt).y);
                }
            }

            canvas.drawPath(touchPath, strokePaint);
        }

        drawColorPanel(canvas, true, true);
    }

    public void onFingerDown(float x, float y)
    {
        //touchPath.reset();
        //touchPath.moveTo(x, y);
        touchPoints.clear();
        touchPoints.add(new PointF(x, y));
    }

    public void onFingerMove(float x, float y)
    {
        //touchPath.lineTo(x, y);
        if(isTriggered)
        {
            touchPoints.add(new PointF(x, y));
            invalidate();
        }

    }

    public void onFingerUp(float x, float y)
    {
        isTriggered = false;
        invalidate();
    }

    public void onLongPressed(float x, float y)
    {
        menuCenter.set(x, y);
        isTriggered = true;
        invalidate();
    }


    private void drawColorPanel(Canvas canvas, boolean isVertical, boolean isleftTop)
    {
        float mWidth;
        float mHeight;
        float anchorX;
        float anchorY;

        if(isVertical)
        {
            mWidth = subMenuWidth;
            mHeight = screenHeight;
            anchorY = 0;
            //on left or right
            if(isleftTop)
            {
                //on left
                anchorX = 0;

                //draw a slider
                canvas.drawLine(anchorX, anchorY + mHeight / 2, anchorX + mWidth * 2, anchorY + mHeight / 2, subMenuSliderPaint);
            }else
            {
                //on right
                anchorX = screenWidth - subMenuWidth;

                //draw a slider
                canvas.drawLine(anchorX - mWidth, anchorY + mHeight / 2, anchorX + mWidth, anchorY + mHeight / 2, subMenuSliderPaint);
            }

            //draw color bars
            //divide the bars in vertical
            //0 - 1000, 10 * 10 * 10

            float segment = mHeight / 100;
            for(int itrc = 0; itrc < 100; itrc++)
            {
                subMenuPaint.setColor(colorCode.get(itrc));
                canvas.drawRect(anchorX , anchorY + segment * itrc, anchorX + mWidth, anchorY + segment * itrc + segment, subMenuPaint);
            }

        }else
        {
            //on top or bottom
            mWidth = screenWidth;
            mHeight = subMenuWidth;
            anchorX = 0;

            if(isleftTop)
            {
                //on top
                anchorY = 0;
                //draw a slider
                canvas.drawLine(anchorX + mWidth / 2, anchorY, anchorX + mWidth / 2, anchorY + mHeight * 2, subMenuSliderPaint);
            }else
            {
                //on bottom
                anchorY = screenHeight - subMenuWidth;

                //draw a slider
                canvas.drawLine(anchorX + mWidth / 2, anchorY - mHeight, anchorX + mWidth / 2, anchorY + mHeight, subMenuSliderPaint);
            }
        }





        //canvas.drawRect(anchorX , anchorY, anchorX + mWidth, anchorY + mHeight, subMenuPaint);

    }

    private void drawFontSizePanel (Canvas canvas, float x, float y)
    {

    }

    private void drawPageFlipPanel(Canvas canvas, float x, float y)
    {

    }

}

package com.tenghan.markingmenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hanteng on 2017-08-22.
 */

public class DemoUIView extends View {

    private Paint inputPaint = new Paint();
    private Paint bgPaint = new Paint();
    private Paint strokePaint = new Paint();
    private int screenWidth, screenHeight;
    private Path touchPath = new Path();
    private int touchLength = 10;

    private String[] menuItems = {"copy", "paste", "color", "size"};
    private float menuDistance = 80.0f;
    private PointF menuCenter = new PointF();

    public DemoUIView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inputPaint.setAntiAlias(true);
        inputPaint.setStrokeWidth(0);
        inputPaint.setColor(Color.RED);
        inputPaint.setTextSize(28);
        //inputPaint.setStyle(Paint.Style.STROKE);
        inputPaint.setStrokeJoin(Paint.Join.ROUND);

        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(1);
        strokePaint.setColor(Color.BLUE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);

        menuCenter.x = 200.0f;
        menuCenter.y = 200.0f;

        this.setBackgroundColor(Color.parseColor("#559B9B9B"));
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
        canvas.drawPath(touchPath, strokePaint);

    }

    public void onFingerDown(float x, float y)
    {
        touchPath.reset();
        touchPath.moveTo(x, y);
    }

    public void onFingerMove(float x, float y)
    {
        touchPath.lineTo(x, y);
        //if(touchPath)
        invalidate();
    }

    public void onFingerUp()
    {

    }


}

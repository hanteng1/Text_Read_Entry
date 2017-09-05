package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by hanteng on 2017-08-21.
 */

public class DemoUIView extends View {

    private Paint inputPaint = new Paint();
    private int screenWidth, screenHeight;


    private Path touchPath = new Path();
    private Paint touchPaint = new Paint();



    private Context mContext;

    private boolean isActive;

    /**
     * 1 - peel2command
     * 2 - notification
     * 3 - copy and paste
     */
    public int demoIndex = 0;


    //used to test
    public PointF origin = new PointF();
    public PointF corner = new PointF();
    public PointF peelOne = new PointF();
    public PointF peelTwo = new PointF();


    public DemoUIView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        inputPaint.setAntiAlias(true);
        inputPaint.setStrokeWidth(1);
        inputPaint.setColor(Color.RED);
        inputPaint.setStyle(Paint.Style.STROKE);
        inputPaint.setStrokeJoin(Paint.Join.ROUND);


        //set touch paint, transparent
        touchPaint.setAntiAlias(true);
        touchPaint.setStrokeWidth(20);
        touchPaint.setColor(Color.GREEN);
        touchPaint.setStyle(Paint.Style.STROKE);
        touchPaint.setStrokeJoin(Paint.Join.ROUND);

    }

    protected int calcFontSize(int size)
    {
        return (int)(size * mContext.getResources().getDisplayMetrics().scaledDensity);
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

        if(demoIndex == 0)
        {
            //origin to corner
            canvas.drawLine(origin.x, origin.y, corner.x, corner.y, inputPaint);
            //flipped
            canvas.drawLine(peelOne.x, peelOne.y, peelTwo.x, peelTwo.y, inputPaint);
        }else if(demoIndex == 3)
        {
            //copy and paste demo
            canvas.drawPath(touchPath, touchPaint);
        }

    }


    public void onDoubleTap(float x, float y)
    {
        clear();
        touchPath.moveTo(x, y);
    }

    public void onTapMove(float x, float y)
    {
        touchPath.lineTo(x, y);
        invalidate();
    }

    public void onTapUp(float x, float y)
    {

    }





    public void activate()
    {
        isActive = true;
        invalidate();
    }

    public void deActivate()
    {
        isActive = false;
        invalidate();
    }


}

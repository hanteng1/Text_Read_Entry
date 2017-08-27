package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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

    private Context mContext;

    private boolean isActive;

    //

    Paint p = new Paint();
    private ArrayList<Integer> fontSizes;
    private int totalFontSize = 100;
    private int presentedFontSize = 10;
    private int fontSizeAnchor = 1;
    private int fontBandDistance = 20;
    private float sin45;


    public DemoUIView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        inputPaint.setAntiAlias(true);
        inputPaint.setStrokeWidth(0);
        inputPaint.setColor(Color.RED);
        //inputPaint.setStyle(Paint.Style.STROKE);
        inputPaint.setStrokeJoin(Paint.Join.ROUND);

        fontSizes = new ArrayList<Integer>();
        generateFontSize(totalFontSize);
        sin45 = (float)Math.sin(Math.PI / 4);

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
        //canvas.drawPath(touchPath, inputPaint);
        //canvas.drawRect(33, 33, 77, 60, inputPaint);

        //seems like using a standalone view is not a good idea
        //maybe can be used for effects..
        if(!isActive)
            return;

        //font size visualization
        p.setColor(Color.GRAY);
        p.setStrokeWidth(1);
        p.setAntiAlias(true);
        String text = "Aa";
        float y = fontBandDistance * 1 * sin45;
        float x = fontBandDistance * 1 * sin45;

        for(int itrf = 0; itrf < (0 + presentedFontSize); itrf++)
        {
            int fontSize = calcFontSize(fontSizes.get(fontSizeAnchor + itrf));
            p.setTextSize(fontSize);

            x += fontBandDistance * sin45;
            y += fontBandDistance * sin45;

            canvas.drawText(text, x - p.measureText(text)/2, y, p);
        }


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

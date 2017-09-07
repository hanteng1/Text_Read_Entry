package com.tenghan.markingmenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hanteng on 2017-08-22.
 */

public class ContentView extends View {

    private static final String TAG = "ContentView";
    private Paint inputPaint = new Paint();
    private int screenWidth, screenHeight;
    private Path touchPath = new Path();
    private PointF textCursor = new PointF();
    private float lineSpace = 20.f;
    private float textSpace = 5.0f;

    //bounding box width
    private int boundX = 3;
    private int boundY = 3;

    public ContentView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inputPaint.setAntiAlias(true);
        inputPaint.setColor(Color.GREEN);
        inputPaint.setStyle(Paint.Style.FILL);
        inputPaint.setStrokeJoin(Paint.Join.ROUND);

        textCursor.x = 0;
        textCursor.y = 0;

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

        //draw a rectangle color
        //canvas.drawRect(boundX, boundY, screenWidth / 2, screenHeight - boundY, inputPaint);

    }
}

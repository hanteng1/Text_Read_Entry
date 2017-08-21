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

/**
 * Created by hanteng on 2017-08-21.
 */

public class DemoUIView extends View {

    private Paint inputPaint = new Paint();
    private int screenWidth, screenHeight;
    private Path touchPath = new Path();

    public DemoUIView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inputPaint.setAntiAlias(true);
        inputPaint.setStrokeWidth(0);
        inputPaint.setColor(Color.RED);
        //inputPaint.setStyle(Paint.Style.STROKE);
        inputPaint.setStrokeJoin(Paint.Join.ROUND);

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

    }

}

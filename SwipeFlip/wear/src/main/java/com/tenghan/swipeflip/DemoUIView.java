package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hanteng on 2017-09-18.
 */

public class DemoUIView extends View{


    //used to test
    public PointF origin = new PointF();
    public PointF corner = new PointF();
    public PointF peelOne = new PointF();
    public PointF peelTwo = new PointF();

    private Paint inputPaint = new Paint();

    public DemoUIView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        inputPaint.setAntiAlias(true);
        inputPaint.setFilterBitmap(true);
        inputPaint.setStrokeWidth(5);
        inputPaint.setColor(Color.BLUE);
        inputPaint.setStyle(Paint.Style.STROKE);
        inputPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawLine(origin.x, origin.y, corner.x, corner.y, inputPaint);
        canvas.drawLine(peelOne.x, peelOne.y, peelTwo.x, peelTwo.y, inputPaint);
    }
}

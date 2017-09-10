package jcli.research.com.swipetap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by hanteng on 2017-09-11.
 */

public class ConTargetView extends View {

    private final static String TAG = "ConTargetView";
    private Paint inputPaint = new Paint();
    private Paint subMenuPaint = new Paint();
    private Paint p = new Paint();
    private int screenWidth, screenHeight;

    public int mTask;
    public int mClose;
    public int mCloseValue;

    public float mContinuousMax = 120;
    public float reservedDistance = 40;
    public float mContinuousTarget = -1;
    public float mContinuousActual = -1;

    public ConTargetView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        inputPaint.setAntiAlias(true);
        inputPaint.setStrokeWidth(0);
        inputPaint.setColor(Color.RED);
        inputPaint.setTextSize(28);
        //inputPaint.setStyle(Paint.Style.STROKE);
        inputPaint.setStrokeJoin(Paint.Join.ROUND);

        subMenuPaint.setAntiAlias(true);
        subMenuPaint.setStrokeWidth(0);
        subMenuPaint.setColor(Color.RED);
        subMenuPaint.setStyle(Paint.Style.FILL);
    }

    public void setTask(int _task, int _closevalue)
    {
        mTask = _task;
        mCloseValue = _closevalue;

        mContinuousTarget = mContinuousMax * mCloseValue + reservedDistance;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        p.setFilterBitmap(true);
        //draw the target
        int fontSize = calcFontSize(20);
        p.setColor(Color.GRAY);
        p.setStrokeWidth(1);
        p.setAntiAlias(true);
        p.setTextSize(fontSize);

        //draw target
        if(mTask == 4)
        {
            //font size
            if(mContinuousTarget != -1 && mContinuousTarget < (mContinuousMax + reservedDistance))
            {
                Path path = new Path();
                float mx = screenWidth/2;
                float my = screenHeight/2;
                float offx = 320 - mContinuousTarget / 2 - mx;
                float offy = mContinuousTarget / 2 - my;

                path.moveTo(320 - offx, 0 - offy);
                path.lineTo(320 - offx, mContinuousTarget- offy);
                path.lineTo(320 - mContinuousTarget - offx, mContinuousTarget- offy);
                path.lineTo(320 - offx, 0- offy);

                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.BLUE);
                p.setStrokeWidth(2);
                canvas.drawPath(path, p);

                path.reset();
                path.moveTo(300 - offx, 0 - offy);
                path.lineTo(300 - offx, mContinuousTarget- offy);
                canvas.drawPath(path, p);

            }
        }else if(mTask == 5)
        {
            //color
            Path path = new Path();
            float mx = screenWidth/2;
            float my = screenHeight/2;
            float offx = 320 - 160 / 2 - mx;
            float offy = 160 / 2 - my;

            path.moveTo(320 - offx, 0 - offy);
            path.lineTo(320 - offx, 160 - offy);
            path.lineTo(320 - 160 - offx, 160- offy);
            path.lineTo(320 - offx, 0- offy);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(255, 0, 0, (int)(255 * (mContinuousTarget / (mContinuousMax + reservedDistance) ))));
            canvas.drawPath(path, p);

        }else if(mTask == 6)
        {

            //wegght
            Path path = new Path();
            float mx = screenWidth/2;
            float my = screenHeight/2;
            float offx = 320 - 160 / 2 - mx;
            float offy = 160 / 2 - my;

            path.moveTo(320 - offx, 0 - offy);
            path.lineTo(320 - offx, 160 - offy);
            path.lineTo(320 - 160 - offx, 160- offy);
            path.lineTo(320 - offx, 0- offy);

            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.BLUE);
            p.setStrokeWidth( 20 * (mContinuousTarget / (mContinuousMax + reservedDistance) ));

            canvas.drawPath(path, p);
        }

    }

    protected int calcFontSize(int size)
    {
        return (int)(size * MainActivity.getSharedInstance().getResources().getDisplayMetrics().scaledDensity);
    }
}

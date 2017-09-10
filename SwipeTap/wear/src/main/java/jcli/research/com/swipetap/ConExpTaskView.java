package jcli.research.com.swipetap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by jchrisli on 2017-09-09.
 */

public class ConExpTaskView extends View {

    private final static String TAG = "exptaskview";

    public float mContinuousMax = 120;
    public float reservedDistance = 40;
    public float mContinuousTarget = -1;
    public float mContinuousActual = -1;

    public int mTask;
    public int mClose;
    public float mCloseValue;


    private Paint inputPaint = new Paint();
    private Paint subMenuPaint = new Paint();
    private Paint p = new Paint();



    public void setTask (int _task, int _closevalue) {
        mTask = _task + 1;
        mCloseValue = _closevalue / 100.0f;

        mContinuousTarget = mContinuousMax * mCloseValue + reservedDistance;

        Log.d(TAG, "task " + mTask + " , value " + mContinuousTarget);
        invalidate();
    }

    public ConExpTaskView(Context context, AttributeSet attrs) {
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

    public void updateState (int progress) {
        //Set the progress according
        mContinuousActual = progress * 2.0f / 3.0f;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        p.setFilterBitmap(true);
        p.setColor(Color.GRAY);
        p.setStrokeWidth(1);
        p.setAntiAlias(true);

        if(mTask == 4)
        {
            //size
            //target
            if(mContinuousTarget != -1 && mContinuousTarget < (mContinuousMax + reservedDistance))
            {
                Path path = new Path();
                path.moveTo(320, 0);
                path.lineTo(320, mContinuousTarget);
                path.lineTo(320 - mContinuousTarget, mContinuousTarget);
                path.lineTo(320, 0);

                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.BLUE);
                p.setStrokeWidth(2);
                canvas.drawPath(path, p);

            }

            //actual
            if(mContinuousActual != -1 && mContinuousActual < (mContinuousMax + reservedDistance))
            {
                Path path = new Path();
                path.moveTo(320, 0);
                path.lineTo(320 - mContinuousActual, 0);
                path.lineTo(320 - mContinuousActual, mContinuousActual);
                path.lineTo(320, 0);

                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.GREEN);
                p.setStrokeWidth(2);
                canvas.drawPath(path, p);
            }

        }else if(mTask == 5)
        {

            //color
            //target
            if(mContinuousTarget != -1 && mContinuousTarget < (mContinuousMax + reservedDistance))
            {
                Path path = new Path();
                path.moveTo(320, 0);
                path.lineTo(320, 160);
                path.lineTo(160, 160);
                path.lineTo(320, 0);

                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.argb(255, 0, 0, (int)(255 * (mContinuousTarget / (mContinuousMax + reservedDistance) ))));
                canvas.drawPath(path, p);

            }

            //actual
            if(mContinuousActual != -1 && mContinuousActual < (mContinuousMax + reservedDistance))
            {
                Path path = new Path();
                path.moveTo(320, 0);
                path.lineTo(160, 0);
                path.lineTo(160, 160);
                path.lineTo(320, 0);

                p.setStyle(Paint.Style.FILL);
                p.setColor(Color.argb(255, 0, 0, (int)(255 * (mContinuousActual / (mContinuousMax + reservedDistance) ))));
                canvas.drawPath(path, p);
            }

        }else if(mTask == 6)
        {
            //width
            //target
            if(mContinuousTarget != -1 && mContinuousTarget < (mContinuousMax + reservedDistance))
            {
                Path path = new Path();
                path.moveTo(320, 0);
                path.lineTo(320, 160);
                path.lineTo(160, 160);
                path.lineTo(320, 0);

                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.BLUE);
                p.setStrokeWidth( 20 * (mContinuousTarget / (mContinuousMax + reservedDistance) ));
                canvas.drawPath(path, p);

            }

            //actual
            if(mContinuousActual != -1 && mContinuousActual < (mContinuousMax + reservedDistance))
            {
                Path path = new Path();
                path.moveTo(320, 0);
                path.lineTo(160, 0);
                path.lineTo(160, 160);
                path.lineTo(320, 0);

                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.GREEN);
                p.setStrokeWidth( 20 * (mContinuousActual / (mContinuousMax + reservedDistance) ));
                canvas.drawPath(path, p);
            }
        }
    }
}

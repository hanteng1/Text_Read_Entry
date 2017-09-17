package com.tenghan.democopypaste;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by hanteng on 2017-09-18.
 */

public class DemoUIView extends View{


    private Paint mPaint;
    private Path mPath;
    private ArrayList<PointF> mPoints;

    private Bitmap mBitmap;
    private Bitmap editBitmap;
    private boolean normaldrawing = true;

    public DemoUIView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(20);
        mPaint.setColor(Color.GREEN);
        mPaint.setAlpha(150);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);

        mPath = new Path();
        mPoints = new ArrayList<PointF>();

        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.test);
        editBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.test);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if(normaldrawing)
        {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            canvas.drawPath(mPath, mPaint);
        }else
        {
            canvas.drawBitmap(editBitmap, 0, 0, mPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath.reset();
                mPoints.clear();
                mPath.moveTo(x, y);
                normaldrawing = true;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(x, y);
                mPoints.add(new PointF(x, y));
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                getEdge();
                normaldrawing = false;
                invalidate();
                break;
        }
        return true;
    }

    private void getEdge()
    {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(640, 960,
                Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);

        canvas.drawBitmap(mBitmap, 0, 0, mPaint);

        Path path = new Path();
        for(int itr = 0; itr < mPoints.size(); itr++)
        {
            if(itr == 0)
            {
                path.moveTo(mPoints.get(itr).x, mPoints.get(itr).y);
            }else
            {
                path.lineTo(mPoints.get(itr).x, mPoints.get(itr).y);
            }
        }

        path.setFillType(Path.FillType.INVERSE_EVEN_ODD);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(20);
        paint.setColor(Color.GREEN);
        paint.setAlpha(150);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        canvas.drawPath(path, paint);

        editBitmap = bitmap;

    }

}

package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.health.PackageHealthStats;
import android.os.health.SystemHealthManager;
import android.provider.ContactsContract;
import android.util.Log;

import com.eschao.android.widget.pageflip.modify.PageModify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by hanteng on 2017-08-28.
 */

public class StudyOneRender extends StudyRender{

    private final static String TAG = "StudyOneRender";

    private ArrayList<Path> paths;

    private float sin45;

    //set the base

    //need to change
    private float maxDistance = 160.0f;

    //need to test and change
    //the value is in pixels
    private float reservedDistance = 40.0f;


    private float maxAngle = (float)Math.PI / 2;
    private float maxAngleDegree = 90.0f;
    private float crossLength = 20;

    public int mCorner;
    public int mClose = -1;  //1 - close, 2 - middle, 3 - far
    public float mCloseValue;
    public int mAngleTarget = -1;
    public int mDistanceTargert = -1;
    public int mAngleNum;
    public int mDistanceNum;

    public int mAngleActual = -1;
    public int mDistanceActual = -1;

    private Random rand;


    public StudyOneRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                         Handler handler, int pageNo)
    {
        super(context, pageFlipAbstract, handler, pageNo);

        //maxDistance = mCanvas.getWidth() / 2;

        paths = new ArrayList<Path>();
        sin45 = (float)Math.sin(Math.PI / 4);

        rand = new Random();

    }

    public void LoadTextures(){

        //initial condition
        int[] curCondition = MainActivity.getSharedInstance().mStudyView.mStudy.obtainNextCondition();
        MainActivity.getSharedInstance().mStudyView.mStudy.currentCondition--;

        mCorner = curCondition[0];
        mAngleNum = curCondition[1];
        mDistanceNum = curCondition[2];

        mAngleTarget = rand.nextInt(mAngleNum);
        mDistanceTargert = rand.nextInt(mDistanceNum);

        mPageFlipAbstract.deleteUnusedTextures();
        PageModify[] pages = mPageFlipAbstract.getPages();

        //set the first page
        if(!pages[FIRST_PAGE].isFrontTextureSet())
        {
            loadPageWithTrialInfo();
            pages[FIRST_PAGE].setFrontTexture(mBitmap);
        }

        //set the second page
        if(!pages[SECOND_PAGE].isFrontTextureSet())
        {
            loadPageWithCondition();
            pages[SECOND_PAGE].setFrontTexture(mBitmap);
        }
    }

    public void ReloadFirstPageTexture()
    {
        PageModify page = mPageFlipAbstract.getPages()[FIRST_PAGE];
        page.waiting4TextureUpdate = true;
    }

    //this reload is corresponding to page flip gesture
    public void ReloadSecondPageTexture()
    {
        //update the real time target

        PageModify page = mPageFlipAbstract.getPages()[SECOND_PAGE];
        page.waiting4TextureUpdate = true;
    }

    //this is called by study trials
    public void ReloadTrial()
    {
        int[] curCondition = MainActivity.getSharedInstance().mStudyView.mStudy.obtainNextCondition();
        mCorner = curCondition[0];
        mAngleNum = curCondition[1];
        mDistanceNum = curCondition[2];
        mClose = curCondition[3];
        mCloseValue = curCondition[4] / 100.0f;

        mAngleTarget = rand.nextInt(mAngleNum);
        //mDistanceTargert = rand.nextInt(mDistanceNum);
        mDistanceTargert = (int)(mDistanceNum * mCloseValue);


        mDistanceActual = -1;
        mAngleActual = -1;

        PageModify page = mPageFlipAbstract.getPages()[FIRST_PAGE];
        page.waiting4TextureUpdate = true;

        page = mPageFlipAbstract.getPages()[SECOND_PAGE];
        page.waiting4TextureUpdate = true;
    }


    public void loadPageWithoutTrialInfo()
    {
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();

        Paint p = new Paint();
        p.setFilterBitmap(true);

        // 1. load/draw background bitmap
        Bitmap background = LoadBitmapTask.get(mContext).getBitmap();  //get the bitmap in queue
        Rect rect = new Rect(0, 0, width, height);
        mCanvas.drawBitmap(background, null, rect, p); //will this refresh the canvas? since it's using a new rect
        background.recycle();
        background = null;
    }

    public void loadPageWithTrialInfo()
    {
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();

        Paint p = new Paint();
        p.setFilterBitmap(true);

        // 1. load/draw background bitmap
        Bitmap background = LoadBitmapTask.get(mContext).getBitmap();  //get the bitmap in queue
        Rect rect = new Rect(0, 0, width, height);
        mCanvas.drawBitmap(background, null, rect, p); //will this refresh the canvas? since it's using a new rect
        background.recycle();
        background = null;

        // 2. load/draw page number
        int fontSize = calcFontSize(20);
        p.setColor(Color.GRAY);
        p.setStrokeWidth(1);
        p.setAntiAlias(true);
        p.setTextSize(fontSize);

        String conditionText = "" + (MainActivity.getSharedInstance().mStudyView.mStudy.currentCondition + 1)
                + " / " + MainActivity.getSharedInstance().mStudyView.mStudy.conditions.size();

        float textWidth = p.measureText(conditionText);
        float y = height / 2;
        float x = width / 2 - textWidth / 2;
        mCanvas.drawText(conditionText, x, y, p);

        conditionText = "AngleNum " + mAngleNum;
        textWidth = p.measureText(conditionText);
        y += (10 + fontSize);
        x = width / 2 - textWidth / 2;
        //mCanvas.drawText(conditionText, x, y, p);

        conditionText =  "DistanceNum " + mDistanceNum;
        textWidth = p.measureText(conditionText);
        y += (10 + fontSize);
        x = width / 2 - textWidth / 2;
        //mCanvas.drawText(conditionText, x, y, p);

        //draw target indicators
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);

        if(mCorner < 4)
        {
            //corners
            PointF origin = new PointF();
            if(mCorner == 0)
            {
                origin.set(0, 0);
            }else if(mCorner == 1)
            {
                origin.set(width, 0);
            }else if(mCorner == 2)
            {
                origin.set(width, height);
            }else if(mCorner == 3)
            {
                origin.set(0, height);
            }

            //angle paths
            float segAngle = maxAngle / mAngleNum;
            for(int itra = 1; itra < mAngleNum; itra++)
            {

                mCanvas.drawLine(origin.x +  reservedDistance * (float)Math.cos(segAngle * itra + (Math.PI/2) * mCorner),
                        origin.y + reservedDistance * (float)Math.sin(segAngle * itra + (Math.PI/2) * mCorner),
                        origin.x + (maxDistance + reservedDistance) * (float)Math.cos(segAngle * itra + (Math.PI/2) * mCorner),
                        origin.y + (maxDistance + reservedDistance) * (float)Math.sin(segAngle * itra + (Math.PI/2) * mCorner), p);

            }

            //distance paths
            float segDis = maxDistance / mDistanceNum;
            for(int itrd = 0; itrd < mDistanceNum + 1; itrd++)
            {

                //beging and end
                if(itrd == 0 || itrd == mDistanceNum)
                {
                    p.setColor(Color.RED);
                }else
                {
                    p.setColor(Color.GRAY);
                }

                RectF rectF = new RectF(origin.x - segDis * itrd - reservedDistance, origin.y - segDis * itrd - reservedDistance,
                        origin.x + segDis * itrd + reservedDistance, origin.y + segDis * itrd + reservedDistance);

                mCanvas.drawArc(rectF, mCorner * maxAngleDegree, maxAngleDegree, false, p);
            }

            //target
            float targetLength = segDis * (mDistanceTargert + 0.5f) + reservedDistance;
            float targetAngle = segAngle * ( mAngleTarget + 0.5f) + maxAngle * mCorner;
            float targetX = origin.x + targetLength * (float)Math.cos(targetAngle);
            float targetY = origin.y + targetLength * (float)Math.sin(targetAngle);

            p.setColor(Color.BLUE);
            drawTargetCross(mCanvas, p, targetX, targetY);

        }else
        {
            //edges
            float left = 0;
            float top = 0;
            float right = width;
            float bottom = height;

            ArrayList<float[]> lines = new ArrayList<float[]>();
            float segDis = maxDistance / mDistanceNum;

            float targetlength = segDis * (mDistanceTargert + 0.5f);
            float targetX = 0;
            float targetY = 0;

            if(mCorner == 4)
            {
                //top
                for(int itrd = 0; itrd < mDistanceNum + 1; itrd++)
                {
                    lines.add(new float[]{left, segDis * itrd + reservedDistance, right, segDis * itrd + reservedDistance});
                }

                targetX = (left + right) / 2;
                targetY = top + targetlength + reservedDistance;
            }else if(mCorner == 5)
            {
                //right
                for(int itrd = 0; itrd < mDistanceNum + 1; itrd++)
                {
                    lines.add(new float[]{right - segDis * itrd - reservedDistance, top, right - segDis * itrd - reservedDistance, bottom});
                }

                targetX = right - targetlength - reservedDistance;
                targetY = (top + bottom) / 2;
            }else if(mCorner == 6)
            {
                //bottom
                for(int itrd = 0; itrd < mDistanceNum + 1; itrd++)
                {
                    lines.add(new float[]{left, bottom - segDis * itrd - reservedDistance, right, bottom - segDis * itrd - reservedDistance});
                }

                targetX = (left + right) / 2;
                targetY = bottom - targetlength - reservedDistance;
            }else if(mCorner == 7)
            {
                //left
                for(int itrd = 0; itrd < mDistanceNum + 1; itrd++)
                {
                    lines.add(new float[]{left + segDis * itrd + reservedDistance, top, left + segDis * itrd + reservedDistance, bottom});
                }

                targetX = left + targetlength + reservedDistance;
                targetY = (top + bottom) / 2;
            }

            for(int itrl = 0; itrl < lines.size(); itrl++)
            {
                //begin and end
                if(itrl == 0 || itrl == (lines.size() - 1))
                {
                    p.setColor(Color.RED);
                }else
                {
                    p.setColor(Color.GRAY);
                }
                mCanvas.drawLine(lines.get(itrl)[0], lines.get(itrl)[1], lines.get(itrl)[2], lines.get(itrl)[3], p);
            }

            //target
            p.setColor(Color.BLUE);
            drawTargetCross(mCanvas, p, targetX, targetY);
        }

    }

    public void loadPageWithCondition()
    {
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();

        //general purpose
        Paint p = new Paint();
        p.setFilterBitmap(true);
        p.setColor(Color.GRAY);
        p.setStrokeWidth(3);
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.STROKE);

        // 1. load/draw background bitmap
        Bitmap background = LoadBitmapTask.get(mContext).getBitmap();  //get the bitmap in queue
        Rect rect = new Rect(0, 0, width, height);
        mCanvas.drawBitmap(background, null, rect, p); //will this refresh the canvas? since it's using a new rect
        background.recycle();
        background = null;

        //draw conditions

        if(mCorner < 4)
        {
            //corners
            PointF origin = new PointF();
            if(mCorner == 0)
            {
                origin.set(0, 0);
            }else if(mCorner == 1)
            {
                origin.set(width, 0);
            }else if(mCorner == 2)
            {
                origin.set(width, height);
            }else if(mCorner == 3)
            {
                origin.set(0, height);
            }

            paths.clear();

            //angle paths
            float segAngle = maxAngle / mAngleNum;
            for(int itra = 1; itra < mAngleNum; itra++)
            {
                mCanvas.drawLine(origin.x +  reservedDistance * (float)Math.cos(segAngle * itra + (Math.PI/2) * mCorner),
                        origin.y + reservedDistance * (float)Math.sin(segAngle * itra + (Math.PI/2) * mCorner),
                        origin.x + (maxDistance + reservedDistance) * (float)Math.cos(segAngle * itra + (Math.PI/2) * mCorner),
                        origin.y + (maxDistance + reservedDistance) * (float)Math.sin(segAngle * itra + (Math.PI/2) * mCorner), p);

            }

            //distance paths
            float segDis = maxDistance / mDistanceNum;
            for(int itrd = 0; itrd < mDistanceNum + 1; itrd++)
            {
                //beging and end
                if(itrd == 0 || itrd == mDistanceNum)
                {
                    p.setColor(Color.RED);
                }else
                {
                    p.setColor(Color.GRAY);
                }

                RectF rectF = new RectF(origin.x - segDis * itrd - reservedDistance, origin.y - segDis * itrd - reservedDistance,
                        origin.x + segDis * itrd + reservedDistance, origin.y + segDis * itrd + reservedDistance);

                mCanvas.drawArc(rectF, mCorner * maxAngleDegree, maxAngleDegree, false, p);
            }

            //target
            float targetLength = segDis * (mDistanceTargert + 0.5f) + reservedDistance;
            float targetAngle = segAngle * ( mAngleTarget + 0.5f) + maxAngle * mCorner;
            float targetX = origin.x + targetLength * (float)Math.cos(targetAngle);
            float targetY = origin.y + targetLength * (float)Math.sin(targetAngle);

            p.setColor(Color.BLUE);
            if(mDistanceTargert == mDistanceActual &&
                    mAngleTarget == mAngleActual)
            {
                p.setColor(Color.GREEN);
            }
            drawTargetCross(mCanvas, p, targetX, targetY);

            if(mAngleActual != -1 && mDistanceActual != -1)
            {
                float actualLength = segDis * (mDistanceActual + 0.5f) + reservedDistance;
                float actualAngle = segAngle * ( mAngleActual + 0.5f) + maxAngle * mCorner;
                float actualX = origin.x + actualLength * (float)Math.cos(actualAngle);
                float actualY = origin.y + actualLength * (float)Math.sin(actualAngle);
                p.setColor(Color.GREEN);
                drawMoveCross(mCanvas, p, actualX, actualY);
            }

        }else
        {
            //edges
            float left = 0;
            float top = 0;
            float right = width;
            float bottom = height;

            ArrayList<float[]> lines = new ArrayList<float[]>();
            float segDis = maxDistance / mDistanceNum;

            float targetlength = segDis * (mDistanceTargert + 0.5f);
            float targetX = 0;
            float targetY = 0;
            float actualLength = segDis * (mDistanceActual + 0.5f);
            float actualX = 0;
            float actualY = 0;

            if(mCorner == 4)
            {
                //top
                for(int itrd = 0; itrd < mDistanceNum + 1; itrd++)
                {
                    lines.add(new float[]{left, segDis * itrd  + reservedDistance, right, segDis * itrd + reservedDistance});
                }

                targetX = (left + right) / 2;
                targetY = top + targetlength + reservedDistance;

                actualX = targetX;
                actualY = top + actualLength + reservedDistance;
            }else if(mCorner == 5)
            {
                //right
                for(int itrd = 0; itrd < mDistanceNum + 1; itrd++)
                {
                    lines.add(new float[]{right - segDis * itrd - reservedDistance, top, right - segDis * itrd - reservedDistance, bottom});
                }

                targetX = right - targetlength - reservedDistance;
                targetY = (top + bottom) / 2;

                actualX = right - actualLength - reservedDistance;
                actualY = targetY;
            }else if(mCorner == 6)
            {
                //bottom
                for(int itrd = 0; itrd < mDistanceNum + 1; itrd++)
                {
                    lines.add(new float[]{left, bottom - segDis * itrd - reservedDistance, right, bottom - segDis * itrd - reservedDistance});
                }

                targetX = (left + right) / 2;
                targetY = bottom - targetlength - reservedDistance;

                actualX = targetX;
                actualY = bottom - actualLength - reservedDistance;
            }else if(mCorner == 7)
            {
                //left
                for(int itrd = 0; itrd < mDistanceNum + 1; itrd++)
                {
                    lines.add(new float[]{left + segDis * itrd + reservedDistance, top, left + segDis * itrd + reservedDistance, bottom});
                }

                targetX = left + targetlength + reservedDistance;
                targetY = (top + bottom) / 2;

                actualX = left + actualLength + reservedDistance;
                actualY = targetY;
            }

            for(int itrl = 0; itrl < lines.size(); itrl++)
            {
                //begin and end
                if(itrl == 0 || itrl == (lines.size() - 1))
                {
                    p.setColor(Color.RED);
                }else
                {
                    p.setColor(Color.GRAY);
                }
                mCanvas.drawLine(lines.get(itrl)[0], lines.get(itrl)[1], lines.get(itrl)[2], lines.get(itrl)[3], p);
            }

            //target

            p.setColor(Color.BLUE);
            if(mDistanceTargert == mDistanceActual)
            {
                p.setColor(Color.GREEN);
            }
            drawTargetCross(mCanvas, p, targetX, targetY);

            //draw actual
            if(mDistanceActual != -1)
            {
                p.setColor(Color.GREEN);
                drawMoveCross(mCanvas, p, actualX, actualY);
            }

        }

    }

    public void loadPageWithRealTimeFeedback()
    {

    }

    private void drawTargetCross(Canvas canvas, Paint paint, float x, float y)
    {
         canvas.drawLine(x - crossLength * sin45, y - crossLength * sin45,
                 x + crossLength * sin45, y + crossLength * sin45, paint);
        canvas.drawLine(x + crossLength * sin45, y - crossLength * sin45,
                x - crossLength * sin45, y + crossLength * sin45, paint);

    }

    private void drawMoveCross(Canvas canvas, Paint paint, float x, float y)
    {
        canvas.drawLine(x - crossLength, y,
                x + crossLength, y , paint);
        canvas.drawLine(x , y - crossLength,
                x , y + crossLength, paint);
    }

    //calcuate the intersection point of line 1-2 and 3-4
    private float[] calIntersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4)
    {
        float[] cross = new float[]{0, 0};

        if(x1 == x2 || x3 == x4)
        {
            return cross;
        }

        //y = ax + b
        float a1 = (y2-y1) / (x2 - x1);
        float b1 = y1 - a1 * x1;
        float a2 = (y4 - y3) / (x4 - x3);
        float b2 = y3 - a2*x3;

        //parallel
        if(a1 == a2)
        {
            return cross;
        }

        float x0 = -(b1-b2) / (a1 - a2);

        if(Math.min(x1, x2) < x0 && x0 < Math.max(x1, x2) &&
                Math.min(x3, x4) < x0  && x0 < Math.max(x3, x4))
        {
            cross[0] = x0;
            cross[1] = a1 * x0 + b1;
            return cross;
        }else
        {
            return cross;
        }

    }

    public void selectedSegment(PointF cursor)
    {
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();

        if(mCorner < 4)
        {
            PointF origin = new PointF();
            if(mCorner == 0)
            {
                origin.set(0, 0);
            }else if(mCorner == 1)
            {
                origin.set(width, 0);
            }else if(mCorner == 2)
            {
                origin.set(width, height);
            }else if(mCorner == 3)
            {
                origin.set(0, height);
            }

            //first dis
            float dis = (float)Math.sqrt((cursor.x -  origin.x) * (cursor.x -  origin.x) +
                    (cursor.y - origin.y) * (cursor.y - origin.y)) ;

            float segDis = maxDistance / mDistanceNum;
            int disSegs = (int) ( (dis - reservedDistance )/ segDis);

            //then angle
            float ang = 0;
            if(mCorner == 0 || mCorner == 2)
            {
                ang = (float)Math.asin( Math.abs(cursor.y - origin.y)  / Math.abs(dis) );
            }else
            {
                ang = (float)Math.asin( Math.abs(cursor.x - origin.x)  / Math.abs(dis) );
            }

            float segAngle = maxAngle / mAngleNum;
            int angSegs = (int) (ang / segAngle);

            //float targetLength = segDis * (disSegs + 0.5f);
            //float targetAngle = segAngle * ( angSegs + 0.5f) + maxAngle * mCorner;
            //float targetX = origin.x + targetLength * (float)Math.cos(targetAngle);
            //float targetY = origin.y + targetLength * (float)Math.sin(targetAngle);

            if(dis >= reservedDistance &&
                    disSegs != mDistanceActual || angSegs != mAngleActual)
            {
                mDistanceActual = disSegs;

                //Log.d(TAG, "dis actual "  + mDistanceActual);

                mAngleActual = angSegs;
                ReloadSecondPageTexture();
                //record the data
                long timestamp = System.currentTimeMillis();
                DataStorage.AddSample(MainActivity.getSharedInstance().mStudyView.mStudy.currentCondition,
                        mCorner,
                        mAngleNum,
                        mDistanceNum,
                        mClose,
                        mAngleTarget,
                        mDistanceTargert,
                        mAngleActual,
                        mDistanceActual,
                        timestamp);
            }
        }else
        {
            float left = 0;
            float top = 0;
            float right = width;
            float bottom = height;

            float segDis = maxDistance / mDistanceNum;
            float dis = 0;

            if(mCorner == 4)
            {
                dis = Math.abs(cursor.y - top) - reservedDistance;
            }else if(mCorner == 5)
            {
                dis = Math.abs(cursor.x - right) - reservedDistance;
            }else if(mCorner == 6)
            {
                dis = Math.abs(cursor.y - bottom) - reservedDistance;
            }else if(mCorner == 7)
            {
                dis = Math.abs(cursor.x - left) - reservedDistance;
            }

            int disSegs = (int) (dis / segDis);

            if(disSegs >= 0 &&
                    disSegs != mDistanceActual)
            {
                mDistanceActual = disSegs;
                ReloadSecondPageTexture();

                //record the data
                long timestamp = System.currentTimeMillis();
                DataStorage.AddSample(MainActivity.getSharedInstance().mStudyView.mStudy.currentCondition,
                        mCorner,
                        mAngleNum,
                        mDistanceNum,
                        mClose,
                        mAngleTarget,
                        mDistanceTargert,
                        mAngleActual,
                        mDistanceActual,
                        timestamp);
            }
        }

    }

}

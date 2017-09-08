package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;

import com.eschao.android.widget.pageflip.modify.PageModify;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by hanteng on 2017-09-08.
 */

public class StudyTwoRender extends StudyRender{

    private final static String TAG = "StudyTwoRender";

    private ArrayList<Path> paths;

    private float sin45;

    //need to change
    private float maxDistance = 160.0f;
    private float reservedDistance = 40.0f;

    private float maxAngle = (float)Math.PI / 2;
    private float maxAngleDegree = 90.0f;


    public int mTask;
    public int mCorner;
    public int mAngleTarget = -1;
    public int mDistanceTargert = -1;
    public int mAngleNum = 3;
    public int mDistanceNum = 5;
    public int mClose;
    public float mCloseValue;
    public int mAngleActual = -1;
    public int mDistanceActual = -1;

    public boolean obtainNext = true;

    private Random rand;


    //task 1, alphabet
    public ArrayList<String> task_alphabet;

    //task 2, number
    public ArrayList<Integer> task_number;

    //task 3, shape
    public ArrayList<PointF[]> task_shape;



    public StudyTwoRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                          Handler handler, int pageNo)
    {
        super(context, pageFlipAbstract, handler, pageNo);
        paths = new ArrayList<Path>();
        sin45 = (float)Math.sin(Math.PI / 4);
        rand = new Random();

        //task 1
        task_alphabet = new ArrayList<String>();
        task_alphabet.add("A");
        task_alphabet.add("B");
        task_alphabet.add("C");
        task_alphabet.add("D");
        task_alphabet.add("E");

        task_number = new ArrayList<Integer>();
        task_number.add(1);
        task_number.add(2);
        task_number.add(3);
        task_number.add(4);
        task_number.add(5);

        task_shape = new ArrayList<PointF[]>();
        task_shape.add(new PointF[]{new PointF(0, 0), new PointF(1, 0), new PointF(1, 1), new PointF(0, 1), new PointF(0, 0)});
        task_shape.add(new PointF[]{new PointF(0, 0), new PointF(1, 0), new PointF(1, 1), new PointF(0, 1), new PointF(0, 0)});
        task_shape.add(new PointF[]{new PointF(0, 0), new PointF(1, 0), new PointF(1, 1), new PointF(0, 1), new PointF(0, 0)});
        task_shape.add(new PointF[]{new PointF(0, 0), new PointF(1, 0), new PointF(1, 1), new PointF(0, 1), new PointF(0, 0)});
        task_shape.add(new PointF[]{new PointF(0, 0), new PointF(1, 0), new PointF(1, 1), new PointF(0, 1), new PointF(0, 0)});


    }

    //initial load
    public void LoadTextures()
    {

        mPageFlipAbstract.deleteUnusedTextures();
        PageModify[] pages = mPageFlipAbstract.getPages();

        //set the first page
        if(!pages[FIRST_PAGE].isFrontTextureSet())
        {
            loadPageWithoutTrialInfo();
            pages[FIRST_PAGE].setFrontTexture(mBitmap);
        }

        //set the second page
        if(!pages[SECOND_PAGE].isFrontTextureSet())
        {
            loadPageWithoutTrialInfo();
            pages[SECOND_PAGE].setFrontTexture(mBitmap);
        }


    }


    public void ReloadFirstPageTexture()
    {
        PageModify page = mPageFlipAbstract.getPages()[FIRST_PAGE];
        page.waiting4TextureUpdate = true;
    }

    //this reload is corresponding to page flip gesture in real time
    public void ReloadSecondPageTexture()
    {
        PageModify page = mPageFlipAbstract.getPages()[SECOND_PAGE];
        page.waiting4TextureUpdate = true;
    }


    //this is called by study trials
    //when the previous trial is done
    public void ReloadTrial()
    {
        int[] curTask;

        if(obtainNext == true)
        {
            curTask = MainActivity.getSharedInstance().mStudyView.mStudy.obtainNextTask();
        }else
        {
            curTask = MainActivity.getSharedInstance().mStudyView.mStudy.obtainCurrentTask();
        }

        obtainNext = false;

        mTask = curTask[0];
        mClose = curTask[1];
        mCloseValue = curTask[2] / 100.0f;

        if(mTask == 1)
        {
            //task 1 , alphabet
            mCorner = 0;
            mAngleTarget = 0;


        }else if(mTask == 2)
        {
            //task 2, number
            mCorner = 0;
            mAngleTarget = 1;
        }else if(mTask == 3)
        {
            //task 3, shape
            mCorner = 0;
            mAngleTarget = 2;
        }else if(mTask == 4)
        {
            //task 4
            mCorner = 1;
            mAngleTarget = 0;
        }else if(mTask == 5)
        {
            //task 5
            mCorner = 1;
            mAngleTarget = 1;
        }else if(mTask == 6)
        {
            //task 6
            mCorner = 1;
            mAngleTarget = 2;
        }

        mDistanceTargert = (int)(mDistanceNum * mCloseValue);

        mDistanceActual = -1;
        mAngleActual = -1;

        mPageFlipAbstract.getPages()[FIRST_PAGE].waiting4TextureUpdate = true;
        mPageFlipAbstract.getPages()[SECOND_PAGE].waiting4TextureUpdate = true;

    }

    //set up the second page
    public void loadPageWithCondition()
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


        float segAngle = maxAngle / mAngleNum;
        float segDis = maxDistance / mDistanceNum;


        //for references///////////
        paths.clear();
        p.setStyle(Paint.Style.STROKE);
        //angle paths
        for(int itra = 1; itra < mAngleNum; itra++)
        {
            mCanvas.drawLine(origin.x +  reservedDistance * (float)Math.cos(segAngle * itra + (Math.PI/2) * mCorner),
                    origin.y + reservedDistance * (float)Math.sin(segAngle * itra + (Math.PI/2) * mCorner),
                    origin.x + (maxDistance + reservedDistance) * (float)Math.cos(segAngle * itra + (Math.PI/2) * mCorner),
                    origin.y + (maxDistance + reservedDistance) * (float)Math.sin(segAngle * itra + (Math.PI/2) * mCorner), p);

        }

        //distance paths
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
        ///////////


        //target and actual
        if(mTask == 1 && mAngleActual == mAngleTarget)
        {
            //float segDis = maxDistance / mDistanceNum;
            //float segAngle = maxAngle / mAngleNum;
            for(int itr = 0; itr < mDistanceNum; itr++)
            {
                float targetLength = segDis * (itr + 0.5f) + reservedDistance;
                float targetAngle = segAngle * ( mAngleTarget + 0.5f) + maxAngle * mCorner;
                float targetX = origin.x + targetLength * (float)Math.cos(targetAngle);
                float targetY = origin.y + targetLength * (float)Math.sin(targetAngle);

                //p.setStyle(Paint.Style.STROKE);
                p.setTextSize(calcFontSize(20));
                p.setColor(Color.BLUE);
                String taskText = task_alphabet.get(itr);
                float textWidth = p.measureText(taskText);
                mCanvas.drawText(taskText, targetX - textWidth, targetY, p);
            }

            if(mDistanceActual != -1 && mDistanceActual < mDistanceNum)
            {
                float actualLength = segDis * (mDistanceActual + 0.5f) + reservedDistance;
                float actualAngle = segAngle * ( mAngleActual + 0.5f) + maxAngle * mCorner;
                float targetX = origin.x + actualLength * (float)Math.cos(actualAngle);
                float targetY = origin.y + actualLength * (float)Math.sin(actualAngle);

                //p.setStyle(Paint.Style.STROKE);
                p.setTextSize(calcFontSize(20));
                p.setColor(Color.GREEN);
                String taskText = task_alphabet.get(mDistanceActual);
                float textWidth = p.measureText(taskText);
                mCanvas.drawText(taskText, targetX - textWidth, targetY, p);
            }

        }else if(mTask == 2 && mAngleActual == mAngleTarget)
        {
            //float segDis = maxDistance / mDistanceNum;
            //float segAngle = maxAngle / mAngleNum;
            for(int itr = 0; itr < mDistanceNum; itr++)
            {
                float targetLength = segDis * (itr + 0.5f) + reservedDistance;
                float targetAngle = segAngle * ( mAngleTarget + 0.5f) + maxAngle * mCorner;
                float targetX = origin.x + targetLength * (float)Math.cos(targetAngle);
                float targetY = origin.y + targetLength * (float)Math.sin(targetAngle);

                //p.setStyle(Paint.Style.STROKE);
                p.setTextSize(calcFontSize(20));
                p.setColor(Color.BLUE);
                String taskText = "" + task_number.get(itr);
                float textWidth = p.measureText(taskText);
                mCanvas.drawText(taskText, targetX - textWidth, targetY, p);
            }

            if(mDistanceActual != -1 && mDistanceActual < mDistanceNum)
            {
                float actualLength = segDis * (mDistanceActual + 0.5f) + reservedDistance;
                float actualAngle = segAngle * ( mAngleActual + 0.5f) + maxAngle * mCorner;
                float targetX = origin.x + actualLength * (float)Math.cos(actualAngle);
                float targetY = origin.y + actualLength * (float)Math.sin(actualAngle);

                //p.setStyle(Paint.Style.STROKE);
                p.setTextSize(calcFontSize(20));
                p.setColor(Color.GREEN);
                String taskText = "" + task_number.get(mDistanceActual);
                float textWidth = p.measureText(taskText);
                mCanvas.drawText(taskText, targetX - textWidth, targetY, p);
            }

        }else if(mTask == 3 && mAngleActual == mAngleTarget)
        {
            //float segDis = maxDistance / mDistanceNum;
            //float segAngle = maxAngle / mAngleNum;
            for(int itr = 0; itr < mDistanceNum; itr++)
            {
                float targetLength = segDis * (itr + 0.5f) + reservedDistance;
                float targetAngle = segAngle * ( mAngleTarget + 0.5f) + maxAngle * mCorner;
                float targetX = origin.x + targetLength * (float)Math.cos(targetAngle);
                float targetY = origin.y + targetLength * (float)Math.sin(targetAngle);

                float scale = 20;
                float xoffSet = targetX - scale * 0.5f;
                float yoffSet = targetY - scale * 0.5f;

                Path path = new Path();
                path.moveTo(task_shape.get(itr)[0].x * scale + xoffSet, task_shape.get(itr)[0].y * scale + yoffSet);
                for(int itrs = 1; itrs < task_shape.get(itr).length; itrs++)
                {
                    path.lineTo(task_shape.get(itr)[itrs].x * scale + xoffSet, task_shape.get(itr)[itrs].y * scale + yoffSet);
                }

                p.setColor(Color.BLUE);
                p.setStyle(Paint.Style.FILL);
                mCanvas.drawPath(path, p);

            }

            if(mDistanceActual != -1 && mDistanceActual < mDistanceNum)
            {
                float actualLength = segDis * (mDistanceActual + 0.5f) + reservedDistance;
                float actualAngle = segAngle * ( mAngleTarget + 0.5f) + maxAngle * mCorner;
                float targetX = origin.x + actualLength * (float)Math.cos(actualAngle);
                float targetY = origin.y + actualLength * (float)Math.sin(actualAngle);

                float scale = 20;
                float xoffSet = targetX - scale * 0.5f;
                float yoffSet = targetY - scale * 0.5f;

                Path path = new Path();
                path.moveTo(task_shape.get(mDistanceActual)[0].x * scale + xoffSet, task_shape.get(mDistanceActual)[0].y * scale + yoffSet);
                for(int itrs = 1; itrs < task_shape.get(mDistanceActual).length; itrs++)
                {
                    path.lineTo(task_shape.get(mDistanceActual)[itrs].x * scale + xoffSet, task_shape.get(mDistanceActual)[itrs].y * scale + yoffSet);
                }

                p.setColor(Color.GREEN);
                p.setStyle(Paint.Style.FILL);
                mCanvas.drawPath(path, p);
            }


        }else if(mTask == 4)
        {

        }else if(mTask == 5)
        {

        }else if(mTask == 6)
        {

        }


    }

    //show the task on first page
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

        int fontSize = calcFontSize(20);
        p.setColor(Color.GRAY);
        p.setStrokeWidth(1);
        p.setAntiAlias(true);
        p.setTextSize(fontSize);

        String taskText = "" + (MainActivity.getSharedInstance().mStudyView.mStudy.currentTask + 1)
                + " / " + MainActivity.getSharedInstance().mStudyView.mStudy.taskCount;

        float textWidth = p.measureText(taskText);
        float y = height / 4;
        float x = width / 2 - textWidth / 2;
        mCanvas.drawText(taskText, x, y, p);

        //draw target
        if(mTask == 1)
        {
            //draw letter
            p.setTextSize(calcFontSize(40));
            p.setColor(Color.GREEN);
            y = height/2 + 10;
            taskText = task_alphabet.get(mDistanceTargert);
            textWidth = p.measureText(taskText);
            x = width / 2 - textWidth/2;
            mCanvas.drawText(taskText, x, y, p);
        }else if(mTask == 2)
        {
            //draw number
            p.setTextSize(calcFontSize(40));
            p.setColor(Color.GREEN);
            y = height/2 + 10;
            taskText = "" + task_number.get(mDistanceTargert);
            textWidth = p.measureText(taskText);
            x = width / 2 - textWidth/2;
            mCanvas.drawText(taskText, x, y, p);

        }else if(mTask == 3)
        {
            //draw shape
            float scale = 40;
            x = width / 2;
            y = height / 2;
            float xoffSet = x - scale * 0.5f;
            float yoffSet = y - scale * 0.5f;

            Path path = new Path();
            path.moveTo(task_shape.get(mDistanceTargert)[0].x * scale + xoffSet, task_shape.get(mDistanceTargert)[0].y * scale + yoffSet);
            for(int itrs = 1; itrs < task_shape.get(mDistanceTargert).length; itrs++)
            {
                path.lineTo(task_shape.get(mDistanceTargert)[itrs].x * scale + xoffSet, task_shape.get(mDistanceTargert)[itrs].y * scale + yoffSet);
            }

            p.setColor(Color.GREEN);
            p.setStyle(Paint.Style.FILL);
            mCanvas.drawPath(path, p);
        }else if(mTask == 4)
        {

        }else if(mTask == 5)
        {

        }else if(mTask == 6)
        {

        }



    }

    //remove the first page texture
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


    //not used
    public void loadPageWithRealTimeFeedback()
    {

    }


    public void selectedSegment(PointF cursor)
    {
        final int width = mCanvas.getWidth();
        final int height = mCanvas.getHeight();

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

        if(mTask < 4)
        {
            //discrete
            if(dis >= reservedDistance &&
                    disSegs != mDistanceActual || angSegs != mAngleActual)
            {
                mDistanceActual = disSegs;
                mAngleActual = angSegs;
                ReloadSecondPageTexture();
            }
        }else
        {

        }


    }



}

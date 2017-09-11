package com.tenghan.swipestudytwo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;

import com.eschao.android.widget.pageflip.modify.PageModify;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by hanteng on 2017-09-10.
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
    public int mTaskType;
    public int mCorner;
    public int mAngleTarget = -1;
    public int mDistanceTargert = -1;
    public int mAngleNum = 3;
    public int mDistanceNum = 5;
    public int mClose;
    public float mCloseValue;
    public int mAngleActual = -1;
    public int mDistanceActual = -1;


    public int isWrongTask = 0;
    public int isOvershot = 0;


    public boolean obtainNext = true;

    private Random rand;

    //task 1, alphabet
    public ArrayList<String> task_alphabet;

    //task 2, number
    public ArrayList<Integer> task_number;

    //task 3, shape
    public ArrayList<PointF[]> task_shape;



    //for continuous values
    public float mContinuousMax = maxDistance - reservedDistance ; //120 + 40
    public float mContinuousTarget = -1;
    public float mContinuousActual = -1;
    public float accuracyInterval = 0.1f;  // + -




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
        //this one will not be used
        task_shape.add(new PointF[]{new PointF(0, 0), new PointF(1, 0), new PointF(1, 1), new PointF(0, 1), new PointF(0, 0)});

        task_shape.add(new PointF[]{new PointF(0.5f, 0), new PointF(1, 1), new PointF(0, 1), new PointF(0.5f, 0)});
        task_shape.add(new PointF[]{new PointF(0.5f, 0), new PointF(1, 0.5f), new PointF(0.5f, 1), new PointF(0, 0.5f), new PointF(0.5f, 0)});
        task_shape.add(new PointF[]{new PointF(0, 0.25f), new PointF(1, 0.25f), new PointF(1, 0.75f), new PointF(0, 0.75f), new PointF(0, 25f)});

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

        //for discrete
        mDistanceTargert = (int)(mDistanceNum * mCloseValue);
        mDistanceActual = -1;
        mAngleActual = -1;

        //for continuous
        mContinuousTarget = mContinuousMax * mCloseValue + reservedDistance;  //40 - 160
        mContinuousActual = -1;

        isWrongTask = 0;
        isOvershot = 0;

        //save the trial start
        long currentTimestamp = System.currentTimeMillis();
        MainActivity.getSharedInstance().mStudyView.mStudy.trialStartTime = currentTimestamp;

        mTaskType = mTask < 4 ? 1 : 2;
        float distancevaluetarget = mTask < 4 ? mDistanceTargert : mContinuousTarget;

        DataStorage.AddSample(1, MainActivity.getSharedInstance().mStudyView.mStudy.currentTask,
                MainActivity.getSharedInstance().mStudyView.mStudy.currentAttempt,
                1, currentTimestamp, mCorner, mTask, mTaskType, mClose,
                mAngleTarget, distancevaluetarget,
                -1, -1);


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
//        paths.clear();
//        p.setStyle(Paint.Style.STROKE);
//        //angle paths
//        for(int itra = 1; itra < mAngleNum; itra++)
//        {
//            mCanvas.drawLine(origin.x +  reservedDistance * (float)Math.cos(segAngle * itra + (Math.PI/2) * mCorner),
//                    origin.y + reservedDistance * (float)Math.sin(segAngle * itra + (Math.PI/2) * mCorner),
//                    origin.x + (maxDistance + reservedDistance) * (float)Math.cos(segAngle * itra + (Math.PI/2) * mCorner),
//                    origin.y + (maxDistance + reservedDistance) * (float)Math.sin(segAngle * itra + (Math.PI/2) * mCorner), p);
//
//        }
//
//        //distance paths
//        for(int itrd = 0; itrd < mDistanceNum + 1; itrd++)
//        {
//            //beging and end
//            if(itrd == 0 || itrd == mDistanceNum)
//            {
//                p.setColor(Color.RED);
//            }else
//            {
//                p.setColor(Color.GRAY);
//            }
//
//            RectF rectF = new RectF(origin.x - segDis * itrd - reservedDistance, origin.y - segDis * itrd - reservedDistance,
//                    origin.x + segDis * itrd + reservedDistance, origin.y + segDis * itrd + reservedDistance);
//
//            mCanvas.drawArc(rectF, mCorner * maxAngleDegree, maxAngleDegree, false, p);
//        }
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
                p.setTextSize(calcFontSize(30));
                p.setColor(Color.BLUE);
                String taskText = task_alphabet.get(itr);
                float textWidth = p.measureText(taskText);
                float textHeight = p.getTextSize();
                mCanvas.drawText(taskText, targetX - textWidth/2, targetY + textHeight / 2, p);
            }

            if(mDistanceActual != -1 && mDistanceActual < mDistanceNum)
            {
                float actualLength = segDis * (mDistanceActual + 0.5f) + reservedDistance;
                float actualAngle = segAngle * ( mAngleActual + 0.5f) + maxAngle * mCorner;
                float targetX = origin.x + actualLength * (float)Math.cos(actualAngle);
                float targetY = origin.y + actualLength * (float)Math.sin(actualAngle);

                //p.setStyle(Paint.Style.STROKE);
                p.setTextSize(calcFontSize(30));
                p.setColor(Color.GREEN);
                String taskText = task_alphabet.get(mDistanceActual);
                float textWidth = p.measureText(taskText);
                float textHeight = p.getTextSize();
                mCanvas.drawText(taskText, targetX - textWidth/2, targetY + textHeight/2, p);
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
                p.setTextSize(calcFontSize(30));
                p.setColor(Color.BLUE);
                String taskText = "" + task_number.get(itr);
                float textWidth = p.measureText(taskText);
                float textHeight = p.getTextSize();
                mCanvas.drawText(taskText, targetX - textWidth/2, targetY + textHeight/2, p);
            }

            if(mDistanceActual != -1 && mDistanceActual < mDistanceNum)
            {
                float actualLength = segDis * (mDistanceActual + 0.5f) + reservedDistance;
                float actualAngle = segAngle * ( mAngleActual + 0.5f) + maxAngle * mCorner;
                float targetX = origin.x + actualLength * (float)Math.cos(actualAngle);
                float targetY = origin.y + actualLength * (float)Math.sin(actualAngle);

                //p.setStyle(Paint.Style.STROKE);
                p.setTextSize(calcFontSize(30));
                p.setColor(Color.GREEN);
                String taskText = "" + task_number.get(mDistanceActual);
                float textWidth = p.measureText(taskText);
                float textHeight = p.getTextSize();
                mCanvas.drawText(taskText, targetX - textWidth/2, targetY + textHeight / 2, p);
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

                float scale = 30;
                float xoffSet = targetX - scale * 0.5f;
                float yoffSet = targetY - scale * 0.5f;

                if(itr != 1)
                {
                    Path path = new Path();
                    path.moveTo(task_shape.get(itr)[0].x * scale + xoffSet, task_shape.get(itr)[0].y * scale + yoffSet);
                    for(int itrs = 1; itrs < task_shape.get(itr).length; itrs++)
                    {
                        path.lineTo(task_shape.get(itr)[itrs].x * scale + xoffSet, task_shape.get(itr)[itrs].y * scale + yoffSet);
                    }

                    p.setColor(Color.BLUE);
                    p.setStyle(Paint.Style.FILL);
                    mCanvas.drawPath(path, p);
                }else
                {
                    p.setColor(Color.BLUE);
                    p.setStyle(Paint.Style.FILL);
                    mCanvas.drawCircle(targetX, targetY, scale*0.5f, p);
                }


            }

            if(mDistanceActual != -1 && mDistanceActual < mDistanceNum)
            {
                float actualLength = segDis * (mDistanceActual + 0.5f) + reservedDistance;
                float actualAngle = segAngle * ( mAngleTarget + 0.5f) + maxAngle * mCorner;
                float targetX = origin.x + actualLength * (float)Math.cos(actualAngle);
                float targetY = origin.y + actualLength * (float)Math.sin(actualAngle);

                float scale = 30;
                float xoffSet = targetX - scale * 0.5f;
                float yoffSet = targetY - scale * 0.5f;

                if(mDistanceActual != 1)
                {
                    Path path = new Path();
                    path.moveTo(task_shape.get(mDistanceActual)[0].x * scale + xoffSet, task_shape.get(mDistanceActual)[0].y * scale + yoffSet);
                    for(int itrs = 1; itrs < task_shape.get(mDistanceActual).length; itrs++)
                    {
                        path.lineTo(task_shape.get(mDistanceActual)[itrs].x * scale + xoffSet, task_shape.get(mDistanceActual)[itrs].y * scale + yoffSet);
                    }

                    p.setColor(Color.GREEN);
                    p.setStyle(Paint.Style.FILL);
                    mCanvas.drawPath(path, p);
                }else
                {
                    p.setColor(Color.GREEN);
                    p.setStyle(Paint.Style.FILL);
                    mCanvas.drawCircle(targetX, targetY, scale*0.5f, p);
                }

            }


        }else if(mTask == 4 && mAngleActual == mAngleTarget)
        {
            // size

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
                mCanvas.drawPath(path, p);

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
                mCanvas.drawPath(path, p);
            }

        }else if(mTask == 5 && mAngleActual == mAngleTarget)
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
                mCanvas.drawPath(path, p);

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
                mCanvas.drawPath(path, p);
            }


        }else if(mTask == 6 && mAngleActual == mAngleTarget)
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
                mCanvas.drawPath(path, p);

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
                mCanvas.drawPath(path, p);
            }
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
            p.setColor(Color.BLUE);
            y = height/2 + 10;
            taskText = task_alphabet.get(mDistanceTargert);
            textWidth = p.measureText(taskText);
            x = width / 2 - textWidth/2;
            mCanvas.drawText(taskText, x, y, p);
        }else if(mTask == 2)
        {
            //draw number
            p.setTextSize(calcFontSize(40));
            p.setColor(Color.BLUE);
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

            if(mDistanceTargert != 1)
            {
                Path path = new Path();
                path.moveTo(task_shape.get(mDistanceTargert)[0].x * scale + xoffSet, task_shape.get(mDistanceTargert)[0].y * scale + yoffSet);
                for(int itrs = 1; itrs < task_shape.get(mDistanceTargert).length; itrs++)
                {
                    path.lineTo(task_shape.get(mDistanceTargert)[itrs].x * scale + xoffSet, task_shape.get(mDistanceTargert)[itrs].y * scale + yoffSet);
                }

                p.setColor(Color.BLUE);
                p.setStyle(Paint.Style.FILL);
                mCanvas.drawPath(path, p);
            }else
            {
                p.setColor(Color.BLUE);
                p.setStyle(Paint.Style.FILL);
                mCanvas.drawCircle(x, y, scale*0.5f, p);
            }

        }else if(mTask == 4)
        {
            //font size
            if(mContinuousTarget != -1 && mContinuousTarget < (mContinuousMax + reservedDistance))
            {
                Path path = new Path();
                float mx = width/2;
                float my = height/2;
                float offx = 320 - mContinuousTarget / 2 - mx;
                float offy = mContinuousTarget / 2 - my;

                path.moveTo(320 - offx, 0 - offy);
                path.lineTo(320 - offx, mContinuousTarget- offy);
                path.lineTo(320 - mContinuousTarget - offx, mContinuousTarget- offy);
                path.lineTo(320 - offx, 0- offy);

                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.BLUE);
                p.setStrokeWidth(2);
                mCanvas.drawPath(path, p);

                path.reset();
                path.moveTo(300 - offx, 0 - offy);
                path.lineTo(300 - offx, mContinuousTarget- offy);
                mCanvas.drawPath(path, p);



            }
        }else if(mTask == 5)
        {
            //color
            Path path = new Path();
            float mx = width/2;
            float my = height/2;
            float offx = 320 - 160 / 2 - mx;
            float offy = 160 / 2 - my;

            path.moveTo(320 - offx, 0 - offy);
            path.lineTo(320 - offx, 160 - offy);
            path.lineTo(320 - 160 - offx, 160- offy);
            path.lineTo(320 - offx, 0- offy);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(255, 0, 0, (int)(255 * (mContinuousTarget / (mContinuousMax + reservedDistance) ))));
            mCanvas.drawPath(path, p);

        }else if(mTask == 6)
        {

            //wegght
            Path path = new Path();
            float mx = width/2;
            float my = height/2;
            float offx = 320 - 160 / 2 - mx;
            float offy = 160 / 2 - my;

            path.moveTo(320 - offx, 0 - offy);
            path.lineTo(320 - offx, 160 - offy);
            path.lineTo(320 - 160 - offx, 160- offy);
            path.lineTo(320 - offx, 0- offy);

            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.BLUE);
            p.setStrokeWidth( 20 * (mContinuousTarget / (mContinuousMax + reservedDistance) ));

            mCanvas.drawPath(path, p);
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
            if( (dis >= reservedDistance && disSegs != mDistanceActual)
                    || angSegs != mAngleActual)
            {


                //check logical error
                //when first start, it should go to 0, 1, ....
                if(mDistanceActual == -1)
                {
                    if(disSegs != 0)
                    {
                        return;
                    }
                }



                mDistanceActual = disSegs;

                if(isOvershot <  (mDistanceActual - mDistanceTargert))
                {
                    isOvershot = (mDistanceActual - mDistanceTargert);
                }

                if(mAngleActual != angSegs && mAngleActual == -1)
                {
                    //a start angle

                    if(isWrongTask == 1)
                    {
                        //start with wrong corner
                        //isWrongTask = 3;
                    }else if(isWrongTask == 0)
                    {
                        //start with right corner
                        if(angSegs == mAngleTarget)
                        {
                            //correct
                        }else
                        {
                            isWrongTask = 2;
                        }

                    }
                }

                mAngleActual = angSegs;

//                //record the change of distance or angle
//
//                {
//                    long currentTimestamp = System.currentTimeMillis();
//                    int mTaskType =  1 ;
//                    float distancevaluetarget = mDistanceTargert;
//
//                    DataStorage.AddSample(1, MainActivity.getSharedInstance().mStudyView.mStudy.currentTask,
//                            MainActivity.getSharedInstance().mStudyView.mStudy.currentAttempt,
//                            3, currentTimestamp, mCorner, MainActivity.getSharedInstance().mStudyView.mPageRender.mTask, mTaskType, mClose,
//                            mAngleTarget, distancevaluetarget, mAngleActual, mDistanceActual);
//
//                }

                ReloadSecondPageTexture();
            }
        }else
        {
            if((2 * dis /3) != mContinuousActual || angSegs != mAngleActual)
            {

                //do we have logical error here?

                mDistanceActual = disSegs;

                mContinuousActual = (2*dis /3);

                if(angSegs != mAngleActual)
                {
                    if(mAngleActual == -1)
                    {
                        if(isWrongTask == 1)
                        {

                        }else if(isWrongTask == 0)
                        {
                            //start with right corner
                            if(angSegs == mAngleTarget)
                            {
                                //correct
                            }else
                            {
                                isWrongTask = 2;
                            }

                        }

                    }

                    mAngleActual = angSegs;

//                    //record the change of angle
//                    {
//                        long currentTimestamp = System.currentTimeMillis();
//                        int mTaskType = 2;
//                        float distancevaluetarget = mContinuousActual;
//
//                        DataStorage.AddSample(1, MainActivity.getSharedInstance().mStudyView.mStudy.currentTask,
//                                MainActivity.getSharedInstance().mStudyView.mStudy.currentAttempt,
//                                3, currentTimestamp, mCorner, MainActivity.getSharedInstance().mStudyView.mPageRender.mTask, mTaskType, mClose,
//                                mAngleTarget, distancevaluetarget, mAngleActual, mContinuousActual);
//                    }

                }

                if(isOvershot == 0 && mContinuousActual > (mContinuousTarget * (1 + accuracyInterval)))
                {
                    isOvershot = 1;
                }

                ReloadSecondPageTexture();
            }



        }


    }
}

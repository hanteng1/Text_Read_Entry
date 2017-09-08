package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
    public ArrayList<Integer> task_shape;




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
        task_alphabet.add("F");

        task_number = new ArrayList<Integer>();
        task_number.add(1);
        task_number.add(2);
        task_number.add(3);
        task_number.add(4);
        task_number.add(5);

        task_shape = new ArrayList<Integer>();
        task_shape.add(R.drawable.geo_1);
        task_shape.add(R.drawable.geo_2);
        task_shape.add(R.drawable.geo_3);
        task_shape.add(R.drawable.geo_4);
        task_shape.add(R.drawable.geo_5);



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

        //target



        //and actual
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
            Bitmap shapebit = BitmapFactory.decodeResource(mContext.getResources(), task_shape.get(mDistanceTargert));
            y = height /2;
            x = width / 2;
            RectF dest = new RectF(x - 30, y - 30, x + 30, y + 30);
            mCanvas.drawBitmap(shapebit, null, dest, p);
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


            //update the recrod
            MainActivity.getSharedInstance().mStudyView.mStudy.numVistedCells++;
            int overshot = mDistanceActual - mDistanceTargert;
            if(overshot > MainActivity.getSharedInstance().mStudyView.mStudy.numOvershoot)
            {
                MainActivity.getSharedInstance().mStudyView.mStudy.numOvershoot = overshot;
            }

            //record the data
            long timestamp = System.currentTimeMillis();
            MainActivity.getSharedInstance().mStudyView.mStudy.trialState = 2;
//            DataStorage.AddSample(MainActivity.getSharedInstance().mStudyView.mStudy.currentCondition,
//                    MainActivity.getSharedInstance().mStudyView.mStudy.currentAttempt,
//                    mCorner,
//                    mAngleNum,
//                    mDistanceNum,
//                    mClose,
//                    mAngleTarget,
//                    mDistanceTargert,
//                    mAngleActual,
//                    mDistanceActual,
//                    MainActivity.getSharedInstance().mStudyView.mStudy.trialState,
//                    timestamp);
        }
    }



}

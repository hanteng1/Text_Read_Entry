package com.tenghan.swipeflip;

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

    public int mCorner;
    public int mAngleTarget = -1;
    public int mDistanceTargert = -1;
    public int mAngleNum = 3;
    public int mDistanceNum = 5;
    public int mClose;
    public int mAngleActual = -1;
    public int mDistanceActual = -1;

    public boolean obtainNext = true;

    private Random rand;

    public StudyTwoRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                          Handler handler, int pageNo)
    {
        super(context, pageFlipAbstract, handler, pageNo);
        paths = new ArrayList<Path>();
        sin45 = (float)Math.sin(Math.PI / 4);
        rand = new Random();
    }

    //initial load
    public void LoadTextures()
    {
        int curTask = MainActivity.getSharedInstance().mStudyView.mStudy.obtainNextTask();
        MainActivity.getSharedInstance().mStudyView.mStudy.currentTask--;

        if(curTask < 4)
        {
            mCorner = 0;  //left-top
        }else
        {
            mCorner = 1;  //right-top
        }

        if(curTask == 1)
        {
            //task 1
        }else if(curTask == 2)
        {
            //task 2
        }else if(curTask == 3)
        {
            //task 3
        }else if(curTask == 4)
        {
            //task 4
        }else if(curTask == 5)
        {
            //task 5
        }else if(curTask == 6)
        {
            //task 6
        }

        mPageFlipAbstract.deleteUnusedTextures();
        PageModify[] pages = mPageFlipAbstract.getPages();

        //set the first page
        if(!pages[FIRST_PAGE].isFrontTextureSet())
        {
            loadPageWithTrialInfo(curTask);
            pages[FIRST_PAGE].setFrontTexture(mBitmap);
        }

        //set the second page
        if(!pages[SECOND_PAGE].isFrontTextureSet())
        {
            loadPageWithTask(curTask);
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
    public void ReloadTrial()
    {

    }

    //set up the second page
    public void loadPageWithCondition()
    {

    }

    //show the task on first page
    public void loadPageWithTrialInfo()
    {

    }

    public void loadPageWithTask(int taskIndex)
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
    public void loadPageWithTrialInfo(int taskIndex)
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
        float y = height / 2;
        float x = width / 2 - textWidth / 2;
        mCanvas.drawText(taskText, x, y, p);

        //draw target


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

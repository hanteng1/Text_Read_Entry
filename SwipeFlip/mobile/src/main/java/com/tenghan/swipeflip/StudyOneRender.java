package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.health.PackageHealthStats;
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
    private float maxDistance = 160.0f;
    private float maxAngle = (float)Math.PI / 2;
    private float maxAngleDegree = 90.0f;
    private int FIRST_PAGE = 0;
    private int SECOND_PAGE = 1;

    private int mCorner;
    private int mAngleTarget;
    private int mDistanceTargert;
    private int mAngleNum;
    private int mDistanceNum;

    private Random rand;


    public StudyOneRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                         Handler handler, int pageNo)
    {
        super(context, pageFlipAbstract, handler, pageNo);


        paths = new ArrayList<Path>();
        sin45 = (float)Math.sin(Math.PI / 4);

        rand = new Random();


    }

    public void LoadTextures(){

        //initial condition
        int[] curCondition = MainActivity.getSharedInstance().mStudyView.mStudy.obtainNextCondition();
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
            loadPageWithTrialInfo(mCorner, mAngleTarget, mDistanceTargert, mAngleNum, mDistanceNum);
            pages[FIRST_PAGE].setFrontTexture(mBitmap);
        }

        //set the second page
        if(!pages[SECOND_PAGE].isFrontTextureSet())
        {
            loadPageWithCondition(mCorner, mAngleTarget, mDistanceTargert, mAngleNum, mDistanceNum);
            pages[SECOND_PAGE].setFrontTexture(mBitmap);
        }
    }

    //this reload is corresponding to page flip gesture
    public void ReloadTexture()
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

        rand = new Random();
        mAngleTarget = rand.nextInt(mAngleNum);
        mDistanceTargert = rand.nextInt(mDistanceNum);

        PageModify page = mPageFlipAbstract.getPages()[FIRST_PAGE];
        page.waiting4TextureUpdate = true;

        page = mPageFlipAbstract.getPages()[SECOND_PAGE];
        page.waiting4TextureUpdate = true;
    }

    public void loadPageWithTrialInfo(int corner, int angleTarget, int distanceTargert, int angleNum, int distanceNum)
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

        String conditionText = "AngleNum " + angleNum + " , DistanceNum " + distanceNum;

        float textWidth = p.measureText(conditionText);
        float y = height / 2;
        float x = width / 2 - textWidth / 2;

        mCanvas.drawText(conditionText, x, y, p);
    }

    public void loadPageWithCondition(int corner, int angleTarget, int distanceTargert, int angleNum, int distanceNum)
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

        PointF origin = new PointF();
        if(corner == 0)
        {
            origin.set(0, 0);
        }else if(corner == 1)
        {
            origin.set(width, 0);
        }else if(corner == 2)
        {
            origin.set(width, height);
        }else if(corner == 3)
        {
            origin.set(0, height);
        }

        paths.clear();
        //angle paths
        for(int itra = 1; itra < angleNum; itra++)
        {
            float segAngle = maxAngle / angleNum;

            mCanvas.drawLine(origin.x, origin.y , origin.x + maxDistance * (float)Math.cos(segAngle * itra + (Math.PI/2) * corner),
                    origin.y + maxDistance * (float)Math.sin(segAngle * itra + (Math.PI/2) * corner), p);

        }

        //distance paths
        for(int itrd = 1; itrd < distanceNum; itrd++)
        {
            float segDis = maxDistance / distanceNum;

            RectF rectF = new RectF(origin.x - segDis * itrd, origin.y - segDis * itrd,
                    origin.x + segDis * itrd, origin.y + segDis * itrd);

            mCanvas.drawArc(rectF, corner * maxAngleDegree, maxAngleDegree, false, p);
        }



        //real time update of selected zone


    }

}

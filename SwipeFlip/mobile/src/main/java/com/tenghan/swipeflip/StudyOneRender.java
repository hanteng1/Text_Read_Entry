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
    private float crossLength = 20;

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

        rand = new Random();
        mAngleTarget = rand.nextInt(mAngleNum);
        mDistanceTargert = rand.nextInt(mDistanceNum);

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

        String conditionText = "trial " + (MainActivity.getSharedInstance().mStudyView.mStudy.currentCondition + 1)
                + " / " + MainActivity.getSharedInstance().mStudyView.mStudy.conditions.size();

        float textWidth = p.measureText(conditionText);
        float y = height / 2 - 30;
        float x = width / 2 - textWidth / 2;
        mCanvas.drawText(conditionText, x, y, p);

        conditionText = "AngleNum " + mAngleNum;
        textWidth = p.measureText(conditionText);
        y += (10 + fontSize);
        x = width / 2 - textWidth / 2;
        mCanvas.drawText(conditionText, x, y, p);

        conditionText =  "DistanceNum " + mDistanceNum;
        textWidth = p.measureText(conditionText);
        y += (10 + fontSize);
        x = width / 2 - textWidth / 2;
        mCanvas.drawText(conditionText, x, y, p);


        //draw target indicators
        p.setStyle(Paint.Style.STROKE);

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
            mCanvas.drawLine(origin.x, origin.y , origin.x + maxDistance * (float)Math.cos(segAngle * itra + (Math.PI/2) * mCorner),
                    origin.y + maxDistance * (float)Math.sin(segAngle * itra + (Math.PI/2) * mCorner), p);

        }

        //distance paths
        float segDis = maxDistance / mDistanceNum;
        for(int itrd = 1; itrd < mDistanceNum; itrd++)
        {
            RectF rectF = new RectF(origin.x - segDis * itrd, origin.y - segDis * itrd,
                    origin.x + segDis * itrd, origin.y + segDis * itrd);

            mCanvas.drawArc(rectF, mCorner * maxAngleDegree, maxAngleDegree, false, p);
        }

        //target
        float targetLength = segDis * (mDistanceTargert + 0.5f);
        float targetAngle = segAngle * ( mAngleTarget + 0.5f) + maxAngle * mCorner;
        float targetX = origin.x + targetLength * (float)Math.cos(targetAngle);
        float targetY = origin.y + targetLength * (float)Math.sin(targetAngle);

        drawTargetCross(mCanvas, p, targetX, targetY);

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
        for(int itra = 1; itra < mAngleNum; itra++)
        {
            float segAngle = maxAngle / mAngleNum;

            mCanvas.drawLine(origin.x, origin.y , origin.x + maxDistance * (float)Math.cos(segAngle * itra + (Math.PI/2) * mCorner),
                    origin.y + maxDistance * (float)Math.sin(segAngle * itra + (Math.PI/2) * mCorner), p);

        }

        //distance paths
        for(int itrd = 1; itrd < mDistanceNum; itrd++)
        {
            float segDis = maxDistance / mDistanceNum;

            RectF rectF = new RectF(origin.x - segDis * itrd, origin.y - segDis * itrd,
                    origin.x + segDis * itrd, origin.y + segDis * itrd);

            mCanvas.drawArc(rectF, mCorner * maxAngleDegree, maxAngleDegree, false, p);
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
        canvas.drawLine(x - crossLength /2 , y - crossLength /2 ,
                x + crossLength /2 , y + crossLength /2 , paint);
        canvas.drawLine(x + crossLength /2 , y - crossLength /2 ,
                x - crossLength /2 , y + crossLength /2 , paint);
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

}

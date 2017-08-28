package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.health.PackageHealthStats;

import com.eschao.android.widget.pageflip.modify.PageModify;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hanteng on 2017-08-28.
 */

public class StudyOneRender extends StudyRender{

    private final static String TAG = "StudyOneRender";

    private ArrayList<Integer> testingCorner;
    private ArrayList<Integer> testingAngleSeperation;
    private ArrayList<Integer> testingDistanceSerperation;

    private ArrayList<Path> paths;

    private float sin45;

    //set the base
    private float maxDistance = 160.0f;
    private float maxAngle = 90.0f;
    private int FIRST_PAGE = 0;
    private int SECOND_PAGE = 1;

    public StudyOneRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                         Handler handler, int pageNo)
    {
        super(context, pageFlipAbstract, handler, pageNo);

        testingAngleSeperation = new ArrayList<Integer>();
        testingCorner = new ArrayList<Integer>();
        testingDistanceSerperation = new ArrayList<Integer>();

        //set study conditions
        testingAngleSeperation.add(2);
        testingAngleSeperation.add(3);
        testingAngleSeperation.add(4);
        testingAngleSeperation.add(5);

        testingDistanceSerperation.add(3);
        testingDistanceSerperation.add(4);
        testingDistanceSerperation.add(5);
        testingDistanceSerperation.add(6);

        /**
         *    ---------
         *   |0       1|
         *   |         |
         *   |         |
         *   |3       2|
         *    ---------
         */
        testingCorner.add(0);
        testingCorner.add(1);
        testingCorner.add(2);
        testingCorner.add(3);

        paths = new ArrayList<Path>();

        sin45 = (float)Math.sin(Math.PI / 4);

    }

    public void LoadTextures(){
        mPageFlipAbstract.deleteUnusedTextures();
        PageModify[] pages = mPageFlipAbstract.getPages();

        //set the first page
        if(!pages[FIRST_PAGE].isFrontTextureSet())
        {
            loadPageWithTrialInfo(0, 2, 3, 3, 3);
            pages[FIRST_PAGE].setFrontTexture(mBitmap);
        }

        //set the second page
        if(!pages[SECOND_PAGE].isFrontTextureSet())
        {
            loadPageWithCondition(0, 2,3, 3,3);
            pages[SECOND_PAGE].setFrontTexture(mBitmap);
        }
    }

    public void ReloadTexture(int itrp)
    {
        PageModify page = mPageFlipAbstract.getPages()[itrp];
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
        p.setStrokeWidth(1);
        p.setAntiAlias(true);

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
            Path path = new Path();
            path.moveTo(origin.x, origin.y);

            /*
            switch (corner)
            {
                case 0:

                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }*/

            path.lineTo(origin.x + (float)Math.cos(segAngle * itra + (Math.PI/2) * corner),
                    origin.y + (float)Math.sin(segAngle * itra + (Math.PI/2) * corner));

            paths.add(path);
        }

        //distance paths



        //draw
        for(int itrp = 0; itrp < paths.size(); itrp++)
        {
            mCanvas.drawPath(paths.get(itrp), p);
        }

    }

}

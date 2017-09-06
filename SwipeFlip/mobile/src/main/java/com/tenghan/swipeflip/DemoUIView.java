package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hanteng on 2017-08-21.
 */

public class DemoUIView extends View {

    private final static String TAG = "DemoUIView";


    private Paint inputPaint = new Paint();
    private int screenWidth, screenHeight;


    private Path touchPath = new Path();
    private Paint touchPaint = new Paint();
    public PointF boundingLeftTop = new PointF();
    private PointF boundingRightBottom = new PointF();


    private Context mContext;

    private boolean isActive;

    /**
     * 1 - peel2command
     * 2 - notification
     * 3 - copy and paste
     */
    public int demoIndex = 0;


    //used to test
    public PointF origin = new PointF();
    public PointF corner = new PointF();
    public PointF peelOne = new PointF();
    public PointF peelTwo = new PointF();

    public boolean isdrawing = true;

    public Bitmap resultBitmap;
    public Bitmap cropOriginBitmap;


    public DemoUIView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        inputPaint.setAntiAlias(true);
        inputPaint.setFilterBitmap(true);
        inputPaint.setStrokeWidth(1);
        inputPaint.setColor(Color.RED);
        //inputPaint.setStyle(Paint.Style.STROKE);
        inputPaint.setStrokeJoin(Paint.Join.ROUND);

        //set touch paint, transparent
        touchPaint.setAntiAlias(true);
        touchPaint.setStrokeWidth(20);
        touchPaint.setColor(Color.GREEN);
        touchPaint.setStyle(Paint.Style.STROKE);
        touchPaint.setStrokeJoin(Paint.Join.ROUND);

    }

    protected int calcFontSize(int size)
    {
        return (int)(size * mContext.getResources().getDisplayMetrics().scaledDensity);
    }

    public void setDimension(int x, int y)
    {
        screenWidth = x;
        screenHeight = y;

        resultBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
                Bitmap.Config.ARGB_8888);

        cropOriginBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
                Bitmap.Config.ARGB_8888);


    }

    private void clear()
    {
        touchPath.reset();
        boundingLeftTop.set(screenWidth, screenHeight);
        boundingRightBottom.set(0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {

        if(demoIndex == 0)
        {
            //origin to corner
            canvas.drawLine(origin.x, origin.y, corner.x, corner.y, inputPaint);
            //flipped
            canvas.drawLine(peelOne.x, peelOne.y, peelTwo.x, peelTwo.y, inputPaint);
        }else if(demoIndex == 3)
        {
            //copy and paste demo

            //if still drawing
            if(isdrawing)
            {
                canvas.drawPath(touchPath, touchPaint);

            }else
            {

//                Log.d(TAG, "draw bitmap");

//                canvas.drawBitmap(resultBitmap, boundingLeftTop.x, boundingLeftTop.y, touchPaint);
            }


            //if finger up


        }

    }


    public void onDoubleTap(float x, float y)
    {
        clear();
        touchPath.moveTo(x, y);
    }

    public void onTapMove(float x, float y)
    {
        touchPath.lineTo(x, y);

        if(x < boundingLeftTop.x)
        {
            boundingLeftTop.x = x;
        }

        if(x > boundingRightBottom.x)
        {
            boundingRightBottom.x = x;
        }

        if(y < boundingLeftTop.y)
        {
            boundingLeftTop.y = y;
        }

        if(y > boundingRightBottom.y)
        {
            boundingRightBottom.y = y;
        }

        invalidate();
    }

    public void onTapUp(float x, float y)
    {
        //get the bitmap from canavas
        //draw the same
        Canvas mCanvas = new Canvas();
        mCanvas.setBitmap(resultBitmap);

        //copy from the loadtexture

        //draw thee background
        //Bitmap background = LoadBitmapTask.get(mContext).getBitmap();
        Bitmap background = LoadBitmapTask.get(mContext).getPhoto();
        Rect rect = new Rect(0, 0, screenWidth, screenHeight);
        mCanvas.drawBitmap(background, null, rect, inputPaint);
        background.recycle();
        background = null;


//
//        //draw the text
//        int fontSize = calcFontSize(10);
//        inputPaint.setColor(Color.GRAY);
//        inputPaint.setStrokeWidth(1);
//        inputPaint.setAntiAlias(true);
//        //p.setShadowLayer(5.0f, 8.0f, 8.0f, Color.BLACK);
//        inputPaint.setTextSize(fontSize);
//        //String text = Alphabet[number];
//
//        String text = "Mr Trump also warned Pakistan that the US would no longer tolerate the country offering \"safe havens\" to extremists, saying the country had \"much to lose\" if it did not side with the Americans.\n" +
//                "\"We have been paying Pakistan billions and billions of dollars - at the same time they are housing the very terrorists that we are fighting,\" he said.\n" +
//                "He also said the US would seek a stronger partnership with India.\n" +
//                "Meanwhile, Mr Trump made it clear he expects his existing allies to support him in his new strategy, telling them he wanted them to raise their countries' contributions \"in line with our own\".";
//
//        ArrayList<String> textList = new ArrayList<String>(Arrays.asList(text.split("\\s+")));
//
//        PointF textCursor = new PointF();
//        textCursor.set(0, 0);
//
//        for(int itrt = 0; itrt < textList.size(); itrt++)
//        {
//            if(textCursor.x + inputPaint.measureText(textList.get(itrt)) >  320)
//            {
//                //change to the next line
//                textCursor.x = 0;
//                textCursor.y += 20;
//            }
//
//            mCanvas.drawText(textList.get(itrt), textCursor.x, textCursor.y, inputPaint);
//
//            textCursor.x += (inputPaint.measureText(textList.get(itrt)) + 10);
//        }
//
//        mCanvas.drawPath(touchPath, touchPaint);
//
//
//        //bounding box
//        touchPaint.setStrokeWidth(1);
//        touchPaint.setColor(Color.RED);
//
//        mCanvas.drawRect(boundingLeftTop.x, boundingLeftTop.y, boundingRightBottom.x, boundingRightBottom.y, touchPaint);



        //find the countour, use bounding box for now
        Bitmap croppoedBitmap = Bitmap.createBitmap(resultBitmap, (int)boundingLeftTop.x, (int)boundingLeftTop.y,
                (int)(boundingRightBottom.x - boundingLeftTop.x), (int)(boundingRightBottom.y - boundingLeftTop.y));

        resultBitmap = croppoedBitmap;

        //crop the bitmap, using the drawing path if possible
        //save it for later work
        //https://stackoverflow.com/questions/8993292/cutting-a-multipoint-ploygon-out-of-bitmap-and-placing-it-on-transparency






        //get the cropped first page
        mCanvas = new Canvas();
        mCanvas.setBitmap(cropOriginBitmap);

        //draw thee background
        background = LoadBitmapTask.get(mContext).getPhoto();
        rect = new Rect(0, 0, screenWidth, screenHeight);
        mCanvas.drawBitmap(background, null, rect, inputPaint);
        background.recycle();
        background = null;

        inputPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        inputPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawRect(boundingLeftTop.x, boundingLeftTop.y, boundingRightBottom.x, boundingRightBottom.y, inputPaint);





        //push it to the page render and reload the texture
        MainActivity.getSharedInstance().mDemoView.mPageRender.setCropImage(cropOriginBitmap, resultBitmap, boundingLeftTop);

        MainActivity.getSharedInstance().mDemoView.mDemo.getPages()[1].waiting4TextureUpdate = true;

        invalidate();

    }



    public void activate()
    {
        isActive = true;
        invalidate();
    }

    public void deActivate()
    {
        isActive = false;
        invalidate();
    }


}

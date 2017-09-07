package com.tenghan.markingmenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by hanteng on 2017-08-22.
 */

public class DemoUIView extends View {

    private final static String TAG = "DemoUIView";
    private Paint inputPaint = new Paint();
    private Paint bgPaint = new Paint();
    private Paint subMenuPaint = new Paint();
    private Paint subMenuSliderPaint = new Paint();
    private Paint strokePaint = new Paint();
    private int screenWidth, screenHeight;
    private Path touchPath = new Path();
    private int touchLength = 10;
    private ArrayList<PointF> touchPoints;

    private String[][] menuItems = {{"copy", "paste", "color"},
            {"font", "size", "bright"},
            {"contrast", "volume", "darkness"},
            {"visual", "point", "correct"}};
    private float menuDistance = 80.0f;
    private PointF menuCenter = new PointF();

    private Runnable strokeDeleting;
    private Handler mHandler;

    //0 - first layer, 1- second layer, 2 - third layer (value selection)
    public int totalMenuLayers = 3;
    public int currentMenuLayer = -1;
    public int currentSecondMenu = -1;
    public boolean waitingforsecondlayer = false;


    //for the first layer - second layer


    //for the second layer - third layer
    public boolean isTriggered = false;
    public boolean isSubMenuing = false;

    private boolean isSubMenuVertical = false;
    private boolean isSubMenuLeftTop = false;
    private float subMenuWidth = 60;
    private int currentSubMenu = -1;
    private PointF subMenuTouchAchor = new PointF();


    //color submenu
    private ArrayList<Integer> colorCode;
    private Random rand;
    private int totalColor = 100;
    private int presentedColor = 10;
    private int colorAnchor = 45;


    //letter submenu, scroll list
    private ArrayList<String> scrollList;
    private int totallists = 7;

    private String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};


    //continuous bar





    public DemoUIView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inputPaint.setAntiAlias(true);
        inputPaint.setStrokeWidth(0);
        inputPaint.setColor(Color.RED);
        inputPaint.setTextSize(28);
        //inputPaint.setStyle(Paint.Style.STROKE);
        inputPaint.setStrokeJoin(Paint.Join.ROUND);

        subMenuSliderPaint.setAntiAlias(true);
        subMenuSliderPaint.setStrokeWidth(2);
        subMenuSliderPaint.setColor(Color.BLACK);
        subMenuSliderPaint.setStyle(Paint.Style.STROKE);
        subMenuSliderPaint.setStrokeJoin(Paint.Join.ROUND);

        subMenuPaint.setAntiAlias(true);
        subMenuPaint.setStrokeWidth(0);
        subMenuPaint.setColor(Color.RED);
        subMenuPaint.setStyle(Paint.Style.FILL);

        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(1);
        strokePaint.setColor(Color.BLUE);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);

        menuCenter.x = 160.0f;
        menuCenter.y = 160.0f;

        this.setBackgroundColor(Color.parseColor("#559B9B9B"));

        //generate color code
        colorCode = new ArrayList<Integer>();
        rand = new Random();
        generateColor(totalColor);


        //scroll list
        scrollList = new ArrayList<String>();
        generateScrollList(totallists);

        touchPoints = new ArrayList<PointF>();

        mHandler = new Handler();
        strokeDeleting = new Runnable() {
            @Override
            public void run() {

                if (touchPoints.size() > 1)
                {
                    touchPoints.remove(0);
                    invalidate();
                }

                mHandler.postDelayed(this, 50);
            }
        };

        mHandler.post(strokeDeleting);
    }


    private void generateColor(int num)
    {
        int maxDistance = 30;

        int a = 255;
        //set first to white
        int r = 255;
        int g = 255;
        int b = 255;

        int color = Color.argb(a, r, g, b);
        colorCode.add(color);

        for(int i = 1; i < num; i++)
        {
            r = randomValue(r, maxDistance);
            g = randomValue(g, maxDistance);
            b = randomValue(b, maxDistance);

            color = Color.argb(a, r, g, b);
            colorCode.add(color);
        }
    }


    private void generateScrollList(int num)
    {
        for(int itrl =0; itrl < num; itrl++)
        {
            scrollList.add(alphabet[itrl]);
        }
    }

    private int randomValue(int value, int distance)
    {
        //randomly decide pos or negative
        int pos = rand.nextInt(2) > 0 ? 1 : -1;
        int newValue = value + pos * rand.nextInt(distance);

        if(newValue > 255 || newValue < 0)
        {
            return randomValue(value, distance);
        }

        return newValue;

    }

    public void setDimension(int x, int y)
    {
        screenWidth = x;
        screenHeight = y;
    }

    private void clear()
    {
        touchPath.reset();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        if(!isTriggered)
        {
            return;
        }

        //set background color

        if(!isSubMenuing)
        {
            //first layer
            if(currentMenuLayer == 0)
            {
                /**
                 *          1
                 *          |
                 *          |
                 *    0 --------- 2
                 */
                //for the commands tags
                for(int itrc = 0; itrc < menuItems[currentMenuLayer].length; itrc++)
                {
                    int posX = 1;
                    int posY = 1;
                    if(itrc == 0 || itrc == 2)
                    {
                        posY = 0;
                        posX = 1;
                        if(itrc == 0)
                        {
                            posX = -1;
                        }
                    }else if(itrc == 1 || itrc == 3)
                    {
                        posX = 0;
                        posY = 1;
                        if(itrc == 1)
                        {
                            posY = -1;
                        }
                    }
                    String text = menuItems[currentMenuLayer][itrc];
                    float textLength = 0;
                    if(itrc == 0)
                    {
                        textLength = inputPaint.measureText(text);
                    }else if(itrc == 1 || itrc == 3)
                    {
                        textLength = inputPaint.measureText(text) / 2;
                    }

                    canvas.drawText(text, menuCenter.x + posX * menuDistance - textLength, menuCenter.y + posY * menuDistance, inputPaint);
                }
            }else if(currentMenuLayer == 1)
            {
                /**
                 *
                 *    0 --------- 1
                 *          |
                 *          |
                 *          2
                 */
                for(int itrc = 0; itrc < menuItems[currentMenuLayer + currentSecondMenu].length; itrc++)
                {
                    int posX = 1;
                    int posY = 1;
                    if(itrc == 0 || itrc == 1)
                    {
                        posY = 0;
                        posX = 1;
                        if(itrc == 0)
                        {
                            posX = -1;
                        }
                    }else if(itrc == 2 || itrc == 3)
                    {
                        posX = 0;
                        posY = 1;
                        if(itrc == 3)
                        {
                            posY = -1;
                        }
                    }
                    String text = menuItems[currentMenuLayer + currentSecondMenu][itrc];
                    float textLength = 0;
                    if(itrc == 0)
                    {
                        textLength = inputPaint.measureText(text);
                    }else if(itrc == 1 || itrc == 3)
                    {
                        textLength = inputPaint.measureText(text) / 2;
                    }

                    canvas.drawText(text, menuCenter.x + posX * menuDistance - textLength, menuCenter.y + posY * menuDistance, inputPaint);
                }

            }



            //draw touch path
            if (touchPoints.size() > 0)
            {
                touchPath.reset();
                for (int itrt = 0; itrt < touchPoints.size(); itrt++)
                {
                    if(itrt == 0)
                    {
                        touchPath.moveTo(touchPoints.get(itrt).x, touchPoints.get(itrt).y);
                    }else
                    {
                        touchPath.lineTo(touchPoints.get(itrt).x, touchPoints.get(itrt).y);
                    }
                }

                canvas.drawPath(touchPath, strokePaint);
            }
        }else
        {
            //based on which command is triggered
            if(currentSubMenu == 0)
            {
                drawColorPanel(canvas, isSubMenuVertical, isSubMenuLeftTop);
            }else if(currentSubMenu == 1)
            {
                //scroll bar

                drawListPanel(canvas, isSubMenuVertical, isSubMenuLeftTop);

            }else if(currentSubMenu == 2)
            {
                //scroll list

                drawListPanel(canvas, isSubMenuVertical, isSubMenuLeftTop);

            }


        }

    }

    public void onFingerDown(float x, float y)
    {
        //touchPath.reset();
        //touchPath.moveTo(x, y);
        touchPoints.clear();
        touchPoints.add(new PointF(x, y));

        //check the menu status
        //if()


    }

    public void onFingerMove(float x, float y)
    {
        //touchPath.lineTo(x, y);
        if(isTriggered)
        {
            touchPoints.add(new PointF(x, y));

            //here submenu is for value selection
            if(!isSubMenuing)
            {

                if(currentMenuLayer == 0)
                {

                    /**
                     *          1
                     *          |
                     *          |
                     *    0 --------- 2
                     */

                    //gesture detection
                    int gestureResult = detectGesture(touchPoints);
                    switch (gestureResult)
                    {
                        case -1:
                            break;
                        case 0:
                            //currentMenuLayer = 1;
                            currentSecondMenu = 0;
                            waitingforsecondlayer = true;
                            break;
                        case 1:
                            currentSecondMenu = 1;
                            //currentMenuLayer = 1;
                            waitingforsecondlayer = true;
                            break;
                        case 2:
                            currentSecondMenu = 2;
                            //currentMenuLayer = 1;
                            waitingforsecondlayer = true;
                            break;
                        case 3:
                            break;
                        default:
                            break;
                    }

                }else if(currentMenuLayer == 1 && waitingforsecondlayer == false)
                {
                    //has to take effects on the second selection

                    /**
                     *
                     *    0 --------- 1
                     *          |
                     *          |
                     *          2
                     */
                    //gesture detection
                    int gestureResult = detectGesture(touchPoints);
                    switch (gestureResult)
                    {
                        case -1:
                            break;
                        case 0:
                            isSubMenuVertical = true;
                            isSubMenuLeftTop = false;
                            isSubMenuing = true;
                            currentSubMenu = 0;
                            subMenuTouchAchor.set(x, y);
                            break;
                        case 1:
//                            isSubMenuVertical = false;
//                            isSubMenuLeftTop = false;
//                            isSubMenuing = true;
//                            subMenuTouchAchor.set(x, y);
                            break;
                        case 2:
                            isSubMenuVertical = true;
                            isSubMenuLeftTop = true;
                            isSubMenuing = true;
                            currentSubMenu = 1;
                            subMenuTouchAchor.set(x, y);
                            break;
                        case 3:
                            isSubMenuVertical = false;
                            isSubMenuLeftTop = true;
                            isSubMenuing = true;
                            currentSubMenu = 2;
                            subMenuTouchAchor.set(x, y);
                            break;
                        default:
                            break;
                    }
                }

            }else
            {
                //submenu is activated
                //operate on the submenu

                if(currentSubMenu == 0)
                {
                    //for color
                    if(isSubMenuVertical == true)
                    {
                        //vertical, use y
                        float diffY = y - subMenuTouchAchor.y;
                        int diffColorAnchor = (int) (diffY * (totalColor - presentedColor)) / screenHeight;
                        colorAnchor = totalColor / 2 + diffColorAnchor;

                        Log.d(TAG, "diffY " + diffY);
                    }else
                    {
                        //horizontal, use x
                        float diffX = x - subMenuTouchAchor.x;
                        int diffColorAnchor = (int) (diffX * (totalColor - presentedColor)) / screenWidth;
                        colorAnchor = totalColor / 2 + diffColorAnchor;

                        Log.d(TAG, "diffX " + diffX);
                    }

                    if( (colorAnchor + presentedColor)  > totalColor)
                    {
                        colorAnchor = totalColor - presentedColor;
                    }else if(colorAnchor < 0)
                    {
                        colorAnchor = 0;
                    }
                }else if(currentSubMenu == 1)
                {

                    //control a scroll bar



                }else if(currentSubMenu == 2)
                {
                    //control scroll list

                }


            }

            invalidate();
        }

    }

    public void onFingerUp(float x, float y)
    {
        if(waitingforsecondlayer)
        {
            //after the first layer

            currentMenuLayer = 1;
            waitingforsecondlayer = false;

            touchPoints.clear();
        }else
        {
            //after the sub menuing
            //reset
            isTriggered = false;
            isSubMenuing = false;

            currentMenuLayer = -1;
            currentSecondMenu = -1;
            currentSubMenu = -1;

        }


        invalidate();
    }

    public void onLongPressed(float x, float y)
    {
        if(currentMenuLayer == -1)
        {
            //menuCenter.set(x, y);
            isTriggered = true;
            currentMenuLayer = 0;
            invalidate();
        }

    }

    //gesture detection
    private int detectGesture(ArrayList<PointF> points)
    {

        /**
         *          1
         *          |
         *          |
         *    0 --------- 2
         *          |
         *          |
         *          3
         */

        int gestureResult = -1;

        if(points.size() > 2)
        {
            float origX = points.get(0).x;
            float origY = points.get(0).y;
            float destX = points.get(points.size() - 1).x;
            float destY = points.get(points.size() - 1).y;

            if(calDistance(origX, origY, destX, destY) > menuDistance)
            {
                //a command is activated
                float disX = destX - origX;
                float disY = destY - origY;
                if(Math.abs(disX) > Math.abs(disY))
                {
                    if(disX > 0)
                    {
                        //stroke to right
                        gestureResult = 2;
                    }else
                    {
                        //stroke to left
                        gestureResult = 0;
                    }
                }else if(Math.abs(disX) < Math.abs(disY))
                {
                    if(disY < 0)
                    {
                        //stroke to top
                        gestureResult = 1;
                    }else
                    {
                        //stroke to bottom
                        gestureResult = 3;
                    }

                }
            }
        }

        return gestureResult;

    }

    private float calDistance(float x1, float y1, float x2, float y2)
    {
        return (float)Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    //dont use this for now
    private float calAngle(float x1, float y1, float x2, float y2)
    {
        return 0;
    }

    private void drawColorPanel(Canvas canvas, boolean isVertical, boolean isleftTop)
    {
        float mWidth;
        float mHeight;
        float anchorX;
        float anchorY;

        if(isVertical)
        {
            mWidth = subMenuWidth;
            mHeight = screenHeight;
            anchorY = 0;
            //on left or right
            if(isleftTop)
            {
                //on left
                anchorX = 0;

                //draw a slider
                canvas.drawLine(anchorX, anchorY + mHeight / 2, anchorX + mWidth * 2, anchorY + mHeight / 2, subMenuSliderPaint);
            }else
            {
                //on right
                anchorX = screenWidth - subMenuWidth;

                //draw a slider
                canvas.drawLine(anchorX - mWidth, anchorY + mHeight / 2, anchorX + mWidth, anchorY + mHeight / 2, subMenuSliderPaint);
            }

            //draw color bars
            //divide the bars in vertical
            //0 - 1000, 10 * 10 * 10

            float segment = mHeight / presentedColor;
            for(int itrc = 0; itrc < presentedColor; itrc++)
            {
                subMenuPaint.setColor(colorCode.get(colorAnchor + itrc));
                canvas.drawRect(anchorX , anchorY + segment * itrc, anchorX + mWidth, anchorY + segment * itrc + segment, subMenuPaint);
            }

        }else
        {
            //on top or bottom
            mWidth = screenWidth;
            mHeight = subMenuWidth;
            anchorX = 0;

            if(isleftTop)
            {
                //on top
                anchorY = 0;
                //draw a slider
                canvas.drawLine(anchorX + mWidth / 2, anchorY, anchorX + mWidth / 2, anchorY + mHeight * 2, subMenuSliderPaint);
            }else
            {
                //on bottom
                anchorY = screenHeight - subMenuWidth;

                //draw a slider
                canvas.drawLine(anchorX + mWidth / 2, anchorY - mHeight, anchorX + mWidth / 2, anchorY + mHeight, subMenuSliderPaint);
            }


            //draw color bars
            //divide the bars in horizontal
            //0 - 1000, 10 * 10 * 10

            float segment = mWidth / presentedColor;
            for(int itrc = 0; itrc < presentedColor; itrc++)
            {
                subMenuPaint.setColor(colorCode.get(colorAnchor + itrc));
                canvas.drawRect(anchorX + segment * itrc , anchorY, anchorX + segment * itrc + segment, anchorY + mHeight, subMenuPaint);
            }

        }
    }

    private void drawListPanel(Canvas canvas, boolean isVertical, boolean isLeftTop)
    {
        float mWidth;
        float mHeight;
        float anchorX;
        float anchorY;

        subMenuPaint.setStyle(Paint.Style.STROKE);
        subMenuPaint.setTextSize(60);
        subMenuPaint.setColor(Color.GRAY);

        if(isVertical)
        {
            mWidth = subMenuWidth;
            mHeight = screenHeight;
            anchorY = 0;

            if(isLeftTop)
            {
                //on left
                anchorX = 0;
            }else
            {
                //on right
                anchorX = screenWidth - subMenuWidth;
            }

            //draw the list segments
            float segment = mHeight / scrollList.size();
            for(int itrc = 0; itrc < scrollList.size(); itrc++)
            {
                canvas.drawText(scrollList.get(itrc), anchorX, anchorY + segment * (itrc + 1), subMenuPaint);
            }

        }else
        {
            //on top or bottom
            mWidth = screenWidth;
            mHeight = subMenuWidth;
            anchorX = 0;

            if(isLeftTop)
            {
                //on top
                anchorY = subMenuWidth;
            }else
            {
                //on bottom
                anchorY = screenHeight;
            }

            float segment = mWidth / scrollList.size();
            for(int itrc = 0; itrc < scrollList.size(); itrc++)
            {
                canvas.drawText(scrollList.get(itrc), anchorX + segment * itrc, anchorY, subMenuPaint);
            }

        }

    }


}

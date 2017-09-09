package com.tenghan.markingmenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by hanteng on 2017-09-09.
 */

public class StudyTwoUIView extends View {

    private final static String TAG = "StudyUIView";
    private Paint inputPaint = new Paint();
    private Paint bgPaint = new Paint();
    private Paint subMenuPaint = new Paint();
    private Paint subMenuSliderPaint = new Paint();
    private Paint strokePaint = new Paint();
    private int screenWidth, screenHeight;
    private Path touchPath = new Path();
    private int touchLength = 10;
    private ArrayList<PointF> touchPoints;

    private float menuDistance = 120.0f;
    private PointF menuCenter = new PointF();

    private Runnable strokeDeleting;
    private Handler mHandler;

    public int currentMenuLayer = -1;

    public boolean isTriggered = false;
    public boolean isSubMenuing = false;

    private boolean isSubMenuVertical = true;
    private boolean isSubMenuLeftTop = false;
    private float subMenuWidth = 60;
    private int currentSubMenu = -1;
    private PointF subMenuTouchAchor = new PointF();


    //first layer menu
    private ArrayList<String> menuItems;



    //continuous bar
    private float barWidth = 40;

    private float offsetX = 3;
    private float offsetY = 3;


    private PointF listCursor = new PointF();
    boolean isDrawingCursor = true;

    //task 1, alphabet
    public ArrayList<String> task_alphabet;

    //task 2, number
    public ArrayList<String> task_number;

    //task 3, shape
    public ArrayList<PointF[]> task_shape;




    //tasks
    public ArrayList<int[]> tasks;
    public ArrayList<int[]> tempTasks;
    public int taskCount;  // 6
    public int currentAttempt = 0;
    //this should be currentTask, just to keep the same with study one
    public int currentTask = -1;
    private Random rand;
    private int repeat = 5;

    private int closeBound = 50;

    //data record
    public int trialState;
    public long trialDuration;
    public long trialStartTime = 0;
    public long trialEndTime = 0;
    public int isCorrect;  // 1 - correct, 0 - incorrect

    //these willn not be used
    public int numVistedCells;
    public int numOvershoot;

    //current task
    public int mTask;

    //for discrete task
    public int mAngleTarget = -1;
    public int mDistanceTargert = -1;
    public int mAngleNum = 3;
    public int mDistanceNum = 5;
    public int mClose;
    public float mCloseValue;
    public int mAngleActual = -1;
    public int mDistanceActual = -1;

    //for continuous values

    public float mContinuousMax = 120;
    public float reservedDistance = 40;
    public float mContinuousTarget = -1;
    public float mContinuousActual = -1;
    public float accuracyInterval = 0.05f;  // + -


    public boolean obtainNext = true;




    public StudyTwoUIView(Context context, AttributeSet attrs)
    {
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

        menuItems = new ArrayList<String>();
        menuItems.add("Letter");
        menuItems.add("Number");
        menuItems.add("Icon");
        menuItems.add("Size");
        menuItems.add("Color");
        menuItems.add("Weight");


        //task 1
        task_alphabet = new ArrayList<String>();
        task_alphabet.add("A");
        task_alphabet.add("B");
        task_alphabet.add("C");
        task_alphabet.add("D");
        task_alphabet.add("E");

        task_number = new ArrayList<String>();
        task_number.add("1");
        task_number.add("2");
        task_number.add("3");
        task_number.add("4");
        task_number.add("5");

        task_shape = new ArrayList<PointF[]>();
        task_shape.add(new PointF[]{new PointF(0, 0), new PointF(1, 0), new PointF(1, 1), new PointF(0, 1), new PointF(0, 0)});
        //this one will not be used
        task_shape.add(new PointF[]{new PointF(0, 0), new PointF(1, 0), new PointF(1, 1), new PointF(0, 1), new PointF(0, 0)});

        task_shape.add(new PointF[]{new PointF(0.5f, 0), new PointF(1, 1), new PointF(0, 1), new PointF(0.5f, 0)});
        task_shape.add(new PointF[]{new PointF(0.5f, 0), new PointF(1, 0.5f), new PointF(0.75f, 1), new PointF(0.25f, 1), new PointF(0, 0.5f), new PointF(0.5f, 0)});
        task_shape.add(new PointF[]{new PointF(0, 0.25f), new PointF(1, 0.25f), new PointF(1, 0.75f), new PointF(0, 0.75f), new PointF(0, 25f)});

        touchPoints = new ArrayList<PointF>();


        //set up the tasks
        tasks = new ArrayList<int[]>();
        tempTasks = new ArrayList<int[]>();
        rand = new Random();

        for(int itr = 1; itr < 7; itr++)
        {
            for(int itrt = 0; itrt < repeat; itrt++)
            {
                int close = rand.nextInt(closeBound);
                //discrete
                tempTasks.add(new int[]{itr, 1, close});

                int far = rand.nextInt(closeBound) + closeBound;
                tempTasks.add(new int[]{itr, 2, far});
            }
        }

        taskCount = tempTasks.size();

        //randomize the order
        ArrayList<Integer> order = new ArrayList<Integer>();
        for(int itro = 0; itro < taskCount; itro++)
        {
            order.add(itro);
        }

        for(int itrc = 0; itrc < taskCount; itrc++)
        {
            int picked = rand.nextInt(order.size());
            int temp = order.get(picked);
            tasks.add(tempTasks.get(temp));

            for(int itrr = 0; itrr < order.size(); itrr++)
            {
                if(order.get(itrr) == temp)
                {
                    order.remove(itrr);
                    continue;
                }
            }
        }
        //the task orders has been set

        //get the first task
        ReloadTrial();


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

    public void ReloadTrial()
    {
        int[] curTask;

        if(obtainNext == true)
        {
            curTask = obtainNextTask();
        }else
        {
            curTask = obtainCurrentTask();
        }

        obtainNext = false;

        mTask = curTask[0];
        mClose = curTask[1];
        mCloseValue = curTask[2] / 100.0f;

        if(mTask < 4)
        {
            isSubMenuLeftTop = false;
        }else
        {
            isSubMenuLeftTop = true;
        }

        //1, 2, 3, 4, 5, 6
        mAngleTarget = mTask;

        //distance target
        mDistanceTargert = (int)(mDistanceNum * mCloseValue);
        mContinuousTarget = mContinuousMax * mCloseValue + reservedDistance;
        mDistanceActual = -1;
        mContinuousActual = -1;
        mAngleActual = -1;

    }

    public int[] obtainNextTask()
    {
        currentTask++;
        currentAttempt = 1;

        trialState = 0;
        trialDuration = 0;
        trialStartTime = 0;
        trialEndTime = 0;
        isCorrect = 0;

        if(currentTask == tasks.size())
        {
            //save
            currentTask = 0;
        }

        return tasks.get(currentTask);
    }

    public int[] obtainCurrentTask()
    {
        currentAttempt++;

        trialState = 0;
        trialDuration = 0;
        trialStartTime = 0;
        trialEndTime = 0;
        isCorrect = 0;

        return tasks.get(currentTask);
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

        Paint p = new Paint();
        p.setFilterBitmap(true);

        //draw the target
        int fontSize = calcFontSize(20);
        p.setColor(Color.GRAY);
        p.setStrokeWidth(1);
        p.setAntiAlias(true);
        p.setTextSize(fontSize);

        String taskText = "" + (currentTask + 1)
                + " / " + taskCount;

        float textWidth = p.measureText(taskText);
        float y = screenHeight / 4;
        float x = screenWidth / 2 - textWidth / 2;
        canvas.drawText(taskText, x, y, p);

        //draw target
        if(mTask == 1)
        {
            //draw letter
            p.setTextSize(calcFontSize(40));
            p.setColor(Color.BLUE);
            y = screenHeight/2 + 10;
            taskText = task_alphabet.get(mDistanceTargert);
            textWidth = p.measureText(taskText);
            x = screenWidth / 2 - textWidth/2;
            canvas.drawText(taskText, x, y, p);
        }else if(mTask == 2)
        {
            //draw number
            p.setTextSize(calcFontSize(40));
            p.setColor(Color.BLUE);
            y = screenHeight/2 + 10;
            taskText = "" + task_number.get(mDistanceTargert);
            textWidth = p.measureText(taskText);
            x = screenWidth / 2 - textWidth/2;
            canvas.drawText(taskText, x, y, p);

        }else if(mTask == 3)
        {
            //draw shape
            float scale = 40;
            x = screenWidth / 2;
            y = screenHeight / 2;
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
                canvas.drawPath(path, p);
            }else
            {
                p.setColor(Color.BLUE);
                p.setStyle(Paint.Style.FILL);
                canvas.drawCircle(x, y, scale*0.5f, p);
            }

        }else if(mTask == 4)
        {
            //font size
            if(mContinuousTarget != -1 && mContinuousTarget < (mContinuousMax + reservedDistance))
            {
                Path path = new Path();
                float mx = screenWidth/2;
                float my = screenHeight/2;
                float offx = 320 - mContinuousTarget / 2 - mx;
                float offy = mContinuousTarget / 2 - my;

                path.moveTo(320 - offx, 0 - offy);
                path.lineTo(320 - offx, mContinuousTarget- offy);
                path.lineTo(320 - mContinuousTarget - offx, mContinuousTarget- offy);
                path.lineTo(320 - offx, 0- offy);

                p.setStyle(Paint.Style.STROKE);
                p.setColor(Color.BLUE);
                p.setStrokeWidth(2);
                canvas.drawPath(path, p);

                path.reset();
                path.moveTo(300 - offx, 0 - offy);
                path.lineTo(300 - offx, mContinuousTarget- offy);
                canvas.drawPath(path, p);

            }
        }else if(mTask == 5)
        {
            //color
            Path path = new Path();
            float mx = screenWidth/2;
            float my = screenHeight/2;
            float offx = 320 - 160 / 2 - mx;
            float offy = 160 / 2 - my;

            path.moveTo(320 - offx, 0 - offy);
            path.lineTo(320 - offx, 160 - offy);
            path.lineTo(320 - 160 - offx, 160- offy);
            path.lineTo(320 - offx, 0- offy);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(255, 0, 0, (int)(255 * (mContinuousTarget / (mContinuousMax + reservedDistance) ))));
            canvas.drawPath(path, p);

        }else if(mTask == 6)
        {

            //wegght
            Path path = new Path();
            float mx = screenWidth/2;
            float my = screenHeight/2;
            float offx = 320 - 160 / 2 - mx;
            float offy = 160 / 2 - my;

            path.moveTo(320 - offx, 0 - offy);
            path.lineTo(320 - offx, 160 - offy);
            path.lineTo(320 - 160 - offx, 160- offy);
            path.lineTo(320 - offx, 0- offy);

            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.BLUE);
            p.setStrokeWidth( 20 * (mContinuousTarget / (mContinuousMax + reservedDistance) ));

            canvas.drawPath(path, p);
        }




        if(!isTriggered)
        {


        }else
        {
            if(!isSubMenuing)
            {
                /**
                 *  1         6
                 *
                 *  2         5
                 *
                 *  3         4
                 */

                //show marking menu
                float midX = screenWidth / 2;
                float midY = screenHeight / 2;
                float menuAngle = 5 *  (float) Math.PI / 4.0f;
                float menuAngleSeg = (float) Math.PI / 4.0f;

                for(int itrc = 0; itrc < menuItems.size(); itrc++)
                {
                    float angleItr = itrc;
                    if(itrc >= 3)
                    {
                        angleItr++;
                    }
                    float mx = midX + menuDistance * (float)Math.cos(menuAngle - menuAngleSeg * angleItr);
                    float my = midY + menuDistance * (float)Math.sin(menuAngle - menuAngleSeg * angleItr);

                    String text = menuItems.get(itrc);
                    float textLength = inputPaint.measureText(text);

                    canvas.drawText(text, mx - textLength / 2, my, inputPaint);
                }


                //finger stoke
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
                //show submenu task
                if(mTask == 1)
                {
                    drawListPanel(canvas, isSubMenuVertical, isSubMenuLeftTop, task_alphabet);
                }else if(mTask == 2)
                {
                    drawListPanel(canvas, isSubMenuVertical, isSubMenuLeftTop, task_number);
                }else if(mTask == 3)
                {
                    drawShapePanel(canvas, isSubMenuVertical, isSubMenuLeftTop, task_shape);
                }else if(mTask == 4)
                {

                }else if(mTask == 5)
                {

                }else if(mTask == 6)
                {

                }

            }
        }



    }


    private void drawListPanel(Canvas canvas, boolean isVertical, boolean isLeftTop, ArrayList<String> list)
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

                //draw list cursor
                if(isDrawingCursor)
                {
                    canvas.drawLine(anchorX, anchorY + listCursor.y, anchorX + mWidth * 2, anchorY + listCursor.y, subMenuSliderPaint);
                }
            }else
            {
                //on right
                anchorX = screenWidth - subMenuWidth;
                //draw list cursor
                if(isDrawingCursor)
                {
                    canvas.drawLine(anchorX - mWidth, anchorY + listCursor.y, anchorX + mWidth, anchorY + listCursor.y, subMenuSliderPaint);
                }
            }

            //draw the list segments
            float segment = mHeight / list.size();
            for(int itrc = 0; itrc < list.size(); itrc++)
            {
                canvas.drawText(list.get(itrc), anchorX, anchorY + segment * (itrc + 1), subMenuPaint);
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
                anchorY = mHeight;
                //draw list cursor
                if(isDrawingCursor)
                {
                    canvas.drawLine(anchorX + listCursor.x, anchorY - mHeight, anchorX + listCursor.x, anchorY + mHeight, subMenuSliderPaint);
                }
            }else
            {
                //on bottom
                anchorY = screenHeight;
                //draw list cursor
                if(isDrawingCursor)
                {
                    canvas.drawLine(anchorX + listCursor.x, anchorY - mHeight, anchorX + listCursor.x, anchorY + mHeight, subMenuSliderPaint);
                }
            }

            float segment = mWidth / list.size();
            for(int itrc = 0; itrc < list.size(); itrc++)
            {
                canvas.drawText(list.get(itrc), anchorX + segment * itrc, anchorY, subMenuPaint);
            }

        }

    }

    private void drawShapePanel(Canvas canvas, boolean isVertical, boolean isLeftTop, ArrayList<PointF[]> list)
    {
        float mWidth;
        float mHeight;
        float anchorX;
        float anchorY;

        subMenuPaint.setStyle(Paint.Style.FILL);
        subMenuPaint.setColor(Color.BLUE);

        if(isVertical)
        {
            mWidth = subMenuWidth;
            mHeight = screenHeight;
            anchorY = 0;

            if(isLeftTop)
            {
                //on left
                anchorX = 0;

                //draw list cursor
                if(isDrawingCursor)
                {
                    canvas.drawLine(anchorX, anchorY + listCursor.y, anchorX + mWidth * 2, anchorY + listCursor.y, subMenuSliderPaint);
                }
            }else
            {
                //on right
                anchorX = screenWidth - subMenuWidth;
                //draw list cursor
                if(isDrawingCursor)
                {
                    canvas.drawLine(anchorX - mWidth, anchorY + listCursor.y, anchorX + mWidth, anchorY + listCursor.y, subMenuSliderPaint);
                }
            }

            //draw the list segments
            float segment = mHeight / list.size();
            for(int itrc = 0; itrc < list.size(); itrc++)
            {
                //canvas.drawText(list.get(itrc), anchorX, anchorY + segment * (itrc + 1), subMenuPaint);

                float scale = 40;
                float xoffSet = anchorX + subMenuWidth / 2  - scale * 0.5f;
                float yoffSet = anchorY + segment * (itrc + 0.5f) - scale * 0.5f;

                if(itrc != 1)
                {
                    Path path = new Path();
                    path.moveTo(list.get(itrc)[0].x * scale + xoffSet, list.get(itrc)[0].y * scale + yoffSet);
                    for(int itrs = 1; itrs < list.get(itrc).length; itrs++)
                    {
                        path.lineTo(list.get(itrc)[itrs].x * scale + xoffSet, list.get(itrc)[itrs].y * scale + yoffSet);
                    }

                    canvas.drawPath(path, subMenuPaint);
                }else
                {
                    canvas.drawCircle(anchorX + subMenuWidth / 2, anchorY + segment * (itrc + 0.5f), scale*0.5f, subMenuPaint);
                }

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
                anchorY = mHeight;
                //draw list cursor
                if(isDrawingCursor)
                {
                    canvas.drawLine(anchorX + listCursor.x, anchorY - mHeight, anchorX + listCursor.x, anchorY + mHeight, subMenuSliderPaint);
                }
            }else
            {
                //on bottom
                anchorY = screenHeight;
                //draw list cursor
                if(isDrawingCursor)
                {
                    canvas.drawLine(anchorX + listCursor.x, anchorY - mHeight, anchorX + listCursor.x, anchorY + mHeight, subMenuSliderPaint);
                }
            }

            float segment = mWidth / list.size();
            for(int itrc = 0; itrc < list.size(); itrc++)
            {
                //canvas.drawText(list.get(itrc), anchorX + segment * itrc, anchorY, subMenuPaint);
                float scale = 20;
                float xoffSet = anchorX + segment * itrc - scale * 0.5f;
                float yoffSet = anchorY - scale * 0.5f;

                if(itrc != 1)
                {
                    Path path = new Path();
                    path.moveTo(list.get(itrc)[0].x * scale + xoffSet, list.get(itrc)[0].y * scale + yoffSet);
                    for(int itrs = 1; itrs < list.get(itrc).length; itrs++)
                    {
                        path.lineTo(list.get(itrc)[itrs].x * scale + xoffSet, list.get(itrc)[itrs].y * scale + yoffSet);
                    }

                    canvas.drawPath(path, subMenuPaint);
                }else
                {
                    canvas.drawCircle(anchorX + segment * itrc, anchorY, scale*0.5f, subMenuPaint);
                }

            }

        }

    }


    public void onFingerDown(float x, float y)
    {
        touchPoints.clear();
        touchPoints.add(new PointF(x, y));
    }

    public void onFingerMove(float x, float y)
    {
        if(isTriggered)
        {

            if(!isSubMenuing)
            {
                touchPoints.add(new PointF(x, y));

                //to select a menu
                detectGesture(touchPoints);
                if(mAngleActual == mAngleTarget)
                {
                    isSubMenuing = true;

                }

            }else
            {
                //to select a value
                listCursor.set(x, y);
            }



            invalidate();
        }
    }

    public void onFingerUp(float x, float y)
    {
        isTriggered = false;


        mAngleActual = -1;
        mDistanceActual = -1;
        mContinuousActual = -1;

        //check the result

        //show the next target
        if(isSubMenuing)
        {
            obtainNext = true;
        }else {
            obtainNext = false;
        }
        ReloadTrial();
        isSubMenuing = false;

        invalidate();
    }

    public void onLongPressed(float x, float y)
    {
        //menuCenter.set(x, y);
        isTriggered = true;
        currentMenuLayer = 0;
        invalidate();
    }

    //detect which menu selected
    private void detectGesture(ArrayList<PointF> points)
    {
        /**
         *  1         6
         *
         *  2         5
         *
         *  3         4
         */

        if(points.size() > 2)
        {
            float origX = points.get(0).x;
            float origY = points.get(0).y;
            float destX = points.get(points.size() - 1).x;
            float destY = points.get(points.size() - 1).y;

            if(calDistance(origX, origY, destX, destY) > menuDistance)
            {
                //calculate the angle
                float angle = calAngle(origX, origY, destX, destY);

                //Log.d(TAG, "" + angle);
                if(angle > 202.5 && angle < 247.5)
                {
                    mAngleActual = 1;
                }else if(angle  > 157.5 && angle < 202.5)
                {
                    mAngleActual = 2;
                }else if(angle > 112.5 && angle < 157.5)
                {
                    mAngleActual = 3;
                }else if(angle > 292.5 && angle < 337.5)
                {
                    mAngleActual = 6;
                }else if(angle > 337.5 || angle < 22.5)
                {
                    mAngleActual = 5;
                }else if(angle > 22.5 && angle < 67.5)
                {
                    mAngleActual = 4;
                }

            }
        }
    }


    private float calDistance(float x1, float y1, float x2, float y2)
    {
        return (float)Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    private float calAngle(float x1, float y1, float x2, float y2)
    {
        float angle = (float) Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
        if(angle < 0){
            angle += 360;
        }
        return angle;
    }

    private void drawListPanel(Canvas canvas, boolean isVertical, boolean isLeftTop)
    {

    }

    protected int calcFontSize(int size)
    {
        return (int)(size * MainActivity.getSharedInstance().getResources().getDisplayMetrics().scaledDensity);
    }

}

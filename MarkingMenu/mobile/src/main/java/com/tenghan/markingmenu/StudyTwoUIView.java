package com.tenghan.markingmenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
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

    private float menuDistance = 80.0f;
    private PointF menuCenter = new PointF();

    private Runnable strokeDeleting;
    private Handler mHandler;

    public int currentMenuLayer = -1;

    public boolean isTriggered = false;
    public boolean isSubMenuing = false;

    private boolean isSubMenuVertical = false;
    private boolean isSubMenuLeftTop = false;
    private float subMenuWidth = 60;
    private int currentSubMenu = -1;
    private PointF subMenuTouchAchor = new PointF();

    //continuous bar
    private float barWidth = 40;

    private float offsetX = 3;
    private float offsetY = 3;


    private ArrayList<String> scrollList;
    private int totallists = 5;
    private PointF listCursor = new PointF();
    boolean isDrawingCursor = true;
    private String[] alphabet = {"A", "B", "C", "D", "E"};
    private String[] number = {"1", "2", "3", "4", "5"};


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

        scrollList = new ArrayList<String>();
        generateScrollList(totallists);

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


    private void generateScrollList(int num)
    {
        for(int itrl =0; itrl < num; itrl++)
        {
            scrollList.add(alphabet[itrl]);
        }
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

        if(!isSubMenuing)
        {
            /**
             *  1         4
             *
             *  2         5
             *
             *  3         6
             */


        }else
        {

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

            invalidate();
        }
    }

    public void onFingerUp(float x, float y)
    {
        isTriggered = false;
        isSubMenuing = false;

        //show the next target

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
    private int detectGesture(ArrayList<PointF> points)
    {
        int gestureResult = -1;
        /**
         *  1         4
         *
         *  2         5
         *
         *  3         6
         */



        return gestureResult;
    }


    private float calDistance(float x1, float y1, float x2, float y2)
    {
        return (float)Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    private float calAngle(float x1, float y1, float x2, float y2)
    {
        return 0;
    }

    private void drawListPanel(Canvas canvas, boolean isVertical, boolean isLeftTop)
    {

    }

}

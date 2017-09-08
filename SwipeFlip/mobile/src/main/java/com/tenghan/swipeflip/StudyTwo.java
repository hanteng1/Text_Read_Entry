package com.tenghan.swipeflip;

import android.content.Context;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by hanteng on 2017-09-08.
 */

public class StudyTwo extends PageFlipModifyAbstract{

    private final static String TAG = "Study Two";
    private final static int pageSize = 2;

    //conditions
    public ArrayList<Integer> tasks;
    public ArrayList<int[]> conditions;
    private ArrayList<Integer> tempTasks;
    private int taskCount;  // 6
    public int currentAttempt = 0;
    //this should be currentTask, just to keep the same with study one
    public int currentCondition = -1;
    public int currentTask = -1;
    private Random rand;
    private int repeat = 5;

    public PointF cursor = new PointF();


    //1 - start, 2 - move, 3 - end
    public int trialState;
    public long trialDuration;
    public long trialStartTime = 0;
    public long trialEndTime = 0;
    public int isCorrect;  // 1 - correct, 0 - incorrect

    //these willn not be used
    public int numVistedCells;
    public int numOvershoot;


    public StudyTwo(Context context)
    {
        super(context, pageSize);

        tasks = new ArrayList<Integer>();
        tempTasks = new ArrayList<Integer>();
        rand = new Random();

        conditions = new ArrayList<int[]>();

        for(int itrt = 0; itrt < repeat; itrt++)
        {
            //discrete
            tempTasks.add(1);
            tempTasks.add(2);
            tempTasks.add(3);
            //continuous
            tempTasks.add(4);
            tempTasks.add(5);
            tempTasks.add(6);
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

    }

    public int obtainNextTask()
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

    //fake function
    public int[] obtainNextCondition()
    {
        return new int[]{};
    }

    public int[] obtainCurrentCondition()
    {
        return new int[]{};
    }



    public boolean onFingerMove(float touchX, float touchY)
    {


        return false;
    }


    public void computeVertexesAndBuildPage()
    {

    }

}

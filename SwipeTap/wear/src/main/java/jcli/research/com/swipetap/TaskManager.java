package jcli.research.com.swipetap;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by jchrisli on 2017-09-09.
 */

public class TaskManager {

    private static final TaskManager mSelf = new TaskManager();
    public static TaskManager getInstance() { return mSelf; }

    private int mCurrentTrialNum = 0;
    private boolean mIsFirstTrial = true;
    private ArrayList<ExpTask> mTasks;

    public boolean isFirstTrial() {
        return mIsFirstTrial;
    }

    public TaskManager() {
        //Generate all the tasks
        generateTasks();
    }

    private void generateTasks () {
        mTasks = new ArrayList<ExpTask>();
        ArrayList<int[]> tasks = new ArrayList<int[]>();
        ArrayList<int[]> tempTasks = new ArrayList<int[]>();
        Random rand = new Random();
        int repeat = 3;
        int closeBound = 20;
        int taskCount;

        for(int itr = 1; itr < 7; itr++)
        {
            for(int itrt = 0; itrt < repeat; itrt++)
            {
                int level1 = rand.nextInt(closeBound);
                //discrete
                tempTasks.add(new int[]{itr, 1, level1});

                int level2 = rand.nextInt(closeBound) + closeBound;
                tempTasks.add(new int[]{itr, 2, level2});

                int level3 = rand.nextInt(closeBound) + 2*closeBound;
                tempTasks.add(new int[]{itr, 3, level3});

                int level4 = rand.nextInt(closeBound) + 3*closeBound;
                tempTasks.add(new int[]{itr, 4, level4});

                int level5 = rand.nextInt(closeBound) + 4*closeBound;
                tempTasks.add(new int[]{itr, 5, level5});
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

        for(int itrtt = 0; itrtt < taskCount; itrtt++) {
            int[] task = tasks.get(itrtt);
            mTasks.add(new ExpTask(task[0] - 1, task[1] - 1, task[2]));
        }
    }

    public ExpTask getNextTask() {
        //Not the first trial anymore
        if(mIsFirstTrial) mIsFirstTrial = false;
        if(mCurrentTrialNum + 1 < mTasks.size()) {
            //make sure we don't have invalid index
            mCurrentTrialNum++;
            return mTasks.get(mCurrentTrialNum);
        }
        else return null;
    }

    //Get the current tasks, because the previous trial for this task failed
    public ExpTask getCurrentTask () {
        return mTasks.get(mCurrentTrialNum);
    }
}


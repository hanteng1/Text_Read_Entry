package jcli.research.com.swipetap;

/**
 * Created by jchrisli on 2017-09-09.
 */

public class TaskManager {

    private static final TaskManager mSelf = new TaskManager();
    public static TaskManager getInstance() { return mSelf; }

    private int mCurrentTrialNum = 0;

    public boolean isFirstTrial() {
        return mCurrentTrialNum == 0;
    }

    public TaskManager() {
        //Generate all the tasks
    }

    public ExpTask getNextTask() {
        //temporary code

        return new ExpTask(1, 1, -1);
    }
}


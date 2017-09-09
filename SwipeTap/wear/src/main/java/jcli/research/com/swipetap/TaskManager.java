package jcli.research.com.swipetap;

/**
 * Created by jchrisli on 2017-09-09.
 */

public class TaskManager {

    private static final TaskManager mSelf = new TaskManager();
    public static TaskManager getInstance() { return mSelf; }

    public ExpTask getNextTask() {
        //temporary code
        return new ExpTask(3, 0, 50);
    }
}


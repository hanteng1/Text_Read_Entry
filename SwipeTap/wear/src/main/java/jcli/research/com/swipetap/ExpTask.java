package jcli.research.com.swipetap;

import java.util.Arrays;

public class ExpTask {
    private String[] mTaskNames = new String[]{"Letter", "Number", "Shape", "Size", "Colour", "Weight"};
    private int mClose;
    private int mTaskValue;
    private int mTaskInd;

    public ExpTask(int taskInd, int closeInd, int value) {
        mTaskInd = taskInd;
        mClose = closeInd;
        mTaskValue = value;
    }

    public boolean isDiscrete () { return mTaskInd < 3; }

    public int getTaskInd () {return mTaskInd; }

    public int getValue() {return mTaskValue;}
}

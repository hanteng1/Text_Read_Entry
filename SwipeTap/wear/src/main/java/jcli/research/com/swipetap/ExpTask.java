package jcli.research.com.swipetap;

import java.util.Arrays;

public class ExpTask {
    private String[] mTaskNames = new String[]{"Letter", "Number", "Shape", "Size", "Colour", "Weight"};
    private int mClose;
    private float mTaskValue;
    private int mTaskInd;

    public ExpTask(int taskInd, int closeInd, float value) {
        mTaskInd = taskInd;
        mClose = closeInd;
        mTaskValue = value;
    }

    public boolean isDiscrete () { return mTaskInd < 3; }

    public int getTaskInd () {return mTaskInd; }

    public float getValue() {return mTaskValue;}
}

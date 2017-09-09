package jcli.research.com.swipetap;

import java.util.Arrays;

public class ExpTask {
    private String[] mTaskNames = new String[]{"Letter", "Number", "Shape", "Size", "Colour", "Weight"};
    private String mTaskType;
    private int mTaskTargetInd;
    private float mTaskValue;
    private int mTaskInd;

    public ExpTask(int typeInd, int targetInd, float value) {
        mTaskInd = typeInd;
        mTaskType = mTaskNames[typeInd];
        mTaskTargetInd = targetInd;
        mTaskValue = value;
    }

    public boolean isDiscrete () { return mTaskInd < 3 && mTaskInd >= 0; }

    public String getType() {
        return mTaskType;
    }

    public int getTargetInd() {
        return mTaskTargetInd;
    }

    public float getValue() {return mTaskValue;}
}

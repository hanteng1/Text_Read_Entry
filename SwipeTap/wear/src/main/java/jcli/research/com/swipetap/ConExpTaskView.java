package jcli.research.com.swipetap;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jchrisli on 2017-09-09.
 */

public class ConExpTaskView extends View {

    private String mCurrentTaskType ="";
    private float mCurrentTaskValue = -1.0f; //Target value
    private float mCurrentValue = 0.0f;

    public void setTask (String type, float value) {
        mCurrentTaskType = type;
        mCurrentTaskValue = value;
    }

    public ConExpTaskView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void updateState (float progress) {
        //Set the progress according
        mCurrentValue = progress;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //update the canvas according to current task type and value
        //Do not do anything if the task type is empty and current value is negatives
        super.onDraw(canvas);
    }
}

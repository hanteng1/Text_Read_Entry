package jcli.research.com.swipetap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class ConExpActivity extends Activity {

    private final static String TAG = "ConExpActivity";

    private SeekBar mSeekBar;
    private ConExpTaskView mCanvasView;
    private ConExpActivity mSelf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_con_exp);

        mSelf = this;

        mSeekBar = (SeekBar) findViewById(R.id.con_exp_seekbar);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mSeekBar.setOnTouchListener(mSeekBarTouchListener);
        mCanvasView = (ConExpTaskView) findViewById(R.id.con_exp_canvas_view);

        //Get the data passed in
        Intent intent = getIntent();
        int task = intent.getIntExtra("task", -1);
        int targetValue = intent.getIntExtra("value", -1);
        //mCanvasView.setTask(type, targetValue);
        mCanvasView.setTask(task, targetValue);
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            mCanvasView.updateState(i);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

            //check the result and save


        }
    };

    private View.OnTouchListener mSeekBarTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                //Finger up for commitment
                if(true) {
                    //TODO: record data
                    //correct, go back to the main activity
                    setResult(RESULT_OK);
                } else {
                    //TODO: record data
                    //wrong
                    setResult(RESULT_CANCELED);
                }
                finish();
            }
            return false;
        }
    };
}

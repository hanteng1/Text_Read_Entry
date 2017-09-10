package jcli.research.com.swipetap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

public class ConExpActivity extends Activity {

    private final static String TAG = "ConExpActivity";

    private SeekBar mSeekBar;
    private ConExpTaskView mCanvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_con_exp);

        mSeekBar = (SeekBar) findViewById(R.id.con_exp_seekbar);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
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
}

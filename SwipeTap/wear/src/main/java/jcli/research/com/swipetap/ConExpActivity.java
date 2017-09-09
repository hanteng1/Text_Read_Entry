package jcli.research.com.swipetap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.SeekBar;
import android.widget.TextView;

public class ConExpActivity extends Activity {

    private SeekBar mSeekBar;
    private ConExpTaskView mCanvasView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_con_exp);
        mSeekBar = findViewById(R.id.con_exp_seekbar);
        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mCanvasView = findViewById(R.id.con_exp_canvas_view);

        //Get the data passed in
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        float targetValue = intent.getFloatExtra("value", -1);
        mCanvasView.setTask(type, targetValue);
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            mCanvasView.updateState(((float)i) / seekBar.getMax());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}

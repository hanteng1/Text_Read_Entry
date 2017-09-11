package jcli.research.com.swipetap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpActivity extends Activity {

    //private TextView mTextView;
    private List<String> mOptions;
    private int mTargetIndInOptions;
    private ExpActivity mSelf;
    private long mActivityStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exp);

        mSelf = this;
        //Get the data passed in
        Intent intent = getIntent();
        int task = intent.getIntExtra("task", -1);
        int target = (int) (5 * (intent.getIntExtra("target", -1) / 100.0f));

        //Populate option list according to target type
        final WearableListView optionListView = (WearableListView)findViewById(R.id.exp_option_list);
        //L for letter, N for number, S for shape
        if(task == 0){
            mOptions = Arrays.asList("A", "B", "C", "D", "E");
        } else if (task == 1){
            mOptions = Arrays.asList("1", "2", "3", "4", "5");
        } else {
            mOptions = Arrays.asList("\u25a0", "\u25b2", "\u25cf", "\u2b1f", "\u25ac");
        }
        mTargetIndInOptions = target;
        final TextView tv = (TextView)findViewById(R.id.exp_target_text);
        tv.setText(mOptions.get(mTargetIndInOptions));

        optionListView.setAdapter(new ExpOptionListAdapter(this, mOptions));
        optionListView.setClickListener(mOptionClickListener);

        mActivityStartTime = System.currentTimeMillis();

    }

    private WearableListView.ClickListener mOptionClickListener = new WearableListView.ClickListener() {

        @Override
        public void onClick(WearableListView.ViewHolder viewHolder) {
            //Intent resultIntent = new Intent();
            Intent timeIntent = new Intent();
            timeIntent.putExtra("time", mActivityStartTime);
            if(viewHolder.getAdapterPosition() == mTargetIndInOptions) {
                //TODO: record data
                //correct, go back to the main activity
                setResult(RESULT_OK, timeIntent);
            } else {
                //TODO: record data
                //wrong
                setResult(RESULT_CANCELED, timeIntent);
            }
            finish();
        }

        @Override
        public void onTopEmptyRegionClick() {
            //do nothing
        }
    };
}

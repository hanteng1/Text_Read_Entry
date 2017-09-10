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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exp);

        //Get the data passed in
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        int target = intent.getIntExtra("target", -1);

        //Populate option list according to target type
        final WearableListView optionListView = (WearableListView)findViewById(R.id.exp_option_list);
        //L for letter, N for number, S for shape
        if(type.equals("Letter") ){
            mOptions = Arrays.asList("A", "B", "C", "D", "E");
        } else if (type.equals("Number")){
            mOptions = Arrays.asList("1", "2", "3", "4", "5");
        } else {
            mOptions = Arrays.asList("\u25a0", "\u25b2", "\u25cf", "\u2b1f", "\u25ac");
        }
        mTargetIndInOptions = target;
        final TextView tv = (TextView)findViewById(R.id.exp_target_text);
        tv.setText(mOptions.get(mTargetIndInOptions));

        optionListView.setAdapter(new ExpOptionListAdapter(this, mOptions));
        optionListView.setClickListener(mOptionClickListener);

    }

    private WearableListView.ClickListener mOptionClickListener = new WearableListView.ClickListener() {

        @Override
        public void onClick(WearableListView.ViewHolder viewHolder) {
            if(viewHolder.getAdapterPosition() == mTargetIndInOptions) {
                //correct
            } else {
                //wrong
            }
        }

        @Override
        public void onTopEmptyRegionClick() {
            //do nothing
        }
    };
}

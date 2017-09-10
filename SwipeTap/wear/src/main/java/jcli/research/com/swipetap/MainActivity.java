package jcli.research.com.swipetap;

/**
 * Created by jchrisli on 2017-09-07.
 */

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.WearableListView;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class MainActivity extends Activity{

    //private static ArrayList<Integer> mContinuousIcons;
    private static List<String> mNames;
    private MainActivity mSelf;
    private ExpTask mNextTask;
    private TextView mTargetDisplayTextView;
    private GridViewPager mPager;
    private Button mStartButton;

    private static final String[] LETTER_OPTIONS = new String[] {"A", "B", "C", "D", "E"};
    private static final String[] NUMBER_OPTIONS = new String[] {"1", "2", "3", "4", "5"};
    private static final String[] SHAPE_OPTIONS = new String[] {"\u25a0", "\u25b2", "\u25cf", "\u2b1f", "\u25ac"};

    public static MainActivity instance;
    public static MainActivity getSharedInstance()
    {
        if(instance == null)
        {
            instance = new MainActivity();
        }
        return instance;
    }

    public ConTargetView mConTargetView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        mNames = Arrays.asList("Letter", "Number", "Shape", "Size", "Colour", "Weight");

        mPager = (GridViewPager) findViewById(R.id.pager);

        //---Assigns an adapter to provide the content for this pager---
        mPager.setAdapter(new ListInGridAdapter(this));
        mSelf = this;

        //Get the next task
        mNextTask = TaskManager.getInstance().getNextTask();

    }

    // Handle our Wearable List's click events
    private WearableListView.ClickListener mListClickListener =
            new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {

                    int indClicked = viewHolder.getAdapterPosition();
                    //Check if the correct option is clicked
                    if(indClicked == mNextTask.getTaskInd()) {
                        if(mNextTask.isDiscrete()) {
                            //So it is a discrete task, start the discrete activity
                            Intent disIntent = new Intent(mSelf, ExpActivity.class);
                            disIntent.putExtra("task", mNextTask.getTaskInd());
                            disIntent.putExtra("target", mNextTask.getValue());
                            startActivity(disIntent);
                        } else {
                            //So it is a continuous task
                            Intent conIntent = new Intent(mSelf, ConExpActivity.class);
                            conIntent.putExtra("task", mNextTask.getTaskInd());
                            conIntent.putExtra("value", mNextTask.getValue());
                            startActivity(conIntent);
                        }
                    } else {
                        //Wrong option clicked, return to task display
                        //TODO: increase error count
                        mPager.setCurrentItem(0, 0);
                    }

                }

                @Override
                public void onTopEmptyRegionClick() {
                }
            };



    public class ListInGridAdapter extends GridPagerAdapter {
        private final static String TAG = "listingridadapter";

        final Context mContext;

        public ListInGridAdapter(final Context context) {
            mContext = context;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount(int i) {
            return 2;
        }

        //---Go to current column when scrolling up or down (instead of default column 0)---
        @Override
        public int getCurrentColumnForRow(int row, int currentColumn) {
            return currentColumn;
        }

        //---Return our car image based on the provided row and column---
        @Override
        public Object instantiateItem(ViewGroup viewGroup, int row, int col) {
            //Inflate new list view from XML
            LayoutInflater inflater = LayoutInflater.from(mContext);
            switch (col) {
                case 0:
                    View centerView = inflater.inflate(R.layout.view_center, null);
                    mStartButton = (Button) centerView.findViewById(R.id.start_button);
                    mTargetDisplayTextView = (TextView)centerView.findViewById(R.id.target_display_text);
                    mConTargetView = (ConTargetView)centerView.findViewById(R.id.con_target_view);

                    if(!TaskManager.getInstance().isFirstTrial()) {
                        mStartButton.setVisibility(View.GONE);
                        mTargetDisplayTextView.setVisibility(View.VISIBLE);
                    }
                    
                    mStartButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mStartButton.setVisibility(View.GONE);
                            mTargetDisplayTextView.setVisibility(View.VISIBLE);
                        }
                    });
                    //Set the target display
                    if(mNextTask.isDiscrete()) {
                        //Is discrete task, set the target display with the actual target text
                        mConTargetView.setVisibility(View.INVISIBLE);
                        mTargetDisplayTextView.setVisibility(View.VISIBLE);

                        String targetDisplay;
                        int task = mNextTask.getTaskInd();
                        if(task == 0) {
                            targetDisplay = LETTER_OPTIONS[(int)(mNextTask.getValue() / 20.0f)];
                        } else if(task == 1) {
                            targetDisplay = NUMBER_OPTIONS[(int)(mNextTask.getValue() / 20.0f)];
                        } else targetDisplay = SHAPE_OPTIONS[(int)(mNextTask.getValue() / 20.0f)];
                        mTargetDisplayTextView.setText(targetDisplay);
                    } else {
                        //Is a continuous task, we should draw something then
                        mConTargetView.setTask( mNextTask.getTaskInd() + 1, (int)mNextTask.getValue());
                        mConTargetView.setVisibility(View.VISIBLE);
                        mTargetDisplayTextView.setVisibility(View.INVISIBLE);
                        mConTargetView.invalidate();

                    }
                    viewGroup.addView(centerView);
                    return centerView;
                case 1:
                    View discreteList = inflater.inflate(R.layout.list_side, null);
                    WearableListView discreteWearableListView =
                                (WearableListView) discreteList.findViewById(R.id.wearable_List);
                    discreteWearableListView.setAdapter(new WearableAdapter(mContext, mNames));
                    discreteWearableListView.setClickListener(mListClickListener);
                    discreteWearableListView.setGreedyTouchMode(true);
                    viewGroup.addView(discreteList);
                    return discreteList;
                default:
                    return null;

            }
            //viewGroup.addView(imageView);

        }

        @Override
        public void destroyItem(ViewGroup viewGroup, int i, int i2, Object o) {
            viewGroup.removeView((View) o);
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view.equals(o);
        }
    }

}
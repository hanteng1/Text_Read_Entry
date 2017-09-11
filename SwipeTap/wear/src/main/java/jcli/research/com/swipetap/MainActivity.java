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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


import java.util.Arrays;
import java.util.List;



public class MainActivity extends Activity{
    public static String TAG = "WatchActivity";

    //private static ArrayList<Integer> mContinuousIcons;
    private static List<String> mNames;
    private MainActivity mSelf;
    private ExpTask mNextTask;
    private TextView mTargetDisplayTextView;
    private GridViewPager mPager;
    private Button mStartButton;
    private WearableListView mListView;
    private TextView mTrialCountText;

    private static final String[] LETTER_OPTIONS = new String[] {"A", "B", "C", "D", "E"};
    private static final String[] NUMBER_OPTIONS = new String[] {"1", "2", "3", "4", "5"};
    private static final String[] SHAPE_OPTIONS = new String[] {"\u25a0", "\u25b2", "\u25cf", "\u25c6", "\u25ac"};

    private static final int TASK_RESULT = 1;

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

    public DataStorage storage;

    //data to record
    public int mCurrentTrial; // 0 - 89
    public int mAttemptTimes;
    public int mTrialState;  // 1- start, 2 - finger down 3- finger move 4 - finger up 5-trial end
    public int mTask;  //1 - 6
    public int mTaskType; // 1- discrete, 2 - continuous
    public int mClose;
    public float distancevaluetarget;
    public float distancevalueactual;
    public int isCorrect;
    public int isWrongTask;  // 1 - select a wrong task


    public int isOvershot = 0;  //didnt use this value
    public int mCorner = 0;  //disnt use
    public int anglevaluetarget = 0;  //didnt use
    public int anglevalueactual = -1;  //didnt use

    public long trialDuration;
    public long responseTime;
    public long menuTime;
    public long taskTime;
    public int fingerTouches;
    public long mTrialStartTime;
    public long mTrialFingerStartTime;
    public long mTrialEndTime;
    public long mNavigateToListTime;
    public long mTaskActivityStartTime;
    public long mListClickedTime;

    private int taskNum = 5;
    private float reservedDistance = 40;
    private float maxDistance = 120;
    private float targetInDecimal;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        mNames = Arrays.asList("Letter", "Number", "Icon", "Size", "Colour", "Weight");

        mPager = (GridViewPager) findViewById(R.id.pager);

        //---Assigns an adapter to provide the content for this pager---
        mPager.setAdapter(new ListInGridAdapter(this));
        mPager.setOnPageChangeListener(mGridPageChangeListener);
        mSelf = this;

        storage = DataStorage.getInstance();
        storage.clearData();

        fetchTaskAndRelocate(1);
    }

    private GridViewPager.OnPageChangeListener mGridPageChangeListener = new GridViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, int i1, float v, float v1, int i2, int i3) {
            //nothing
        }

        @Override
        public void onPageSelected(int row, int col) {
            if(col == 1) {
                long time = System.currentTimeMillis();
                //Log.d(TAG, "Page 1 selected at: " + time);
                mNavigateToListTime = time;
            }

        }

        @Override
        public void onPageScrollStateChanged(int i) {
            //nothing
        }
    };

    private boolean fetchTaskAndRelocate(int failureState) {
        //failure 0 -- success 1 -- wrong task result 2 -- wrong task type
        //If the previous trial is successful, get the next task, otherwise get the current one
        //Get the next task
        if(failureState == 0) {
            //record the result
            if(mTrialStartTime != 0 && mTrialFingerStartTime != 0 && mNavigateToListTime != 0)
            {
                mTrialEndTime = System.currentTimeMillis();
                trialDuration = mTrialEndTime - mTrialStartTime;
                responseTime = mTrialFingerStartTime - mTrialStartTime;
                menuTime = mTaskActivityStartTime - mNavigateToListTime;
                taskTime = mTrialEndTime - mTaskActivityStartTime;

                isCorrect = 1;
                isWrongTask = 0;

                DataStorage.AddSample(3, mCurrentTrial, mAttemptTimes, 5, mTrialStartTime,
                        mCorner, mTask, mTaskType, mClose, anglevaluetarget, distancevaluetarget,
                        anglevalueactual, distancevalueactual,
                        isCorrect, isWrongTask, isOvershot, trialDuration,
                        responseTime, menuTime, taskTime, fingerTouches);

            }

            //refresh
            mNextTask = TaskManager.getInstance().getNextTask();
            mCurrentTrial++;
            mAttemptTimes = 1;

        } else {
            //record with result
            //dont save the first trial first trial
            if(mTrialStartTime != 0 && mTrialFingerStartTime != 0 && mNavigateToListTime != 0)
            {
                mTrialEndTime = System.currentTimeMillis();
                trialDuration = mTrialEndTime - mTrialStartTime;
                responseTime = mTrialFingerStartTime - mTrialStartTime;
                menuTime = mTaskActivityStartTime - mNavigateToListTime;
                taskTime = mTrialEndTime - mTaskActivityStartTime;

                isCorrect = 0;
                isWrongTask = failureState == 1 ? 2 : 1;

                DataStorage.AddSample(3, mCurrentTrial, mAttemptTimes, 5, mTrialStartTime,
                        mCorner, mTask, mTaskType, mClose, anglevaluetarget, distancevaluetarget,
                        anglevalueactual, distancevalueactual,
                        isCorrect, isWrongTask, isOvershot, trialDuration,
                        responseTime, menuTime, taskTime, fingerTouches);

            }

            //refreshs
            mNextTask = TaskManager.getInstance().getCurrentTask();
            mAttemptTimes++;

        }

        //save the new trial
        if(mNextTask != null)
        {
            mTask = mNextTask.getTaskInd();
            mTrialStartTime = System.currentTimeMillis();
            mTrialFingerStartTime = 0;
            mNavigateToListTime = 0;
            mTaskActivityStartTime = 0;
            mTrialEndTime = 0;
            trialDuration = 0;
            responseTime = 0;
            fingerTouches = 0;
            isCorrect = 0;
            isWrongTask = 0;

            mTask = mNextTask.getTaskInd() + 1;
            mClose = mNextTask.getClose();
            mTaskType = mNextTask.getTaskType();

            targetInDecimal = mNextTask.getValue() / 100.0f;
            distancevaluetarget = mTask < 4 ? (int)(taskNum * targetInDecimal) : (maxDistance * targetInDecimal + reservedDistance);
            distancevalueactual = -1;

            //save the new trial
            DataStorage.AddSample(3, mCurrentTrial, mAttemptTimes, 1, mTrialStartTime,
                    mCorner, mTask, mTaskType, mClose, anglevaluetarget, distancevaluetarget,
                    anglevalueactual, distancevalueactual);

            //Go back to result screen and refresh
            mPager.setCurrentItem(0, 0);

            return false;
        } else
        {
            mPager.setCurrentItem(0, 0); //?
            //save the data
            storage.save2();
            return true;
        }
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        storage.save2();
    }


    // Handle our Wearable List's click events
    private WearableListView.ClickListener mListClickListener =
            new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {
                    long time = System.currentTimeMillis();
                    //Log.d(TAG, "Task list clicked at: " + time);
                    mListClickedTime = time;
                    int indClicked = viewHolder.getAdapterPosition();
                    //Check if the correct option is clicked
                    if(indClicked == mNextTask.getTaskInd()) {
                        if(mNextTask.isDiscrete()) {
                            //So it is a discrete task, start the discrete activity
                            Intent disIntent = new Intent(mSelf, ExpActivity.class);
                            disIntent.putExtra("task", mNextTask.getTaskInd());
                            disIntent.putExtra("target", mNextTask.getValue());
                            startActivityForResult(disIntent, TASK_RESULT);
                        } else {
                            //So it is a continuous task
                            Intent conIntent = new Intent(mSelf, ConExpActivity.class);
                            conIntent.putExtra("task", mNextTask.getTaskInd());
                            conIntent.putExtra("value", mNextTask.getValue());
                            startActivityForResult(conIntent, TASK_RESULT);
                        }
                    } else {
                        //Wrong option clicked, return to task display
                        //TODO: increase error count
                        // 2 for wrong task type
                        fetchTaskAndRelocate(2);
                    }

                }

                @Override
                public void onTopEmptyRegionClick() {
                }
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == TASK_RESULT) {
            mTaskActivityStartTime = data.getLongExtra("time", -1);
            //Log.d(TAG, "Task activity started at: " + mTaskActivityStartTime);
            if(fetchTaskAndRelocate(resultCode == RESULT_OK ? 0 : 1)) {
                //Study done, do something
            } else {
                renderTask();
                //push the option list to the start
                mListView.scrollToPosition(0);
            }
        }
    }

    private void renderDiscreteTask() {
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
    }

    private void renderContinuousTask() {
        //Is a continuous task, we should draw something then
        mConTargetView.setTask( mNextTask.getTaskInd() + 1, (int)mNextTask.getValue());
        mConTargetView.setVisibility(View.VISIBLE);
        mTargetDisplayTextView.setVisibility(View.INVISIBLE);
        mConTargetView.invalidate();

    }

    private void renderTask () {
        if(mNextTask.isDiscrete()) {
            renderDiscreteTask();
        } else {
            renderContinuousTask();
        }
        //Display the current trial count
        mTrialCountText.setText("" + (MainActivity.getSharedInstance().mCurrentTrial + 1) + " / 90");
    }


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
                    mTrialCountText = (TextView) centerView.findViewById(R.id.trial_count_text);

                    //if(!TaskManager.getInstance().isFirstTrial()) {
                    //    mStartButton.setVisibility(View.GONE);
                        //mTargetDisplayTextView.setText("" + MainActivity.getSharedInstance().mCurrentTrial + " / 90");
                    //    mTargetDisplayTextView.setVisibility(View.VISIBLE);
                    //}
                    
                    mStartButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mStartButton.setVisibility(View.GONE);
                            mTargetDisplayTextView.setVisibility(View.VISIBLE);
                        }
                    });

                    centerView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                //Log.d(TAG, "Touch on first page at:" + String.valueOf(System.currentTimeMillis()));
                                mTrialFingerStartTime = System.currentTimeMillis();
                            }
                            return false;
                        }
                    });
                    //Set the target display
                    renderTask();
                    viewGroup.addView(centerView);
                    return centerView;
                case 1:
                    View discreteList = inflater.inflate(R.layout.list_side, null);
                    WearableListView discreteWearableListView =
                                (WearableListView) discreteList.findViewById(R.id.wearable_List);
                    discreteWearableListView.setAdapter(new WearableAdapter(mContext, mNames));
                    discreteWearableListView.setClickListener(mListClickListener);
                    discreteWearableListView.setGreedyTouchMode(true);
                    mListView = discreteWearableListView;
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
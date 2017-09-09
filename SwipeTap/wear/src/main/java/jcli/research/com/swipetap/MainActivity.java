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
import android.view.View;
import android.view.ViewGroup;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private static ArrayList<Integer> mIcons;
    //private static ArrayList<Integer> mContinuousIcons;
    private static List<String> mNames;
    private MainActivity mSelf;
    private ExpTask mNextTask;
    //private static List<String> mContinuousNames;
    //private TextView mHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sample icons for the list
        mIcons = new ArrayList<Integer>();
        mIcons.add(R.drawable.ic_action_attach);
        mIcons.add(R.drawable.ic_action_call);
        mIcons.add(R.drawable.ic_action_locate);
        mIcons.add(R.drawable.ic_action_locate);
        mIcons.add(R.drawable.ic_action_locate);
        mIcons.add(R.drawable.ic_action_locate);

        mNames = Arrays.asList("Letter", "Number", "Shape", "Size", "Colour", "Weight");

        // This is our list header
        //mHeader = (TextView) findViewById(R.id.header);


        //wearableListView.addOnScrollListener(mOnScrollListener);

        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);

        //---Assigns an adapter to provide the content for this pager---
        pager.setAdapter(new ListInGridAdapter(this));
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
                    if(indClicked == mNames.indexOf(mNextTask.getType())) {
                        if(mNextTask.isDiscrete()) {
                            //So it is a discrete task, start the discrete activity
                            Intent disIntent = new Intent(mSelf, ExpActivity.class);
                            disIntent.putExtra("type", mNextTask.getType());
                            disIntent.putExtra("target", mNextTask.getTargetInd());
                            startActivity(disIntent);
                        } else {
                            //So it is a continuous task
                        }
                    } else {
                        //Wrong option clicked, return to task display
                    }

                }

                @Override
                public void onTopEmptyRegionClick() {
                }
            };



    public class ListInGridAdapter extends GridPagerAdapter {
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
                    View centerView = inflater.inflate(R.layout.view_center, viewGroup);
                    return centerView;
                case 1:
                    View discreteList = inflater.inflate(R.layout.list_side, null);
                    WearableListView discreteWearableListView =
                                (WearableListView) discreteList.findViewById(R.id.wearable_List);
                    discreteWearableListView.setAdapter(new WearableAdapter(mContext, mIcons, mNames));
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
package jcli.research.com.swipetap;

/**
 * Created by jchrisli on 2017-09-07.
 */

import android.app.Activity;
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
    private static ArrayList<Integer> mDiscreteIcons;
    private static ArrayList<Integer> mContinuousIcons;
    private static List<String> mDiscreteNames;
    private static List<String> mContinuousNames;
    //private TextView mHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sample icons for the list
        mDiscreteIcons = new ArrayList<Integer>();
        mDiscreteIcons.add(R.drawable.ic_action_attach);
        mDiscreteIcons.add(R.drawable.ic_action_call);
        mDiscreteIcons.add(R.drawable.ic_action_locate);

        mContinuousIcons = new ArrayList<Integer>();
        mContinuousIcons.add(R.drawable.ic_action_mail);
        mContinuousIcons.add(R.drawable.ic_action_microphone);
        mContinuousIcons.add(R.drawable.ic_action_photo);
        mDiscreteNames = Arrays.asList("Letter", "Number", "Shape");
        mContinuousNames = Arrays.asList("Font Size", "Font Weight", "Grayscale");

        // This is our list header
        //mHeader = (TextView) findViewById(R.id.header);


        //wearableListView.addOnScrollListener(mOnScrollListener);

        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);

        //---Assigns an adapter to provide the content for this pager---
        pager.setAdapter(new ListInGridAdapter(this));
        pager.setCurrentItem(0, 1);
    }

    // Handle our Wearable List's click events
    private WearableListView.ClickListener mDiscreteClickListener =
            new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {
                    switch (viewHolder.getAdapterPosition()) {
                        //TODO: initiate different tasks
                    }
                }

                @Override
                public void onTopEmptyRegionClick() {
                }
            };

    private WearableListView.ClickListener mContinuousClickListener =
            new WearableListView.ClickListener() {
                @Override
                public void onClick(WearableListView.ViewHolder viewHolder) {
                    switch (viewHolder.getAdapterPosition()) {
                        //TODO: initiate different tasks
                    }

                }

                @Override
                public void onTopEmptyRegionClick() {

                }
            };

    private WearableListView.OnScrollListener mOnScrollListener = new WearableListView.OnScrollListener() {

        @Override
        public void onScroll(int i) {

        }

        @Override
        public void onAbsoluteScrollChange(int i) {

        }

        @Override
        public void onScrollStateChanged(int i) {

        }

        @Override
        public void onCentralPositionChanged(int i) {

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
            return 3;
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
                    View discreteList = inflater.inflate(R.layout.list_side, null);
                    WearableListView discreteWearableListView =
                            (WearableListView) discreteList.findViewById(R.id.wearable_List);
                    discreteWearableListView.setAdapter(new WearableAdapter(mContext, mDiscreteIcons, mDiscreteNames));
                    discreteWearableListView.setClickListener(mDiscreteClickListener);
                    discreteWearableListView.addOnScrollListener(mOnScrollListener);
                    discreteWearableListView.setGreedyTouchMode(true);
                    viewGroup.addView(discreteList);
                    return discreteList;
                case 1:
                    View centerView = inflater.inflate(R.layout.view_center, viewGroup);
                    return centerView;
                case 2:
                    View continuousList = inflater.inflate(R.layout.list_side, null);
                    WearableListView continuousWearableListView =
                            (WearableListView) continuousList.findViewById(R.id.wearable_List);
                    continuousWearableListView.setAdapter(new WearableAdapter(mContext, mContinuousIcons, mContinuousNames));
                    continuousWearableListView.setClickListener(mContinuousClickListener);
                    continuousWearableListView.addOnScrollListener(mOnScrollListener);
                    continuousWearableListView.setGreedyTouchMode(true);
                    viewGroup.addView(continuousList);
                    return continuousList;
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
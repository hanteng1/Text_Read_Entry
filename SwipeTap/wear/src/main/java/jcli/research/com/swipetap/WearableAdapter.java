package jcli.research.com.swipetap;

/**
 * Created by jchrisli on 2017-09-07.
 */

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WearableAdapter extends WearableListView.Adapter {
    private ArrayList<Integer> mImageItems;
    private List<String> mTextItems;
    private final LayoutInflater mInflater;

    public WearableAdapter(Context context, ArrayList<Integer> items, List<String> textItems) {
        mInflater = LayoutInflater.from(context);
        mImageItems = items;
        mTextItems = textItems;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int i) {
        return new ItemViewHolder(mInflater.inflate(R.layout.list_item, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder,
                                 int position) {
        ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        CircledImageView circledView = itemViewHolder.mCircledImageView;
        circledView.setImageResource(mImageItems.get(position));
        TextView textView = itemViewHolder.mItemTextView;
        textView.setText(mTextItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mImageItems.size();
    }

    private static class ItemViewHolder extends WearableListView.ViewHolder {
        private CircledImageView mCircledImageView;
        private TextView mItemTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mCircledImageView = (CircledImageView)
                    itemView.findViewById(R.id.circle);
            mItemTextView = (TextView) itemView.findViewById(R.id.name);
        }
    }
}

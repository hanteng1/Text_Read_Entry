package jcli.research.com.swipetap;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;


/**
 * Created by jchrisli on 2017-09-09.
 */

public class ExpOptionListAdapter extends WearableListView.Adapter {
    private List<String> mTextItems;
    //private Context mContext;
    private final LayoutInflater mInflater;

    public ExpOptionListAdapter(Context context, List<String> textItems) {
        mTextItems = textItems;
        //mContext = context;
        mInflater = LayoutInflater.from(context);

    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int i) {
        return new ExpOptionItemViewHolder(mInflater.inflate(R.layout.item_list_option_exp, null));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder,
                                 int position) {
        ExpOptionItemViewHolder holder = (ExpOptionItemViewHolder) viewHolder;
        holder.setText(mTextItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mTextItems.size();
    }

    private static class ExpOptionItemViewHolder extends WearableListView.ViewHolder {
        private TextView mItemTextView;

        public ExpOptionItemViewHolder(View itemView) {
            super(itemView);
            mItemTextView = (TextView) itemView.findViewById(R.id.exp_option_name);
        }

        public void setText(String t) {
            mItemTextView.setText(t);
        }
    }

}

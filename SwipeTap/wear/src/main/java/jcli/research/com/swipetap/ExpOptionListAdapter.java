package jcli.research.com.swipetap;

import android.content.Context;
import android.support.wearable.view.WearableListView;
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
    private Context mContext;

    public ExpOptionListAdapter(Context context, List<String> textItems) {
        mTextItems = textItems;
        mContext = context;
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int i) {
        return new WearableListView.ViewHolder(new TextView(mContext));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder viewHolder,
                                 int position) {
        TextView textView = (TextView) viewHolder.itemView;
        textView.setText(mTextItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mTextItems.size();
    }

}

package jcli.research.com.swipetap;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.support.wearable.view.WearableListView;

/**
 * Created by jchrisli on 2017-09-09.
 */

public class ExpOptionListItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {


    private static final float NO_ALPHA = 1f, PARTIAL_ALPHA = 0.40f;

    public ExpOptionListItemLayout(Context context) {
        this(context, null);
    }

    public ExpOptionListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpOptionListItemLayout(Context context, AttributeSet attrs,
                                   int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public void onCenterPosition(boolean animate) {
        if (animate) {
            animate().alpha(NO_ALPHA).start();
        }
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        if (animate) {
            animate().alpha(PARTIAL_ALPHA).start();
        }
    }
}

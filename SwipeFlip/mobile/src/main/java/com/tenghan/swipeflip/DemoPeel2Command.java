package com.tenghan.swipeflip;

import android.content.Context;
import android.util.Log;

import com.eschao.android.widget.pageflip.modify.PageModify;

/**
 * Created by hanteng on 2017-08-18.
 */

public class DemoPeel2Command extends PageFlipModifyAbstract{

    private final static String TAG = "DemoPeel2Command";

    //first page as main content, second page as command
    private final static int pageSize = 2;


    public DemoPeel2Command(Context context)
    {
        super(context, pageSize);
    }



}

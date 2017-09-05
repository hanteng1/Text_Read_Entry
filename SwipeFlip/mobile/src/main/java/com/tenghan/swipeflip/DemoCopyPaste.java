package com.tenghan.swipeflip;

import android.content.Context;

/**
 * Created by hanteng on 2017-09-05.
 */

public class DemoCopyPaste extends PageFlipModifyAbstract{

    private final static String TAG = "DemoCopyPaste";

    private final static int pageSize = 2;

    private final static int FRONT_PAGE = 0;
    private final static int SECOND_PAGE = 1;

    public DemoCopyPaste(Context context)
    {
        super(context, pageSize);
    }



    public boolean onFingerMove(float touchX, float touchY)
    {

        return false;
    }

    public void computeVertexesAndBuildPage(){

    }

}

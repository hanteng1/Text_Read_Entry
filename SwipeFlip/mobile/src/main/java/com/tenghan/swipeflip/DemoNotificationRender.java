package com.tenghan.swipeflip;

import android.content.Context;
import android.os.Handler;

/**
 * Created by hanteng on 2017-09-01.
 */

public class DemoNotificationRender extends DemoRender{

    private final static String TAG = "DemoNotification";

    public DemoNotificationRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                                  Handler handler, int pageNo)
    {
        super(context, pageFlipAbstract, handler, pageNo);
    }

    //initial load
    public void LoadTextures(){

    }

    public void ReloadTexture(int itrp)
    {

    }

    public void loadPageWithCommands(int number, String[] commandIds)
    {

    }


}

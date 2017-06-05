package com.tenghan.swipeflip;

import android.content.Context;
import android.os.Handler;

/**
 * Created by hanteng on 2017-06-01.
 * Single page render without doing the second texture
 */

public class SinglePageNoSecTexRender extends PageRender {

    public SinglePageNoSecTexRender(Context context, PageFlipModify pageFlip,
                                    Handler handler, int pageNo) {
        super(context, pageFlip, handler, pageNo);
    }

    public void onDrawFrame() {

    }

    public void onSurfaceChanged(int width, int height) {


    }

    public boolean onEndedDrawing(int what) {
        return false;
    }

    private void drawPage(int number) {

    }

    public boolean canFlipForward()
    {
        return false;
    }

    public boolean canFlipBackward()
    {
        return false;
    }
}

package com.tenghan.swipeflip;

import android.content.Context;
import android.os.Handler;

import com.eschao.android.widget.pageflip.PageFlip;

/**
 * Created by hanteng on 2017-06-01.
 * Single page render without doing the second texture
 */

public class SinglePageNoSecTexRender extends PageRender {

    public SinglePageNoSecTexRender(Context context, PageFlip pageFlip,
                                    Handler handler, int pageNo) {
        super(context, pageFlip, handler, pageNo);
    }

    public void onDrawFrame() {

    }

    public void onSurfaceChanged(int width, int height) {


    }

    public boolean onEndedDrawing(int what) {

    }

    private void drawPage(int number) {

    }

    public boolean canFlipForward()
    {

    }

    public boolean canFlipBackward()
    {

    }
}

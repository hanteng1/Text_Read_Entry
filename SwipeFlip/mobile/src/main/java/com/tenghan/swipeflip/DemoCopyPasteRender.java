package com.tenghan.swipeflip;

import android.content.Context;
import android.os.Handler;

import com.eschao.android.widget.pageflip.modify.PageModify;

/**
 * Created by hanteng on 2017-09-05.
 */

public class DemoCopyPasteRender extends DemoRender {

    private final static String TAG = "DemoCopyPaste";

    public DemoCopyPasteRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                               Handler handler, int pageNo)
    {
        super(context, pageFlipAbstract, handler, pageNo);
    }

    //initial load
    public void LoadTextures()
    {
        mPageFlipAbstract.deleteUnusedTextures();
        PageModify[] pages = mPageFlipAbstract.getPages();

        //set first pages
        if(!pages[0].isFrontTextureSet())
        {
            loadPage(0);
            pages[0].setFrontTexture(mBitmap);
        }

        //set the rest to blank
        for(int itrp = 1; itrp < pages.length; itrp++)
        {
            if(pages[itrp].isFrontTextureSet())
            {
                loadBlankPage();
                pages[itrp].setFrontTexture(mBitmap);
            }
        }

    }

    public void ReloadTexture(int itrp)
    {

    }

    public void loadPageWithFacebook(int fbstate)
    {

    }

    public void loadPageWithCommands(int number, String[] commandIds)
    {

    }




}

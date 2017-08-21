package com.tenghan.swipeflip;

import android.content.Context;
import android.os.Handler;

import com.eschao.android.widget.pageflip.modify.PageModify;

/**
 * Created by hanteng on 2017-08-19.
 *
 * To customize the render for the peel2command demo
 * First page - content
 * Second page - command
 */

public class DemoPeel2CommandRender extends DemoRender{

    public DemoPeel2CommandRender(Context context, PageFlipModifyAbstract pageFlipAbstract,
                                  Handler handler, int pageNo)
    {
        super(context, pageFlipAbstract, handler, pageNo);
    }

    public void LoadTextures(){
        mPageFlipAbstract.deleteUnusedTextures();
        PageModify[] pages = mPageFlipAbstract.getPages();

        //set the first page
        if(!pages[0].isFrontTextureSet())
        {
            loadPage(0);
            pages[0].setFrontTexture(mBitmap);
        }

        //set the rest
        for(int itrp = 1; itrp < mPageFlipAbstract.PAGE_SIZE; itrp++)
        {
            if(!pages[itrp].isFrontTextureSet())
            {
                //commands ids
                loadPageWithCommands(itrp, cRIds[itrp]);
                pages[itrp].setFrontTexture(mBitmap);

                loadPage(itrp + 5);
                pages[itrp].setBackTexture(mBitmap);
            }
        }
    }


    public void ReloadTexture(int itrp)
    {
        PageModify page = mPageFlipAbstract.getPages()[itrp];
        page.waiting4TextureUpdate = true;

        //page.deleteAllTextures();
        //loadPageWithCommands(itrp, cRIds[itrp]);
        //page.updateFrontTexture(mBitmap);
        //page.setBackTexture(mBitmap);
        //page.setFirstTextureWithSecond();

    }
}

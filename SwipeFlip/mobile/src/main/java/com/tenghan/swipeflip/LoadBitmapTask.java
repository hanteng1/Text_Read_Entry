package com.tenghan.swipeflip;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.util.LinkedList;
import java.util.Random;

/**
 * Created by hanteng on 2017-05-30.
 */

public final class LoadBitmapTask implements Runnable {

    private final static String TAG = "LoadBitmapTask";
    private static LoadBitmapTask __object;

    final static int SMALL_BG = 0;
    final static int MEDIUM_BG = 1;
    final static int LARGE_BG = 2;


    final static int BG_COUNT = 3;

    int mBGSizeIndex;
    int mQueueMaxSize;
    int mPreRandomNo;
    boolean mIsLandscape;
    boolean mStop;
    Random mBGRandom;
    Resources mResources;
    Thread mThread;
    LinkedList<Bitmap> mQueue;
    int[][] mPortraitBGs;
    int mWatchBG;


    //for notification demo
    int mFacebookBgEmpty;
    int mFacebookBgFull;
    int mFacebookGlobal;
    int mFacebookMsg;
    int mFacebookCont;

    //for copy demo
    int mPhoto;


    public static LoadBitmapTask get(Context context) {
        if (__object == null) {
            __object = new LoadBitmapTask(context);
        }
        return __object;
    }

    private LoadBitmapTask(Context context) {
        mResources = context.getResources();
        mBGRandom = new Random();
        mBGSizeIndex = SMALL_BG;
        mStop = false;
        mThread = null;
        mPreRandomNo = 0;
        mIsLandscape = false;
        mQueueMaxSize = BG_COUNT;
        mQueue = new LinkedList<Bitmap>();

        // init all available bitmaps
        mPortraitBGs = new int[][] {};

        mWatchBG = R.drawable.p2_320;

        //for notification demo
        mFacebookBgEmpty = R.drawable.fb_bg_empty;
        mFacebookBgFull = R.drawable.fb_bg_full;
        mFacebookGlobal = R.drawable.fb_global;
        mFacebookMsg = R.drawable.fb_msg;
        mFacebookCont = R.drawable.fb_cont;

        //for copy demo
        mPhoto = R.drawable.copy_origin;

    }


    public Bitmap getBitmap() {
        Bitmap b = null;
        synchronized (this) {
            if (mQueue.size() > 0) {
                b = mQueue.pop();  //grab a texture from the queue
            }
            notify();  //awaking the thread?
        }

        if (b == null) {
            Log.d(TAG, "Load bitmap instantly!");
            //b = getRandomBitmap();
            b = getWatchBitmap();
        }

        return b;
    }


    //for notification
    public Bitmap getFacebook(int tindex)
    {
        Bitmap b = null;

        if(tindex == 1)
        {
            b = BitmapFactory.decodeResource(mResources, mFacebookMsg);
        }else if(tindex == 2)
        {
            b = BitmapFactory.decodeResource(mResources, mFacebookCont);
        }else if(tindex == 3)
        {
            b = BitmapFactory.decodeResource(mResources, mFacebookGlobal);
        }

        return b;
    }

    //for copy paste
    public Bitmap getPhoto()
    {
        Bitmap b = null;
        b = BitmapFactory.decodeResource(mResources, mPhoto);
        return b;
    }

    public boolean isRunning() {
        return mThread != null && mThread.isAlive();
    }

    /**
     * Start task
     */
    public synchronized void start() {
        if (mThread == null || !mThread.isAlive()) {
            mStop = false;
            mThread = new Thread(this);
            mThread.start();
        }
    }

    public void stop() {
        synchronized (this) {
            mStop = true;
            notify();
        }

        // wait for thread stopping
        for (int i = 0; i < 3 && mThread.isAlive(); ++i) {
            Log.d(TAG, "Waiting thread to stop ...");
            try {
                Thread.sleep(500);
            }
            catch (InterruptedException e) {

            }
        }

        if (mThread.isAlive()) {
            Log.d(TAG, "Thread is still alive after waited 1.5s!");
        }
    }

    public void set(int w, int h, int maxCached) {
        int newIndex = SMALL_BG;
        if ((w <= 480 && h <= 854) ||
                (w <= 854 && h <= 480)) {
            mBGSizeIndex = SMALL_BG;
        }
        else if ((w <= 800 && h <= 1280) ||
                (h <= 800 && w <= 1280)) {
            mBGSizeIndex = SMALL_BG;
        }

        mIsLandscape = w > h;

        if (maxCached != mQueueMaxSize) {
            mQueueMaxSize = maxCached;
        }

        if (newIndex != mBGSizeIndex) {
            mBGSizeIndex = newIndex;
            synchronized (this) {
                cleanQueue();
                notify();
            }
        }
    }

    private Bitmap getRandomBitmap() {
        int newNo = mPreRandomNo;
        while (newNo == mPreRandomNo) {
            newNo = mBGRandom.nextInt(BG_COUNT);
        }

        mPreRandomNo = newNo;
        int resId = mPortraitBGs[mBGSizeIndex][newNo];

        Bitmap b = BitmapFactory.decodeResource(mResources, resId);
        /*
        if (mIsLandscape) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap lb = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(),
                    matrix, true);
            b.recycle();
            return lb;
        }*/

        return b;
    }

    private Bitmap getWatchBitmap(){
        int resId = mWatchBG;
        Bitmap b = BitmapFactory.decodeResource(mResources, resId);
        return b;
    }

    private void cleanQueue() {
        for (int i = 0; i < mQueue.size(); ++i) {
            mQueue.get(i).recycle();
        }
        mQueue.clear();
    }

    public void run() {
        while (true) {
            synchronized (this) {
                // check if ask thread stopping
                if (mStop) {
                    cleanQueue();
                    break;
                }

                // load bitmap only when no cached bitmap in queue
                int size = mQueue.size();
                if (size < 1) {
                    for (int i = 0; i < mQueueMaxSize; ++i) {
                        Log.d(TAG, "Load Queue:" + i + " in background!");
                        mQueue.push(getWatchBitmap());  //prepare the textures in a queue
                    }
                }

                // wait to be awaken
                try {
                    wait();
                }
                catch (InterruptedException e) {
                }
            }
        }
    }
}

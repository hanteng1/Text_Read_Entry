package com.tenghan.swipeflip;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.eschao.android.widget.pageflip.GLPoint;

import java.util.ArrayList;

/**
 * Created by hanteng on 2017-08-19.
 */

public class GestureService {

    private final static String TAG = "GestureService";
    private ArrayList<float[]> poses;
    private static int SIZE_LIMIT = 5;  //use 5 sequential points

    private GLPoint origin;
    private boolean originSet;

    /**
     * 1 has to follow 0
     * 2 has to follow 1
     * 3 and 4 have to follow 2
     *
     * 0 - nothing
     * 1 - start to move
     * 2 - hold
     * 3 - continue to move
     * 4 - move back
     */
    public int gestureState = 0;

    private float holdDistance;
    public float curDistance;



    /**
     *    ---------
     *   |0       1|
     *   |         |
     *   |         |
     *   |3       2|
     *    ---------
     */
    public int activiatedCommandIndex = -1;

    //this can be treated as the minimum activation distance
    private static double PEEL_ACTIVIATION_DISTANCE = 100;


    public GestureService()
    {
        //Log.d(TAG, "initial called");
        poses = new ArrayList<float[]>();
        origin = new GLPoint();
        originSet = false;
    }

    public void handleData(float[] pos) {
        // Gets data from the incoming Intent
        //if its the first data
        if(poses.size() == 0 && gestureState == 0)
        {
            gestureState = 1;
        }

        //Log.d(TAG, " " + pos[0] + ", " + pos[1]);
        poses.add(pos);
        if(poses.size() > SIZE_LIMIT)
        {
            poses.remove(0);
        }

        if(gestureState == 1 || gestureState == 2)
        {
            gestureRecognition(poses);
        }else if(gestureState == 3)
        {
            //find out which command
            if(activiatedCommandIndex == -1) {
                activiatedCommandIndex = calPeelCommand(pos);

                if(activiatedCommandIndex > -1) {
                    Log.d(TAG, "command activiated " + activiatedCommandIndex);
                    //reload texture

                    //the page behind of current lock page
                    if(MainActivity.getSharedInstance().activityIndex == 2)
                    {
                        int comamndPage = MainActivity.getSharedInstance().mDemoView.mDemo.currentPageLock + 1;
                        MainActivity.getSharedInstance().mDemoView.mPageRender.ReloadTexture(comamndPage);
                    }else if(MainActivity.getSharedInstance().activityIndex == 3)
                    {
                        int comamndPage = MainActivity.getSharedInstance().mStudyView.mStudy.currentPageLock + 1;
                        //MainActivity.getSharedInstance().mStudyView.mPageRender.ReloadTexture(comamndPage);
                    }


                }
            }

//
//            //for font zoom in
//            if(MainActivity.getSharedInstance().mDemoView.mDemo.currentPageLock == 0
//                    && activiatedCommandIndex == 3)
//            {
//                curDistance = calPeelDistance(pos);
//                MainActivity.getSharedInstance().mDemoView.mPageRender.ReloadTexture(1);
//            }

        }

    }

    private void gestureRecognition(ArrayList<float[]> posXY)
    {
        int recognizedGesture = 0;
        if(posXY.size() < SIZE_LIMIT)
            return;

        //calculate the average distance
        double disSequentialPoint = 0;
        float averageDis = 0;
        for(int itrp = 0; itrp < posXY.size() - 1; itrp++)
        {
            disSequentialPoint = Math.sqrt((double) ((posXY.get(itrp)[0] - posXY.get(itrp+1)[0]) * (posXY.get(itrp)[0] - posXY.get(itrp+1)[0])
                    + (posXY.get(itrp)[1] - posXY.get(itrp+1)[1]) * (posXY.get(itrp)[1] - posXY.get(itrp+1)[1])));

            averageDis += disSequentialPoint;
        }

        averageDis = averageDis / (posXY.size() - 1);

        //Log.d(TAG, "averageDis " + averageDis);

        if(gestureState == 1)
        {
            //looking for the hold
            if(averageDis < 3)
            {
                gestureState = 2;  //hold
                float[] lastpos = posXY.get(posXY.size() - 1);
                holdDistance = calPeelDistance(lastpos);
            }
        }else if(gestureState == 2)
        {
            //looking for flip forward or backward
            if(averageDis > 3)
            {
                float[] lastpos = posXY.get(posXY.size() - 1);
                curDistance = calPeelDistance(lastpos);
                if(curDistance > holdDistance)
                {
                    gestureState = 3;

                }else if(curDistance < holdDistance)
                {
                    gestureState = 4;
                }
            }
        }

    }

    private float calPeelDistance(float[] pos)
    {
        if(originSet == false)
            return -1;

        float distance = (float)Math.sqrt((double)((pos[0] - origin.x) * (pos[0] - origin.x)
                + (pos[1] - origin.y) * (pos[1] - origin.y)));

        //Log.d(TAG, "pos " + pos[0] + " , " + pos[1]);
        return distance;


    }

    private int calPeelCommand(float[] pos)
    {
        if(originSet == false)
            return -1;

        float distance = (float)Math.sqrt((double)((pos[0] - origin.x) * (pos[0] - origin.x)
                + (pos[1] - origin.y) * (pos[1] - origin.y)));

        if(distance > PEEL_ACTIVIATION_DISTANCE)
        {
            if(origin.x < 0  && origin.y > 0)
            {
                return 0;
            }else if(origin.x > 0 && origin.y > 0)
            {
                return 1;
            }else if(origin.x > 0 && origin.y < 0)
            {
                return 2;
            }else{
                return 3;
            }
        }

        return -1;

    }

    public void setOrigin(float[] ori)
    {
        origin.x = ori[0];
        origin.y = ori[1];

        //Log.d(TAG, "origin " + origin.x + ", " + origin.y);

        originSet = true;
    }

    public void reset()
    {
        activiatedCommandIndex = -1;
        gestureState = 0;
        originSet = false;
        poses.clear();
    }
}

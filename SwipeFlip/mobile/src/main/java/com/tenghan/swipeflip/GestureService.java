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
     *    ---------
     *   |0       1|
     *   |         |
     *   |         |
     *   |3       2|
     *    ---------
     */
    public int activiatedCommandIndex = -1;
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

        //Log.d(TAG, " " + pos[0] + ", " + pos[1]);
        poses.add(pos);
        if(poses.size() > SIZE_LIMIT)
        {
            poses.remove(0);
        }

        if(activiatedCommandIndex == -1) {
            activiatedCommandIndex = calPeelDistance(pos);

            if(activiatedCommandIndex > -1) {
                Log.d(TAG, "command activiated " + activiatedCommandIndex);
                //reload texture
                MainActivity.getSharedInstance().mDemoView.mPageRender.ReloadTexture(1);
            }
        }

        gestureRecognition(poses);
    }

    private int gestureRecognition(ArrayList<float[]> posXY)
    {
        int recognizedGesture = 0;
        if(posXY.size() < SIZE_LIMIT)
            return recognizedGesture;

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

        return 0;
    }

    private int calPeelDistance(float[] pos)
    {
        if(originSet == false)
            return -1;

        double distance = Math.sqrt((double)((pos[0] - origin.x) * (pos[0] - origin.x)
                + (pos[1] - origin.y) * (pos[1] - origin.y)));

        //Log.d(TAG, "pos " + pos[0] + " , " + pos[1]);

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

    public void clear()
    {
        activiatedCommandIndex = -1;
        originSet = false;
        poses.clear();
    }
}

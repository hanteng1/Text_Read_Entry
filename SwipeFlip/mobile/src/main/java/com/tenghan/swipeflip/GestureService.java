package com.tenghan.swipeflip;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by hanteng on 2017-08-19.
 */

public class GestureService {

    private final static String TAG = "GestureService";
    private ArrayList<float[]> poses;
    private int SIZE_LIMIT = 5;  //use 5 sequential points

    public GestureService()
    {
        //Log.d(TAG, "initial called");
        poses = new ArrayList<float[]>();
    }

    public void handleData(float[] pos) {
        // Gets data from the incoming Intent

        //Log.d(TAG, " " + pos[0] + ", " + pos[1]);
        poses.add(pos);
        if(poses.size() > SIZE_LIMIT)
        {
            poses.remove(0);
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

        Log.d(TAG, "averageDis " + averageDis);

        return 0;
    }

    public void clear()
    {
        poses.clear();
    }
}

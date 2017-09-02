package com.tenghan.swipeflip;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by hanteng on 2017-08-30.
 */

public class DataSample {

    public int trial;
    public int trialAttempt;
    public int cornerIndex;
    public int angleNum;
    public int distanceNum;

    public int close;

    public int angleTarget;
    public int distanceTarget;
    public int angleActual;
    public int distanceActual;

    //to indicate 1 - start, 2 - move, 3 - end
    public int state;
    public long timestamp;


    //two result
    public long trialDuration;
    public int isCorrect;  // 1 - correct, 0 - incorrect
    public int numVistedCells;
    public int numOvershoot;


    public DataSample(int _trial, int _trialAttempt, int _corner, int _anglenum, int _distancenum, int _close,
                      int _angletarget, int _distancetarget, int _angleactual, int _distanceactual, int _state,
                      long _timestamp)
    {
        trial = _trial;
        trialAttempt = _trialAttempt;
        cornerIndex = _corner;
        angleNum = _anglenum;
        distanceNum = _distancenum;
        close = _close;
        angleTarget = _angletarget;
        distanceTarget = _distancetarget;
        angleActual = _angleactual;
        distanceActual = _distanceactual;
        state = _state;
        timestamp = _timestamp;
    }

    //version two
    //only for the trial end
    public DataSample(int _trial, int _trialAttempt, int _corner, int _anglenum, int _distancenum, int _close,
                      int _angletarget, int _distancetarget, int _angleactual, int _distanceactual, int _state,
                      long _timestamp,
                      int _isCorrect, int _numVistedCells, int _numOvershoot, long _trialDuration)
    {
        trial = _trial;
        trialAttempt = _trialAttempt;
        cornerIndex = _corner;
        angleNum = _anglenum;
        distanceNum = _distancenum;
        close = _close;
        angleTarget = _angletarget;
        distanceTarget = _distancetarget;
        angleActual = _angleactual;
        distanceActual = _distanceactual;
        state = _state;
        timestamp = _timestamp;

        isCorrect = _isCorrect;
        numVistedCells = _numVistedCells;
        numOvershoot = _numOvershoot;
        trialDuration = _trialDuration;
    }



    public static String toCSV(ArrayList<DataSample> arrayList)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for(Iterator<DataSample> iterator = arrayList.iterator(); iterator.hasNext();)
        {
            DataSample sample = iterator.next();

            if(sample.state == 3)
            {
                stringbuilder.append("" + sample.trial + "," + sample.trialAttempt + ","
                        + sample.cornerIndex + "," + sample.angleNum + "," + sample.distanceNum + ","
                        + sample.close + ","
                        + sample.angleTarget + "," + sample.distanceTarget + ","
                        + sample.angleActual + "," + sample.distanceActual + ","
                        + sample.state + ","
                        + sample.timestamp + ","
                        + sample.isCorrect + ","
                        + sample.numVistedCells + ","
                        + sample.numOvershoot + ","
                        + sample.trialDuration + ","
                        + "\r\n");
            }else
            {
                stringbuilder.append("" + sample.trial + "," + sample.trialAttempt + ","
                        + sample.cornerIndex + "," + sample.angleNum + "," + sample.distanceNum + ","
                        + sample.close + ","
                        + sample.angleTarget + "," + sample.distanceTarget + ","
                        + sample.angleActual + "," + sample.distanceActual + ","
                        + sample.state + ","
                        + sample.timestamp + "," + "\r\n");
            }

        }

        return stringbuilder.toString();
    }



}

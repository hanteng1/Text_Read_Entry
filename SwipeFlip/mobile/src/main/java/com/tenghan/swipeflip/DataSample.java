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


    //for study 2, discrete tasks

    //1 - flip, 2 - marking menu, 3- swipe tap
    public int technique;
    public int task;
    public int taskType;

    public float distancevaluetarget;
    public float distancevalueactual;

    //state
    // 1 - trial start
    // 2 - finger touch
    // 3 - finger move
    // 4 - finger up
    // 5 - trial end



    public DataSample(int _technique, int _trial, int _trialattemp, int _state, long _timestamp,
                      int _corner, int _task, int _tasktype, int _close,
                      int _angletarget, float _distancetarget, int  _angleactual, float _distanceActual)
    {
        technique = _technique;
        trial = _trial;
        trialAttempt = _trialattemp;
        state = _state;
        timestamp = _timestamp;
        cornerIndex = _corner;
        task = _task;
        taskType = _tasktype;
        close = _close;

        //this could be either discrete task, or continuous task
        angleTarget = _angletarget;
        distancevaluetarget = _distancetarget;
        angleActual = _angleactual;
        distancevalueactual = _distanceActual;

    }


    public long responseTime;
    public int fingerTouchTimes;

    //response time - from trial start to finger start
    //finger touch time - how many times users taps, times of state 2 - finger touch
    //trial duration should be only from first touch to the last finger up
    public DataSample(int _technique, int _trial, int _trialattemp, int _state, long _timestamp,
                      int _corner, int _task, int _tasktype, int _close,
                      int _angletarget, float _distancetarget, int _angleactual, float _distanceActual,
                      int _isCorrect, int _isWrongTask, int _isOvershoot, long _trialDuration,
                      long _responsetime, int _fingertouchtime)
    {
        technique = _technique;
        trial = _trial;
        trialAttempt = _trialattemp;
        state = _state;
        timestamp = _timestamp;
        cornerIndex = _corner;
        task = _task;
        taskType = _tasktype;
        close = _close;

        //this could be either discrete task, or continuous task
        angleTarget = _angletarget;
        distancevaluetarget = _distancetarget;
        angleActual = _angleactual;
        distancevalueactual = _distanceActual;

        isCorrect = _isCorrect;

        //is wrong corner, start by a wrong target
        //1 - start wrong, 2 middle wrong, 3 - start and middle wrong
        numVistedCells = _isWrongTask;
        //is over shot.. if target is correct, but has overshot
        numOvershoot = _isOvershoot;

        trialDuration = _trialDuration;

        responseTime = _responsetime;
        fingerTouchTimes = _fingertouchtime;

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


    //state
    // 1 - trial start
    // 2 - finger touch
    // 3 - finger move
    // 4 - finger up
    // 5 - trial end

    // 6 - wrong corner

    //for study 2
    public static String toCSV2(ArrayList<DataSample> arrayList)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for(Iterator<DataSample> iterator = arrayList.iterator(); iterator.hasNext();)
        {
            DataSample sample = iterator.next();

            if(sample.state == 5)
            {
                stringbuilder.append("" + sample.technique + "," + sample.trial + "," + sample.trialAttempt + "," +
                        sample.state + "," + sample.timestamp  + "," +
                        sample.cornerIndex + "," + sample.task + "," + sample.taskType  + "," +sample.close + "," +
                        sample.angleTarget + "," + sample.distancevaluetarget + "," + sample.angleActual + "," + sample.distancevalueactual + "," +
                        sample.isCorrect + "," + sample.numVistedCells  + "," + sample.numOvershoot  + "," +
                        sample.trialDuration  + "," + sample.responseTime + "," + sample.fingerTouchTimes + "," +
                        "\r\n");
            }else
            {
                stringbuilder.append("" + sample.technique + "," + sample.trial + "," + sample.trialAttempt + "," +
                        sample.state + "," + sample.timestamp  + "," +
                        sample.cornerIndex + "," + sample.task + "," + sample.taskType  + "," +sample.close + "," +
                        sample.angleTarget + "," + sample.distancevaluetarget + "," + sample.angleActual + "," + sample.distancevalueactual + "," +
                        "\r\n");
            }

        }

        return stringbuilder.toString();
    }


}

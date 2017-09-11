package jcli.research.com.swipetap;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by hanteng on 2017-09-11.
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
        //1 - start wrong, 2 middle wrong, 0 - correct
        numVistedCells = _isWrongTask;
        //is over shot.. if target is correct, but has overshot
        numOvershoot = _isOvershoot;

        trialDuration = _trialDuration;

        responseTime = _responsetime;
        fingerTouchTimes = _fingertouchtime;

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
                        sample.angleTarget + "," + String.format("%.2f", sample.distancevaluetarget) + "," + sample.angleActual + "," + String.format("%.2f", sample.distancevalueactual) + "," +
                        sample.isCorrect + "," + sample.numVistedCells  + "," + sample.numOvershoot  + "," +
                        sample.trialDuration  + "," + sample.responseTime + "," + sample.fingerTouchTimes + "," +
                        "\r\n");
            }else
            {
                stringbuilder.append("" + sample.technique + "," + sample.trial + "," + sample.trialAttempt + "," +
                        sample.state + "," + sample.timestamp  + "," +
                        sample.cornerIndex + "," + sample.task + "," + sample.taskType  + "," +sample.close + "," +
                        sample.angleTarget + "," + String.format("%.2f", sample.distancevaluetarget) + "," + sample.angleActual + "," + String.format("%.2f", sample.distancevalueactual)+ "," +
                        "\r\n");
            }

        }

        return stringbuilder.toString();
    }
}

package com.tenghan.swipeflip;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by hanteng on 2017-08-30.
 */

public class DataStorage {

    public DataStorage()
    {
        samples = new ArrayList<DataSample>(100000);
    }

    public static boolean AddSample(int _trial, int _trialAttempt, int _corner, int _anglenum, int _distancenum, int _close,
                                    int _angletarget, int _distancetarget, int _angleactual, int _distanceactual,
                                    int _state,
                                    long _timestamp)
    {
        if(instance != null)
        {
            instance.add(_trial, _trialAttempt,  _corner,  _anglenum,  _distancenum, _close,
             _angletarget,  _distancetarget,  _angleactual,  _distanceactual, _state,
             _timestamp);
            return true;
        }

        return false;
    }

    public void add(int _trial, int _trialAttempt, int _corner, int _anglenum, int _distancenum, int _close,
                    int _angletarget, int _distancetarget, int _angleactual, int _distanceactual,
                    int _state,
                    long _timestamp)
    {
        if(samples != null)
        {
            DataSample sample = new DataSample(_trial, _trialAttempt, _corner,  _anglenum,  _distancenum, _close,
                    _angletarget,  _distancetarget,  _angleactual,  _distanceactual, _state,
                    _timestamp);
            samples.add(sample);
        }
    }



    //version two
    public static boolean AddSample(int _trial, int _trialAttempt, int _corner, int _anglenum, int _distancenum, int _close,
                                    int _angletarget, int _distancetarget, int _angleactual, int _distanceactual,
                                    int _state,
                                    long _timestamp,
                                    int _isCorrect, int _numVistedCells, int _numOvershoot, long _trialDuration)
    {
        if(instance != null)
        {
            instance.add(_trial, _trialAttempt,  _corner,  _anglenum,  _distancenum, _close,
                    _angletarget,  _distancetarget,  _angleactual,  _distanceactual, _state,
                    _timestamp,
                    _isCorrect, _numVistedCells, _numOvershoot, _trialDuration);
            return true;
        }

        return false;
    }

    public void add(int _trial, int _trialAttempt, int _corner, int _anglenum, int _distancenum, int _close,
                    int _angletarget, int _distancetarget, int _angleactual, int _distanceactual,
                    int _state,
                    long _timestamp,
                    int _isCorrect, int _numVistedCells, int _numOvershoot, long _trialDuration)
    {
        if(samples != null)
        {
            DataSample sample = new DataSample(_trial, _trialAttempt, _corner,  _anglenum,  _distancenum, _close,
                    _angletarget,  _distancetarget,  _angleactual,  _distanceactual, _state,
                    _timestamp,
                    _isCorrect, _numVistedCells, _numOvershoot, _trialDuration);
            samples.add(sample);
        }
    }


    //for study 2
    public static boolean AddSample(int _technique, int _trial, int _trialattemp, int _state, long _timestamp,
                                    int _corner, int _task, int _tasktype, int _close,
                                    int _angletarget, float _distancetarget, int  _angleactual, float _distanceActual)
    {
        if(instance != null)
        {
            instance.add( _technique,  _trial,  _trialattemp,  _state,  _timestamp,
             _corner,  _task,  _tasktype,  _close,
             _angletarget,  _distancetarget,   _angleactual,  _distanceActual);
            return true;
        }

        return false;
    }

    public void add(int _technique, int _trial, int _trialattemp, int _state, long _timestamp,
                    int _corner, int _task, int _tasktype, int _close,
                    int _angletarget, float _distancetarget, int  _angleactual, float _distanceActual)
    {
        if(samples != null)
        {
            DataSample sample = new DataSample(_technique,  _trial,  _trialattemp,  _state,  _timestamp,
                    _corner,  _task,  _tasktype,  _close,
                    _angletarget,  _distancetarget,   _angleactual,  _distanceActual);
            samples.add(sample);
        }
    }



    public static boolean AddSample(int _technique, int _trial, int _trialattemp, int _state, long _timestamp,
                                    int _corner, int _task, int _tasktype, int _close,
                                    int _angletarget, float _distancetarget, int _angleactual, float _distanceActual,
                                    int _isCorrect, int _isWrongTask, int _numOvershoot, long _trialDuration,
                                    long _responsetime, int _fingertouchtime)
    {
        if(instance != null)
        {
            instance.add( _technique,  _trial,  _trialattemp,  _state,  _timestamp,
             _corner,  _task,  _tasktype,  _close,
             _angletarget,  _distancetarget,  _angleactual,  _distanceActual,
             _isCorrect,  _isWrongTask,  _numOvershoot,  _trialDuration,
             _responsetime,  _fingertouchtime);
            return true;
        }

        return false;
    }

    public void add(int _technique, int _trial, int _trialattemp, int _state, long _timestamp,
                    int _corner, int _task, int _tasktype, int _close,
                    int _angletarget, float _distancetarget, int _angleactual, float _distanceActual,
                    int _isCorrect, int _isWrongTask, int _numOvershoot, long _trialDuration,
                    long _responsetime, int _fingertouchtime)
    {
        if(samples != null)
        {
            DataSample sample = new DataSample(_technique,  _trial,  _trialattemp,  _state,  _timestamp,
                    _corner,  _task,  _tasktype,  _close,
                    _angletarget,  _distancetarget,  _angleactual,  _distanceActual,
                    _isCorrect,  _isWrongTask,  _numOvershoot,  _trialDuration,
                    _responsetime,  _fingertouchtime);
            samples.add(sample);
        }
    }





    public static DataStorage getInstance()
    {
        if(instance == null)
        {
            instance = new DataStorage();
        }
        return instance;
    }

    public void clearData()
    {
        if(samples!= null)
        {
            samples.clear();
        }
    }


    public String save(){
        return save(null);
    }

    public String save(String surfix)
    {
        if(samples == null || samples.size() == 0)
        {
            return "";
        }

        if(surfix == null)
        {
            surfix = "LogData";
        }
        if(!surfix.startsWith("_"))
        {
            surfix = "_" + surfix;
        }

        File dir;
        dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FlipPage/");

        String time = String.valueOf(System.currentTimeMillis());
        String filename = time + surfix + "_samples.csv";

        File file = new File(dir, filename);

        if(!dir.exists())
        {
            dir.mkdir();
        }

        try
        {
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter( new FileOutputStream(file, true));

            outputstreamwriter.write( DataSample.toCSV(samples) );
            outputstreamwriter.close();
            Log.i("DataStorage", "write samples completes.");

        } catch (IOException e)
        {
            e.printStackTrace();
            Log.i("DataStorage", e.toString());
        }

        return surfix;
    }


    //for study 2
    public String save2(){
        return save2(null);
    }

    public String save2(String surfix)
    {
        if(samples == null || samples.size() == 0)
        {
            return "";
        }

        if(surfix == null)
        {
            surfix = "LogData";
        }
        if(!surfix.startsWith("_"))
        {
            surfix = "_" + surfix;
        }

        File dir;
        dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/FlipPage/");

        String time = String.valueOf(System.currentTimeMillis());
        String filename = time + surfix + "_samples.csv";

        File file = new File(dir, filename);

        if(!dir.exists())
        {
            dir.mkdir();
        }

        try
        {
            OutputStreamWriter outputstreamwriter = new OutputStreamWriter( new FileOutputStream(file, true));

            outputstreamwriter.write( DataSample.toCSV2(samples) );
            outputstreamwriter.close();
            Log.i("DataStorage", "write samples completes.");

        } catch (IOException e)
        {
            e.printStackTrace();
            Log.i("DataStorage", e.toString());
        }

        return surfix;
    }

    private static DataStorage instance;
    public ArrayList<DataSample> samples;
}

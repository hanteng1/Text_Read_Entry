package com.tenghan.swipeflip;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by hanteng on 2017-09-02.
 */

public class DataStorage {
    public DataStorage()
    {
        samples = new ArrayList<DataSample>(100000);
    }

    public static boolean AddSample(int _trial, int _corner, int _anglenum, int _distancenum, int _close,
                                    int _angletarget, int _distancetarget, int _angleactual, int _distanceactual,
                                    long _timestamp)
    {
        if(instance != null)
        {
            instance.add(_trial,  _corner,  _anglenum,  _distancenum, _close,
                    _angletarget,  _distancetarget,  _angleactual,  _distanceactual,
                    _timestamp);
            return true;
        }

        return false;
    }

    public void add(int _trial, int _corner, int _anglenum, int _distancenum, int _close,
                    int _angletarget, int _distancetarget, int _angleactual, int _distanceactual,
                    long _timestamp)
    {
        if(samples != null)
        {
            DataSample sample = new DataSample(_trial,  _corner,  _anglenum,  _distancenum, _close,
                    _angletarget,  _distancetarget,  _angleactual,  _distanceactual,
                    _timestamp);
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

    private static DataStorage instance;
    public ArrayList<DataSample> samples;
}

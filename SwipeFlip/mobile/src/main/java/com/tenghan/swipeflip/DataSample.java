package com.tenghan.swipeflip;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by hanteng on 2017-08-30.
 */

public class DataSample {

    public int trial;

    public int cornerIndex;
    public int angleNum;
    public int distanceNum;

    public int close;

    public int angleTarget;
    public int distanceTarget;
    public int angleActual;
    public int distanceActual;

    public long timestamp;


    public DataSample(int _trial, int _corner, int _anglenum, int _distancenum, int _close,
                      int _angletarget, int _distancetarget, int _angleactual, int _distanceactual,
                      long _timestamp)
    {
        trial = _trial;
        cornerIndex = _corner;
        angleNum = _anglenum;
        distanceNum = _distancenum;
        close = _close;
        angleTarget = _angletarget;
        distanceTarget = _distancetarget;
        angleActual = _angleactual;
        distanceActual = _distanceactual;
        timestamp = _timestamp;
    }

    public static String toCSV(ArrayList<DataSample> arrayList)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for(Iterator<DataSample> iterator = arrayList.iterator(); iterator.hasNext();)
        {
            DataSample sample = iterator.next();
            stringbuilder.append("" + sample.trial + ","
                    + sample.cornerIndex + "," + sample.angleNum + "," + sample.distanceNum + ","
                    + sample.close + ","
                    + sample.angleTarget + "," + sample.distanceTarget + ","
                    + sample.angleActual + "," + sample.distanceActual + ","
                    + sample.timestamp + "," + "\r\n");

        }

        return stringbuilder.toString();
    }



}

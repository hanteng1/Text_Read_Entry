package com.tenghan.markingmenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by hanteng on 2017-08-22.
 */

public class ContentView extends View {

    private static final String TAG = "ContentView";
    private Paint inputPaint = new Paint();
    private int screenWidth, screenHeight;
    private Path touchPath = new Path();
    private PointF textCursor = new PointF();
    private float lineSpace = 20.f;
    private float textSpace = 5.0f;

    public String text = "The judge questioning them is deciding what charges to press over the vehicle attacks that left 15 people dead and more than 100 injured.\n" +
            "Eight other members of the alleged cell are dead - some killed in an explosion, others shot by police.\n" +
            "The last suspect was killed in a vineyard west of Barcelona on Monday.\n" +
            "A 22-year-old Moroccan, Younes Abouyaaqoub, is thought to have been the driver of the van that rammed into crowds in Barcelona on Thursday.\n" +
            "He was wearing a fake explosives belt and shouted \"Allahu Akbar\" (\"God is Greatest\") before he was shot dead by police, said reports.";

    ArrayList<String> textList = new ArrayList<String>(Arrays.asList(text.split("\\s+")));

    public ContentView(Context context, AttributeSet attrs) {
        super(context, attrs);

        inputPaint.setAntiAlias(true);
        inputPaint.setStrokeWidth(0);
        inputPaint.setColor(Color.RED);
        inputPaint.setTextSize(14);
        //inputPaint.setStyle(Paint.Style.STROKE);
        inputPaint.setStrokeJoin(Paint.Join.ROUND);

        textCursor.x = textSpace;
        textCursor.y = -lineSpace * 2;

    }

    public void setDimension(int x, int y)
    {
        screenWidth = x;
        screenHeight = y;
    }

    private void clear()
    {
        touchPath.reset();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        for(int itrt = 0; itrt < textList.size(); itrt++)
        {
            if(textCursor.x + inputPaint.measureText(textList.get(itrt)) >  screenWidth)
            {
                //change to the next line
                textCursor.x = textSpace;
                textCursor.y += lineSpace;
            }

            canvas.drawText(textList.get(itrt), textCursor.x, textCursor.y, inputPaint);
            Log.d(TAG, "X " + textCursor.x + "  y " + textCursor.y);

            textCursor.x += (inputPaint.measureText(textList.get(itrt)) + textSpace);
        }

    }
}

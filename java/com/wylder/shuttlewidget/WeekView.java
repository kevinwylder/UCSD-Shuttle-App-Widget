package com.wylder.shuttlewidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by kevin on 2/4/15.
 */
public class WeekView extends View {

    private static final float PADDING = 15;
    private static final float GRID_LINE_SIZE = 4;
    private static final float LARGE_TEXT_SIZE = 13;
    private static final float SMALL_TEXT_SIZE = 6;
    private static final int DAYS_OF_THE_WEEK = 7;
    private static final int START_TIME = 7;
    private static final int END_TIME = 23;
    private static final int HOURS_OF_OPERATION = (END_TIME - START_TIME);
    private static final float SMALL_BOX_WIDTH_WEIGHT = .75f;  // percent of the normal column that is given to time
    private static final float SMALL_BOX_HEIGHT_WEIGHT = .9f;     // percent of normal row that is given to days of week
    private static final float TOTAL_WIDTH_WEIGHT = DAYS_OF_THE_WEEK + SMALL_BOX_WIDTH_WEIGHT;
    private static final float TOTAL_HEIGHT_WEIGHT = HOURS_OF_OPERATION + SMALL_BOX_HEIGHT_WEIGHT;

    private float ONE_DIP;

    private float sizedPadding;
    private float smallerBoxWidth;
    private float smallerBoxHeight;
    private float dayBoxWidth;
    private float dayBoxHeight;

    private int viewHeight;
    private int viewWidth;

    private Paint borderPaint = new Paint();
    private Paint largeTextPaint = new Paint();
    private Paint smallTextPaint = new Paint();
    private Paint clockwisePaint = new Paint();
    private Paint counterPaint = new Paint();

    private static final String[] dayAbbreviations = new String[]{
            "Sun",
            "Mon",
            "Tues",
            "Wed",
            "Thu",
            "Fri",
            "Sat"
    };

    public WeekView(Context ctx, AttributeSet atts){
        super(ctx, atts);
        ONE_DIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        borderPaint.setStrokeWidth(GRID_LINE_SIZE * ONE_DIP);
        borderPaint.setStyle(Paint.Style.STROKE);
        largeTextPaint.setTextSize(LARGE_TEXT_SIZE * ONE_DIP);
        smallTextPaint.setTextSize(SMALL_TEXT_SIZE * ONE_DIP);
        clockwisePaint.setColor(Color.BLUE);
        counterPaint.setColor(Color.RED);
    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight){
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        viewWidth = width;
        viewHeight = height;
        sizedPadding = PADDING * ONE_DIP;
        dayBoxWidth = (width - 2 * sizedPadding) / (TOTAL_WIDTH_WEIGHT);
        smallerBoxWidth = dayBoxWidth * SMALL_BOX_WIDTH_WEIGHT;
        dayBoxHeight = (height - 2 * sizedPadding) / (TOTAL_HEIGHT_WEIGHT);
        smallerBoxHeight = SMALL_BOX_HEIGHT_WEIGHT * dayBoxHeight;
    }

    @Override
    public void onDraw(Canvas canvas){
        canvas.drawRect(sizedPadding, sizedPadding, TOTAL_WIDTH_WEIGHT * dayBoxWidth + sizedPadding,
                sizedPadding + smallerBoxHeight, borderPaint);
        canvas.drawRect(sizedPadding, sizedPadding, sizedPadding + smallerBoxWidth,
                TOTAL_HEIGHT_WEIGHT * dayBoxHeight + sizedPadding, borderPaint);
        float startX = sizedPadding + smallerBoxWidth;
        float startY = sizedPadding + smallerBoxHeight;
        for(int x = 0; x < DAYS_OF_THE_WEEK; x++){
            for(int y = 0; y < HOURS_OF_OPERATION; ){           // incremented when drawing box
                canvas.drawRect(startX + (x * dayBoxWidth), startY + (y * dayBoxHeight),
                        startX + ((x + 1) * dayBoxWidth), startY + (++y * dayBoxHeight), borderPaint);
            }
        }
        float relativeTextBox = (dayBoxHeight + smallTextPaint.getTextSize()) / 2.0f;
        for(int y = 0; y < HOURS_OF_OPERATION; y++){
            float yPos = sizedPadding + smallerBoxHeight + (y * dayBoxHeight) + relativeTextBox;
            String time = ScheduleConstraint.getTimeString(START_TIME + y, 0);
            float xPos = sizedPadding + (smallerBoxWidth - smallTextPaint.measureText(time)) / 2.0f;
            canvas.drawText(time, xPos, yPos, smallTextPaint);
        }
        float yPos = sizedPadding + (smallerBoxHeight + largeTextPaint.getTextSize()) / 2.0f;
        for(int x = 0; x < DAYS_OF_THE_WEEK; x++){
            float xPos = sizedPadding + smallerBoxWidth + (x * dayBoxWidth)
                    + (smallerBoxWidth - largeTextPaint.measureText(dayAbbreviations[x])) / 2.0f;
            canvas.drawText(dayAbbreviations[x], xPos, yPos, largeTextPaint);
        }
    }

    public void displayConstraints(ScheduleConstraint[] constraints){

    }

}

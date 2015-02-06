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
    private static final int HOURS_OF_OPERATION = (ShuttleConstants.HOUR_END - ShuttleConstants.HOUR_START);
    private static final float SMALL_BOX_WIDTH_WEIGHT = .75f;  // percent of the normal column that is given to time
    private static final float SMALL_BOX_HEIGHT_WEIGHT = .9f;     // percent of normal row that is given to days of week
    private static final float TOTAL_WIDTH_WEIGHT = ShuttleConstants.DAYS_OF_THE_WEEK + SMALL_BOX_WIDTH_WEIGHT;
    private static final float TOTAL_HEIGHT_WEIGHT = HOURS_OF_OPERATION + SMALL_BOX_HEIGHT_WEIGHT;

    private float ONE_DIP;

    private float sizedPadding;
    private float smallerBoxWidth;
    private float smallerBoxHeight;
    private float hourBoxWidth;
    private float hourBoxHeight;

    private Paint borderPaint = new Paint();
    private Paint largeTextPaint = new Paint();
    private Paint smallTextPaint = new Paint();
    private Paint clockwisePaint = new Paint();
    private Paint counterPaint = new Paint();

    private static final String[] dayAbbreviations = new String[]{
            "Mon",
            "Tues",
            "Wed",
            "Thu",
            "Fri",
            "Sat"
    };

    private ScheduleConstraint[] constraints = new ScheduleConstraint[0];

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
        sizedPadding = PADDING * ONE_DIP;
        hourBoxWidth = (width - 2 * sizedPadding) / (TOTAL_WIDTH_WEIGHT);
        smallerBoxWidth = hourBoxWidth * SMALL_BOX_WIDTH_WEIGHT;
        hourBoxHeight = (height - 2 * sizedPadding) / (TOTAL_HEIGHT_WEIGHT);
        smallerBoxHeight = SMALL_BOX_HEIGHT_WEIGHT * hourBoxHeight;
    }

    @Override
    public void onDraw(Canvas canvas){
        // start by drawing the constraints
        for(int i = 0; i < constraints.length; i++){        // foreach constraint
            ScheduleConstraint constraint = constraints[i];
            for(int day = 0; day < ShuttleConstants.DAYS_OF_THE_WEEK; day++){    // foreach day of the week
                if(constraint.daysActive[day]){
                    for(int hour = constraint.hourStart; hour < constraint.hourEnd; hour++) { // for each hour active
                        // draw a colored box
                        float startX = sizedPadding + smallerBoxWidth + (hourBoxWidth * day);
                        float startY = sizedPadding + smallerBoxHeight + (hourBoxHeight * (hour - ShuttleConstants.HOUR_START));
                        if(constraint.routeId == 0) {
                            canvas.drawRect(startX, startY, startX + hourBoxWidth, startY + hourBoxHeight, counterPaint);
                        }else{
                            canvas.drawRect(startX, startY, startX + hourBoxWidth, startY + hourBoxHeight, clockwisePaint);
                        }
                        // write the bus name/route
                        canvas.drawText(constraint.getStopName(),
                                startX + (hourBoxWidth - smallTextPaint.measureText(constraint.getStopName())) / 2.0f,
                                startY + (hourBoxHeight + smallTextPaint.getTextSize()) / 2.0f,
                                smallTextPaint);
                    }
                }
            }
        }
        // outline the smaller boxes time and day
        canvas.drawRect(sizedPadding, sizedPadding, TOTAL_WIDTH_WEIGHT * hourBoxWidth + sizedPadding,
                sizedPadding + smallerBoxHeight, borderPaint);
        canvas.drawRect(sizedPadding, sizedPadding, sizedPadding + smallerBoxWidth,
                TOTAL_HEIGHT_WEIGHT * hourBoxHeight + sizedPadding, borderPaint);
        // draw each hour box
        float startX = sizedPadding + smallerBoxWidth;
        float startY = sizedPadding + smallerBoxHeight;
        for(int x = 0; x < ShuttleConstants.DAYS_OF_THE_WEEK; x++){
            for(int y = 0; y < HOURS_OF_OPERATION; ){           // incremented when drawing box
                canvas.drawRect(startX + (x * hourBoxWidth), startY + (y * hourBoxHeight),
                        startX + ((x + 1) * hourBoxWidth), startY + (++y * hourBoxHeight), borderPaint);
            }
        }
        // put the time text in the boxes
        float relativeTextBox = (hourBoxHeight + smallTextPaint.getTextSize()) / 2.0f;
        for(int y = 0; y < HOURS_OF_OPERATION; y++){
            float yPos = sizedPadding + smallerBoxHeight + (y * hourBoxHeight) + relativeTextBox;
            String time = ScheduleConstraint.getTimeString(ShuttleConstants.HOUR_START + y);
            float xPos = sizedPadding + (smallerBoxWidth - smallTextPaint.measureText(time)) / 2.0f;
            canvas.drawText(time, xPos, yPos, smallTextPaint);
        }
        // put the day text in the top row
        float yPos = sizedPadding + (smallerBoxHeight + largeTextPaint.getTextSize()) / 2.0f;
        for(int x = 0; x < ShuttleConstants.DAYS_OF_THE_WEEK; x++){
            float xPos = sizedPadding + smallerBoxWidth + (x * hourBoxWidth)
                    + (smallerBoxWidth - largeTextPaint.measureText(dayAbbreviations[x])) / 2.0f;
            canvas.drawText(dayAbbreviations[x], xPos, yPos, largeTextPaint);
        }
    }

    public void displayConstraints(ScheduleConstraint[] constraints){
        this.constraints = constraints;
        invalidate();
    }

}

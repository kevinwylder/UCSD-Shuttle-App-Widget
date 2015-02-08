package com.wylder.shuttlewidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by kevin on 2/4/15.
 *
 * a View class that shows an overview of the week based off what's in the ConstraintDatabase
 */
public class WeekView extends View {

    // constants that don't depend on pixel density/ will be scaled to pixel density
    private static final float PADDING = 15;
    private static final float GRID_LINE_SIZE = 4;
    private static final float LARGE_TEXT_SIZE = 13;
    private static final float SMALL_TEXT_SIZE = 6;
    private static final int HOURS_OF_OPERATION = (ShuttleConstants.HOUR_END - ShuttleConstants.HOUR_START);
    private static final float SMALL_BOX_WIDTH_WEIGHT = .75f;  // percent of the normal column that is given to time
    private static final float SMALL_BOX_HEIGHT_WEIGHT = .9f;     // percent of normal row that is given to days of week
    private static final float TOTAL_WIDTH_WEIGHT = ShuttleConstants.DAYS_OF_THE_WEEK + SMALL_BOX_WIDTH_WEIGHT;
    private static final float TOTAL_HEIGHT_WEIGHT = HOURS_OF_OPERATION + SMALL_BOX_HEIGHT_WEIGHT;

    // one density independent pixel
    private float ONE_DIP;

    // constants that depend on the pixel density
    private float sizedPadding;
    private float smallerBoxWidth;
    private float smallerBoxHeight;
    private float hourBoxWidth;
    private float hourBoxHeight;

    // paints
    private Paint borderPaint = new Paint();
    private Paint largeTextPaint = new Paint();
    private Paint smallTextPaint = new Paint();
    private Paint stopTextPaint = new Paint();
    private Paint stopBackgroundPaint = new Paint();

    private static final String[] dayAbbreviations = new String[]{
            "Mon",
            "Tues",
            "Wed",
            "Thu",
            "Fri",
            "Sat"
    };

    private ScheduleConstraint[] constraints = new ScheduleConstraint[0];

    public WeekView(Context ctx){
        super(ctx);
        // get an instance of one dp to scale for the constants
        ONE_DIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());

        // setup paints
        borderPaint.setStrokeWidth(GRID_LINE_SIZE * ONE_DIP);
        borderPaint.setStyle(Paint.Style.STROKE);
        largeTextPaint.setTextSize(LARGE_TEXT_SIZE * ONE_DIP);
        smallTextPaint.setTextSize(SMALL_TEXT_SIZE * ONE_DIP);
        stopBackgroundPaint.setStyle(Paint.Style.FILL);
        stopTextPaint.set(smallTextPaint);
    }

    /**
     * Called when the size of the view changes, including initialization.
     * here we setup specific instance variables to help paint
     */
    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight){
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        sizedPadding = PADDING * ONE_DIP;
        hourBoxWidth = (width - 2 * sizedPadding) / (TOTAL_WIDTH_WEIGHT);
        smallerBoxWidth = hourBoxWidth * SMALL_BOX_WIDTH_WEIGHT;
        hourBoxHeight = (height - 2 * sizedPadding) / (TOTAL_HEIGHT_WEIGHT);
        smallerBoxHeight = SMALL_BOX_HEIGHT_WEIGHT * hourBoxHeight;
    }

    /**
     * called to draw the paint onto the given Canvas.
     *
     * @param canvas the object that will take drawing instructions
     */
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
                        stopTextPaint.setColor(constraint.getTextColor());
                        stopBackgroundPaint.setColor(constraint.getBackgroundColor());
                        canvas.drawRect(startX, startY, startX + hourBoxWidth, startY + hourBoxHeight, stopBackgroundPaint);
                        // write the bus name/route
                        canvas.drawText(constraint.getStopName(),
                                startX + (hourBoxWidth - smallTextPaint.measureText(constraint.getStopName())) / 2.0f,
                                startY + (hourBoxHeight + smallTextPaint.getTextSize()) / 2.0f, stopTextPaint);
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

    /**
     * a method that accepts an array of ScheduleConstraints to display
     */
    public void displayConstraints(ScheduleConstraint[] constraints){
        this.constraints = constraints;
        invalidate();
    }

}

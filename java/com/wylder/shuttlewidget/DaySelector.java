package com.wylder.shuttlewidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by kevin on 2/2/15.
 */
public class DaySelector extends View {

    private float viewWidth = 0f;
    private float viewHeight = 0f;
    private float padding = 0;
    private float startX = 0f;
    private float startY = 0f;
    private float cellWidth = 0f;
    private float cellHeight = 0f;
    private float textYOffset = 0f;

    private static final float textSize = 13f;
    private static final float textBoldness = 1f;
    private static final float cellStrokeWidth = 3f;
    private static final float paddingPercentage = 0.02f;
    private float ONE_DIP;

    private Paint cellPaint = new Paint();
    private Paint textPaint = new Paint();
    private Paint selectedPaint = new Paint();

    private boolean[] selectedDays = new boolean[ShuttleConstants.DAYS_OF_THE_WEEK];

    private static final String[] dayAbbreviations = new String[]{
            "Mon",
            "Tues",
            "Wed",
            "Thu",
            "Fri",
            "Sat"
    };

    public DaySelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        // get a density independent pixel to scale
        ONE_DIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        // setup paints
        selectedPaint.setColor(Color.CYAN);
        selectedPaint.setStyle(Paint.Style.FILL);
        cellPaint.setStyle(Paint.Style.STROKE);
        cellPaint.setStrokeWidth(cellStrokeWidth * ONE_DIP);
        textPaint.setTextSize(textSize * ONE_DIP);
        textPaint.setStrokeWidth(textBoldness * ONE_DIP);
    }

    @Override
    public void onSizeChanged(int width, int height, int oldWidth, int oldHeight){
        viewWidth = width;
        viewHeight = height;
        padding = width * paddingPercentage;
        cellWidth = (width - (2 * padding)) / ShuttleConstants.DAYS_OF_THE_WEEK;         // divide into 6 for days of the week
        cellHeight = height - (2 * padding);
        startX = startY = padding;
        textYOffset = (height + textPaint.getTextSize()) / 2.0f;  // center vertically only once
        super.onSizeChanged(width, height, oldWidth, oldHeight);
    }

    @Override
    public void onDraw(Canvas canvas){
        float x = startX;   // variable that will move across the view
        for(int i = 0; i < ShuttleConstants.DAYS_OF_THE_WEEK; i++){  // for each day in the week
            if(selectedDays[i]){     // if the user highlighted this day, create a cyan base
                canvas.drawRect(x, startY, x + cellWidth, startY + cellHeight, selectedPaint);
            }
            // draw a box black box for the text and day of the week
            canvas.drawRect(x, startY, x + cellWidth, startY + cellHeight, cellPaint);
            // draw vertically and horizontally centered text for the day of the week
            float textWidth = textPaint.measureText(dayAbbreviations[i]);
            float xOffset = (cellWidth - textWidth) / 2.0f;
            canvas.drawText(dayAbbreviations[i], x + xOffset, textYOffset, textPaint);
            // move to the next x position
            x += cellWidth;
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // check if touchdown, then if it is in the range of Y values
        if(this.isEnabled() &&
                event.getAction() == MotionEvent.ACTION_DOWN &&
                event.getY() > padding &&
                event.getY() < viewHeight - padding){
            float x = event.getX();
            int weekIndex = (int) ((ShuttleConstants.DAYS_OF_THE_WEEK * (x - padding)) / (viewWidth - (2 * padding)));
            if(weekIndex < ShuttleConstants.DAYS_OF_THE_WEEK){  // weekIndex is 7 if touching the far right padding, that would be a problem
                selectedDays[weekIndex] = !selectedDays[weekIndex];
                this.invalidate();      // request redraw with new selectedDays array
            }
        }
        return super.onTouchEvent(event);
    }

    public boolean[] getSelectedDays(){
        return selectedDays;
    }

    public void setSelectedDays(boolean[] selectedDays){
        this.selectedDays = selectedDays;
    }


}

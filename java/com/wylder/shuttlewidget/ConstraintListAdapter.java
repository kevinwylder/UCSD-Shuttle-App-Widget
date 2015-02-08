package com.wylder.shuttlewidget;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by kevin on 2/6/15.
 *
 * A class to hold ScheduleConstraints and display them in a ListView
 */
public class ConstraintListAdapter extends ArrayAdapter<ScheduleConstraint> {

    /**
     * The only constructor
     * @param ctx   the context the ListView lives in
     * @param constraints   What constraints to show.
     */
    public ConstraintListAdapter(Context ctx, ScheduleConstraint[] constraints){
        super(ctx, R.layout.constraint_list_element, constraints);
    }

    /**
     * A method to convert a ScheduleConstraint into a view by "filling out a form" (setting up the views)
     * @param position
     * @param convertView   a recycled view to put the info
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View returner = convertView;
        if(convertView == null){    // if the view hasn't been inflated before (ie, not recycled)
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            returner = layoutInflater.inflate(R.layout.constraint_list_element, null);
        }
        // set up views
        TextView stopView = (TextView) returner.findViewById(R.id.stopNameText);
        TextView timeView = (TextView) returner.findViewById(R.id.timeRangeText);
        TextView loopView = (TextView) returner.findViewById(R.id.loopNameText);
        ScheduleConstraint constraint = getItem(position);
        stopView.setText(constraint.getStopName());
        timeView.setText(constraint.getTimeRangeString());
        loopView.setText(constraint.getRouteName());
        loopView.setTextColor(constraint.getTextColor());
        loopView.setBackgroundColor(constraint.getBackgroundColor());
        DaySelector daySelector = (DaySelector) returner.findViewById(R.id.daySelector);
        daySelector.setEnabled(false);  // don't let the user change the days
        daySelector.setSelectedDays(constraint.daysActive);
        return returner;
    }



}

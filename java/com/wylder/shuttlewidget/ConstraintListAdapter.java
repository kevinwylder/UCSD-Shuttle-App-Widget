package com.wylder.shuttlewidget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by kevin on 2/6/15.
 */
public class ConstraintListAdapter extends ArrayAdapter<ScheduleConstraint> {

    public ConstraintListAdapter(Context ctx, ScheduleConstraint[] constraints){
        super(ctx, R.layout.constraint_list_element, constraints);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View returner = convertView;
        if(convertView == null){
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            returner = layoutInflater.inflate(R.layout.constraint_list_element, null);
        }
        TextView stopView = (TextView) returner.findViewById(R.id.stopNameText);
        TextView timeView = (TextView) returner.findViewById(R.id.timeRangeText);
        TextView loopView = (TextView) returner.findViewById(R.id.loopNameText);
        ScheduleConstraint constraint = getItem(position);
        stopView.setText(constraint.getStopName());
        timeView.setText(constraint.getTimeRangeString());
        loopView.setText(constraint.getRouteName());
        if(constraint.routeId == 0){
            loopView.setBackgroundColor(Color.RED);
        }else{
            loopView.setBackgroundColor(Color.BLUE);
        }
        DaySelector daySelector = (DaySelector) returner.findViewById(R.id.daySelector);
        daySelector.setEnabled(false);
        daySelector.setSelectedDays(constraint.daysActive);
        return returner;
    }



}

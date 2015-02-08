package com.wylder.shuttlewidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by kevin on 2/2/15.
 *
 * Activity to give a UI to construct a new ScheduleConstraint
 * Meant to be used with startActivityForResult()
 * result is an intent that can be used in the ScheduleConstraint constructor
 */
public class AddConstraintActivity extends Activity {

    private TimePicker startTime;
    private TimePicker endTime;
    private Spinner routeSpinner;
    private Spinner stopSpinner;
    private DaySelector daysOfTheWeek;
    private Button submitButton;

    @Override
    public void onCreate(Bundle sis){
        // setup views
        super.onCreate(sis);
        setContentView(R.layout.add_constraint);

        startTime = (TimePicker) findViewById(R.id.startTimePicker);
        endTime = (TimePicker) findViewById(R.id.endTimePicker);
        setUpTimePicker(startTime);
        setUpTimePicker(endTime);

        ArrayAdapter<String> routesAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, ShuttleConstants.routes);
        ArrayAdapter<String> stopsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, ShuttleConstants.stops);

        routeSpinner = (Spinner) findViewById(R.id.routeSpinner);
        stopSpinner  = (Spinner) findViewById(R.id.stopSpinner);
        routeSpinner.setAdapter(routesAdapter);
        stopSpinner.setAdapter(stopsAdapter);

        daysOfTheWeek = (DaySelector) findViewById(R.id.weekView);
        submitButton = (Button) findViewById(R.id.button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // use all the views to make a ScheduleConstraint
                ScheduleConstraint constraint = new ScheduleConstraint(daysOfTheWeek.getSelectedDays(),
                        startTime.getCurrentHour(), endTime.getCurrentHour(),
                        routeSpinner.getSelectedItemPosition(), stopSpinner.getSelectedItemPosition()
                );
                Intent returnIntent = new Intent();
                constraint.setConstraintInfo(returnIntent);    // load constraint info into the intent
                setResult(constraint.legality(), returnIntent);
                finish();
            }
        });
    }

    /**
     * this method manipulates the given TimePicker to not have a scrollable minute column
     * It works by accessing the class and getting a field by name
     *
     * http://stackoverflow.com/a/22958654
     */
    private void setUpTimePicker(TimePicker picker){
        try{
            // find the field in the class to manipulate. this feels sketchy.
            // source from Ali at http://stackoverflow.com/a/22958654
            Class<?> classById = Class.forName("com.android.internal.R$id");
            Field field = classById.getField("minute");
            NumberPicker minutePicker = (NumberPicker) picker.findViewById(field.getInt(null));

            minutePicker.setMinValue(0);
            minutePicker.setMaxValue(0);
            ArrayList<String> values = new ArrayList<String>();
            values.add("00");
            values.add("00");
            minutePicker.setDisplayedValues(values.toArray(new String[values.size()]));
        }catch (Exception e){
            Log.e("KevinRuntime", "TimePicker unable to find method to change increments");
            finish();
        }
    }
}

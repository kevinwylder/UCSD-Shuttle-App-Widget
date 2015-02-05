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
        super.onCreate(sis);
        setContentView(R.layout.add_constraint);

        startTime = (TimePicker) findViewById(R.id.startTimePicker);
        endTime = (TimePicker) findViewById(R.id.endTimePicker);
        setUpTimePicker(startTime);
        setUpTimePicker(endTime);

        ArrayAdapter<String> routesAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, ScheduleConstraint.routes);
        ArrayAdapter<String> stopsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, ScheduleConstraint.stops);

        routeSpinner = (Spinner) findViewById(R.id.routeSpinner);
        stopSpinner  = (Spinner) findViewById(R.id.stopSpinner);
        routeSpinner.setAdapter(routesAdapter);
        stopSpinner.setAdapter(stopsAdapter);

        daysOfTheWeek = (DaySelector) findViewById(R.id.weekView);
        submitButton = (Button) findViewById(R.id.button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ScheduleConstraint constraint = new ScheduleConstraint(daysOfTheWeek.getSelectedDays(),
                        startTime.getCurrentHour(), startTime.getCurrentMinute(),
                        endTime.getCurrentHour(), endTime.getCurrentMinute(),
                        routeSpinner.getSelectedItemPosition(), stopSpinner.getSelectedItemPosition()
                );
                Intent returnIntent = new Intent();
                constraint.setConstraintInfo(returnIntent);
                setResult(constraint.legality(), returnIntent);
                finish();
            }
        });
    }

    private void setUpTimePicker(TimePicker picker){
        try{
            // find the field in the class to manipulate. this feels sketchy.
            // source from Ali at http://stackoverflow.com/a/22958654
            Class<?> classById = Class.forName("com.android.internal.R$id");
            Field field = classById.getField("minute");
            NumberPicker minutePicker = (NumberPicker) picker.findViewById(field.getInt(null));

            minutePicker.setMinValue(0);
            minutePicker.setMaxValue(3);
            ArrayList<String> values = new ArrayList<String>();
            for (int i = 0; i < 60; i += 15) {
                values.add("" + i);
            }
            for (int i = 0; i < 60; i += 15) {
                values.add("" + i);
            }
            minutePicker.setDisplayedValues(values.toArray(new String[values.size()]));
        }catch (Exception e){
            Log.e("KevinRuntime", "TimePicker unable to find method to change increments");
            finish();
        }
    }
}

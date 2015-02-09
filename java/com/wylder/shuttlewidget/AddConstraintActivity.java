package com.wylder.shuttlewidget;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;

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

    private boolean fromWidget = false;     // a flag for if the intent came from the widget

    private ArrayAdapter<String> routesAdapter;
    private ArrayAdapter<String> stopsAdapter;
    private ArrayList<String> stopsAdapterDataBackend;

    private AdapterView.OnItemSelectedListener onRouteSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int selected, long l) {
            if(!stopsAdapter.isEmpty()) {
                stopsAdapterDataBackend.clear();
                for(int i = 0; i < ShuttleConstants.stopNames[selected].length; i++){
                    stopsAdapterDataBackend.add(ShuttleConstants.stopNames[selected][i]);
                }
                stopsAdapter.notifyDataSetChanged();
            }
            adapterView.setBackgroundColor(ShuttleConstants.widgetColors[selected]);
            ((TextView)adapterView.getChildAt(0)).setTextColor(ShuttleConstants.textColors[selected]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            if(!stopsAdapter.isEmpty()){
                stopsAdapterDataBackend.clear();
                stopsAdapter.notifyDataSetChanged();
            }
            // the last element in the ShuttleConstants color lists are the default colors
            adapterView.setBackgroundColor(ShuttleConstants.widgetColors[ShuttleConstants.widgetColors.length - 1]);
            ((TextView)adapterView.getChildAt(0)).setTextColor(ShuttleConstants.textColors[ShuttleConstants.textColors.length - 1]);
        }
    };

    @Override
    public void onCreate(Bundle sis){
        // setup views
        super.onCreate(sis);
        setContentView(R.layout.add_constraint);

        startTime = (TimePicker) findViewById(R.id.startTimePicker);
        endTime = (TimePicker) findViewById(R.id.endTimePicker);
        setUpTimePicker(startTime);
        setUpTimePicker(endTime);
        // add 1 to the end hour to start with a valid range
        endTime.setCurrentHour(endTime.getCurrentHour() + 1);

        // create ArrayAdapters for each dataset. stops must have a backend because it will change
        stopsAdapterDataBackend = new ArrayList<String>();
        for(int i = 0; i < ShuttleConstants.stopNames[0].length; i++){
            stopsAdapterDataBackend.add(ShuttleConstants.stopNames[0][i]);
        }
        routesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                ShuttleConstants.routeNames);
        stopsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                stopsAdapterDataBackend);

        routeSpinner = (Spinner) findViewById(R.id.routeSpinner);
        stopSpinner  = (Spinner) findViewById(R.id.stopSpinner);
        routeSpinner.setAdapter(routesAdapter);
        stopSpinner.setAdapter(stopsAdapter);
        routeSpinner.setOnItemSelectedListener(onRouteSelectedListener);

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

        // test if the user wants to add a constraint from the widget
        if(getIntent().getBooleanExtra(StopSchedulerActivity.ACTION_CREATE_CONSTRAINT, false)){
            fromWidget = true;
            // if the user is coming from the widget, set today to true in DaySelector
            boolean[] days = new boolean[ShuttleConstants.DAYS_OF_THE_WEEK];
            int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
            if(today != -1){        // avoid crashes on Sunday
                days[today] = true;
                daysOfTheWeek.setSelectedDays(days);
            }
        }

        // put name and back button in action bar
        ActionBar actionBar = getActionBar();
        actionBar.setTitle("Add to Schedule");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * This overridden method is run when the user clicks the back arrow.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                if(fromWidget) {
                    // request to close the main activity too
                    setResult(StopSchedulerActivity.RESULT_CLOSE);
                }
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            setResult(Activity.RESULT_CANCELED);
            finish();
        }
    }
}

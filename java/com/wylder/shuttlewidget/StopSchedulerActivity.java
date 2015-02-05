package com.wylder.shuttlewidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.sql.Time;
import java.util.ArrayList;


public class StopSchedulerActivity extends Activity {

    private int REQUEST_CODE = 12;
    WeekView weekView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // weekView = new WeekView(this);
       // setContentView(weekView);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stop_scheduler, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startActivity = new Intent(StopSchedulerActivity.this, AddConstraintActivity.class);
            startActivityForResult(startActivity, REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, "Press button to add constraint", Toast.LENGTH_SHORT).show();
        }else if(resultCode == ScheduleConstraint.BAD_TIME_RANGE){
            Toast.makeText(this, "Constraint not created, bad time range", Toast.LENGTH_LONG).show();
        }else if(resultCode == ScheduleConstraint.NO_DAYS_SELECTED){
            Toast.makeText(this, "Constraint not created, no days selected", Toast.LENGTH_LONG).show();
        }else if(resultCode == ScheduleConstraint.EMPTY_CONSTRAINT){
            Toast.makeText(this, "Constraint not created, something went horribly wrong", Toast.LENGTH_LONG).show();
        }else if(resultCode == ScheduleConstraint.LEGAL_CONSTRAINT){
            TextView tv = new TextView(this);
            ScheduleConstraint constraint = new ScheduleConstraint(data);
            tv.setText(constraint.toString());
            setContentView(tv);
        }
    }


}

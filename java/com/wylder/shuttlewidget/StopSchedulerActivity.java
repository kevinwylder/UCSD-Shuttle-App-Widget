package com.wylder.shuttlewidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * This is the main activity, launched from the app icon. It displays two views - a week overview
 * of ScheduleConstraints and a ListView with each constraint listed - inside a ViewPager
 * It also launches AddConstraintActivity for a result.
 */
public class StopSchedulerActivity extends Activity {

    private static final int REQUEST_CODE = 12;

    private ViewPager pager;
    private ConstraintViewAdapter adapter;
    private ConstraintDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setup views and the database
        pager = new ViewPager(this);
        adapter = new ConstraintViewAdapter(this);
        database = new ConstraintDatabase(this);
        pager.setAdapter(adapter);
        setContentView(pager);
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
        if (id == R.id.add_constraint) {
            // when the user clicks "ADD CONSTRAINT" start the AddConstraintActivity
            Intent startActivity = new Intent(StopSchedulerActivity.this, AddConstraintActivity.class);
            startActivityForResult(startActivity, REQUEST_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * This method is called when the result from AddConstraintActivity is ready.
     * If the Constraint isn't good, it will display an appropriate Toast explaining why
     * @param requestCode to keep track of calls to startActivityForResult
     * @param resultCode an int representing the legality of the created ScheduleConstraint, or RESULT_CANCELLED
     * @param data the intent containing encoded ScheduleConstraint data
     */
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
            // If we've made it this far, we may as well create the ScheduleConstraint
            ScheduleConstraint constraint = new ScheduleConstraint(data);
            if(!database.constraintConflict(constraint)){   // no conflict in database
                Log.e("KevinRuntime", "adding constraint returned from AddConstraintActivity");
                database.addConstraint(constraint);
                adapter.updateConstraintsFromDatabase();
            }else{
                // there was a conflict in the database and we can't use this Constraint
                Log.e("KevinRuntime", "ConstraintDatabase found a conflicting constraint, so It's not being added to the database");
                Toast.makeText(this, "Constraint not created, there's a conflict with another Constraint", Toast.LENGTH_LONG).show();
            }

        }
    }


}

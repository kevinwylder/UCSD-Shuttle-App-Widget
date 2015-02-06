package com.wylder.shuttlewidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;


public class StopSchedulerActivity extends Activity {

    private int REQUEST_CODE = 12;
    WeekView weekView;
    ConstraintDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ListView listView = new ListView(this);
       // setContentView(R.layout.stop_scheduler);
      //  weekView = (WeekView) findViewById(R.id.weekView);
        database = new ConstraintDatabase(this);
      //  weekView.displayConstraints(database.getAllConstraints());
        ConstraintListAdapter adapter = new ConstraintListAdapter(this, database.getAllConstraints());
        listView.setAdapter(adapter);
        setContentView(listView);
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
            ScheduleConstraint constraint = new ScheduleConstraint(data);
            if(!database.constraintConflict(constraint)){
                Log.e("KevinRuntime", "adding constraint returned from AddConstraintActivity");
                database.addConstraint(constraint);
                weekView.displayConstraints(database.getAllConstraints());
            }else{
                Log.e("KevinRuntime", "ConstraintDatabase found a conflicting constraint, so It's not being added to the database");
                Toast.makeText(this, "Constraint not created, there's a conflict with another Constraint", Toast.LENGTH_LONG).show();
            }

        }
    }


}

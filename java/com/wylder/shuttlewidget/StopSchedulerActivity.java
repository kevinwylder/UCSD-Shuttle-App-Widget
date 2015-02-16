package com.wylder.shuttlewidget;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * This is the main activity, launched from the app icon. It displays two views - a week overview
 * of ScheduleConstraints and a ListView with each constraint listed - inside a ViewPager
 * It also launches AddConstraintActivity for a result.
 */
public class StopSchedulerActivity extends FragmentActivity{

    public static final int REQUEST_CODE = 12;
    public static final int RESULT_CLOSE = 244;    // return this to close the app onResult

    // action to open and directly ask to make new constraint
    public static final String ACTION_CREATE_CONSTRAINT = "com.wylder.shuttlewidget.new constraint";

    private static final String[] tabNames = {
            "Search", "Week View", "List View"
    };

    private ViewPager pager;
    private MainViewAdapter adapter;
    private ActionBar actionBar;
    private ConstraintDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        pager = (ViewPager) findViewById(R.id.pagerValid);
        // setup the actionbar to have tabs
        actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        for(int i = 0; i < tabNames.length; i++){
            final int position = i;                     // make this accessable to the TabListener
            ActionBar.Tab tab = actionBar.newTab();     // create a new tab
            tab.setText(tabNames[i]);                   // set the title of the tab
            tab.setTabListener(new ActionBar.TabListener() {
                @Override
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
                    pager.setCurrentItem(position);     // move the pager to the selected tab
                }

                @Override
                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

                @Override
                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}
            });
            actionBar.addTab(tab);
        }
        // if intent has flag to create a new constraint, start AddConstraintActivity
        if(getIntent().getBooleanExtra(ACTION_CREATE_CONSTRAINT, false)){
            Intent startActivity = new Intent(StopSchedulerActivity.this, AddConstraintActivity.class);
            // add same flag to let the class know it should select today
            startActivity.putExtra(ACTION_CREATE_CONSTRAINT, true);
            startActivityForResult(startActivity, REQUEST_CODE);
        }
        // create database and display help if first creation of database
        database = new ConstraintDatabase(this);
        if(database.newDatabaseFlag){
            displayHelp();
        }
        // create and setup views
        adapter = new MainViewAdapter(getSupportFragmentManager(), database);
        pager.setAdapter(adapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            /**
             * Called when the ViewPager moves pages
             * @param position the new selected page index
             */
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        // check if Today is in the range of days of operation, and if not, put the pager on WeekView
        Calendar calendar = Calendar.getInstance();
        int todayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2;  // Sunday is day 1, and we need Monday to be index 0
        if(todayOfTheWeek < 0 || todayOfTheWeek >= ShuttleConstants.DAYS_OF_THE_WEEK){
            // weekend baby!
            pager.setCurrentItem(1);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stop_scheduler, menu);
        return true;
    }

    /**
     * a method to cleanup after the activity finishes. here we close the database
     */
    @Override
    public void onDestroy(){
        database.closeDatabase();
        super.onDestroy();
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
        }else if(id == R.id.help){
            displayHelp();
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
        }else if(resultCode == RESULT_CLOSE){   // special case where the user opened the app from the widget and pressed actionbar up arrow
            finish();
        }else if(resultCode == ScheduleConstraint.BAD_TIME_RANGE){
            Toast.makeText(this, "Constraint not created, bad time range", Toast.LENGTH_LONG).show();
        }else if(resultCode == ScheduleConstraint.SHUTTLE_NOT_RUNNING){
            Toast.makeText(this, "Constraint not created, shuttle doesn't run during that time", Toast.LENGTH_LONG).show();
        }else if(resultCode == ScheduleConstraint.NO_DAYS_SELECTED){
            Toast.makeText(this, "Constraint not created, no days selected", Toast.LENGTH_LONG).show();
        }else if(resultCode == ScheduleConstraint.EMPTY_CONSTRAINT){
            Toast.makeText(this, "Constraint not created, something went horribly wrong", Toast.LENGTH_LONG).show();
        }else if(resultCode == ScheduleConstraint.LEGAL_CONSTRAINT){
            // If we've made it this far, we may as well create the ScheduleConstraint
            ScheduleConstraint constraint = new ScheduleConstraint(data);
            if(!database.constraintConflict(constraint)){   // no conflict in database
                database.addConstraint(constraint);
            }else{
                // there was a conflict in the database and we can't use this Constraint
                Toast.makeText(this, "Constraint not created, there's a conflict with another Constraint", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * a method to show an overlay leading the user to add a constraint and the widget to the homescreen
     */
    public void displayHelp(){
        // we'll be drawing over the whole window, so let's get a WindowManager
        WindowManager windowManager = this.getWindowManager();
        // create a drawing environment the size of the screen
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        Bitmap bitmap = Bitmap.createBitmap(metrics.widthPixels, metrics.heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float ONE_DIP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        Rect visibleFrame = new Rect();         // will contain the height of the statusbar in visibleFrame.top
        getWindow().getDecorView().getWindowVisibleDisplayFrame(visibleFrame);

        // draw the help screen
        // draw a path that goes around the add button, and the whole screen
        float guessPosX = 17 * ONE_DIP;
        float guessPosY = 17 * ONE_DIP;

        float circleRadius = (.2f * metrics.widthPixels);
        RectF circleDimens = new RectF(
                metrics.widthPixels - circleRadius - guessPosX, visibleFrame.top - circleRadius + guessPosY,
                metrics.widthPixels + circleRadius - guessPosX, visibleFrame.top + circleRadius + guessPosY
        );
        Path path = new Path();
        path.moveTo(-50, -50);
        path.arcTo(circleDimens, 240, -180);
        path.lineTo(metrics.widthPixels + 50, metrics.heightPixels + 50);
        path.lineTo(-50, metrics.heightPixels + 50);
        path.close();
        Paint paint = new Paint();
        paint.setColor(Color.argb(200, 0, 153, 204));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, paint);
        paint.setColor(Color.argb(255, 51, 181, 229));
        paint.setStrokeWidth(ONE_DIP * 10);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, paint);
        // draw an text on screen
        paint.setStrokeWidth(ONE_DIP * 2);
        paint.setTextSize(ONE_DIP * 22);
        paint.setColor(Color.WHITE);
        canvas.drawText("Touch the plus", metrics.widthPixels * .15f, metrics.heightPixels * .13f, paint);
        canvas.drawText("to add a Shuttle", metrics.widthPixels * .14f, metrics.heightPixels * .13f + paint.getTextSize() + 15, paint);
        canvas.drawText("Updates will be on", (metrics.widthPixels - paint.measureText("Updates will be on")) / 2.0f, metrics.heightPixels * .5f, paint);
        canvas.drawText("the homescreen widget", (metrics.widthPixels - paint.measureText("the homescreen widget")) / 2.0f, metrics.heightPixels * .5f + paint.getTextSize() + 15, paint);
        // draw a sample widget on the screen
        Bitmap stop = BitmapFactory.decodeResource(getResources(), R.drawable.widget_sample);
  //      canvas.drawBitmap(stop, metrics.widthPixels * .15f, metrics.heightPixels * .7f, paint );
        Rect src = new Rect(0, 0, stop.getWidth(), stop.getHeight());
        RectF dst = new RectF(metrics.widthPixels * .15f, metrics.heightPixels * .6f, metrics.widthPixels * .85f, metrics.heightPixels * .75f);
        canvas.drawBitmap(stop, src, dst, paint);
        // create an ImageView and draw it to the screen
        ImageView imageView = new ImageView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        params.format = PixelFormat.RGBA_8888;
        imageView.setImageBitmap(bitmap);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WindowManager manager = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
                manager.removeViewImmediate(view);
            }
        });
        windowManager.addView(imageView, params);

    }
}

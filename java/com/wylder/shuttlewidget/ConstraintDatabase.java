package com.wylder.shuttlewidget;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by kevin on 2/4/15.
 *
 * This is a class that wraps controls for accessing a database full of ScheduleConstraints
 * A context is needed because the database is stored in the application's /data folder
 *
 * methods are here for getting/writing/deleting constraints
 */
public class ConstraintDatabase extends SQLiteOpenHelper {

    private static final int VERSION_NUMBER = 4;

    private static final String DATABASE_NAME = "scheduleDatabase";
    private static final String TABLE_NAME = "constraints";
    private static final String COL_ROUTE_NUMBER = "RouteNumber";
    private static final String COL_STOP_NUMBER = "StopNumber";
    private static final String COL_START_HOUR = "StartHour";
    private static final String COL_END_HOUR = "EndHour";
    private static final String COL_DAY_1 = "Day1";
    private static final String COL_DAY_2 = "Day2";
    private static final String COL_DAY_3 = "Day3";
    private static final String COL_DAY_4 = "Day4";
    private static final String COL_DAY_5 = "Day5";
    private static final String COL_DAY_6 = "Day6";

    private static final String CREATE_COMMAND = "create table " + TABLE_NAME + " ( "
            + COL_ROUTE_NUMBER + " INTEGER, "
            + COL_STOP_NUMBER + " INTEGER, "
            + COL_START_HOUR + " INTEGER, "
            + COL_END_HOUR + " INTEGER, "
            + COL_DAY_1 + " INTEGER, "
            + COL_DAY_2 + " INTEGER, "
            + COL_DAY_3 + " INTEGER, "
            + COL_DAY_4 + " INTEGER, "
            + COL_DAY_5 + " INTEGER, "
            + COL_DAY_6 + " INTEGER);";

    private SQLiteDatabase database;    // saved to prevent costly creation methods

    public ConstraintDatabase(Context ctx){
        super(ctx, DATABASE_NAME, null, VERSION_NUMBER);
        database = getWritableDatabase();   // only use one instance of the database
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_COMMAND);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i2) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }

    public void closeDatabase(){
        database.close();
    }

    /**
     * A function to check if the ScheduleConstraint will fit into the database
     * it relies on ScheduleConstraint.hasOverlap to check each constraint in the
     * database
     *
     * @param constraint, the constraint to check
     * @return whether or not there is a conflict in the database
     */
    public boolean constraintConflict(ScheduleConstraint constraint){
        ScheduleConstraint[] constraints = getAllConstraints();
        for(int i = 0; i < constraints.length; i++){
            if(constraints[i].hasOverlap(constraint)){
                return true;
            }
        }
        return false;
    }

    /**
     * A method to add a ScheduleConstraint to the database
     *
     * @param constraint the constraint to add
     * @return whether or not there was success in adding it. this is because some things are implied
     * to be correct in the database, and it is worth it to check and not mess up the database while debugging
     */
    public boolean addConstraint(ScheduleConstraint constraint){
        // check if daysActive is filled out
        if(constraint.daysActive.length != ShuttleConstants.DAYS_OF_THE_WEEK){
            Log.e("KevinRuntime", "invalid constraint length " + constraint.daysActive.length);
            return false;
        }
        // check if in the hour range
        if(constraint.hourStart < ShuttleConstants.HOUR_START && constraint.hourEnd > ShuttleConstants.HOUR_END){
            return false;
        }
        String separator = ", ";
        StringBuilder query = new StringBuilder("INSERT INTO " + TABLE_NAME + " VALUES ( ");
        query.append(constraint.routeId);
        query.append(separator);
        query.append(constraint.stopId);
        query.append(separator);
        query.append(constraint.hourStart);
        query.append(separator);
        query.append(constraint.hourEnd);
        for(int i = 0; i < ShuttleConstants.DAYS_OF_THE_WEEK; i++){     // for each day in the week
            query.append(separator);
            if(constraint.daysActive[i]){
                query.append(1);    // SQLite doesn't have a boolean datatype
            }else{
                query.append(0);    // SQLite doesn't have a boolean datatype
            }
        }
        query.append(" );");
        database.execSQL(query.toString());
        return true;
    }

    /**
     * a method to get all the constraints in the database. It will return in the order added (i think)
     * @return an array of ScheduleConstraints
     */
    public ScheduleConstraint[] getAllConstraints(){
        Cursor cursor = database.rawQuery("Select * from " + TABLE_NAME, null);
        cursor.moveToFirst();
        ScheduleConstraint[] constraints = new ScheduleConstraint[cursor.getCount()];
        Log.e("KevinRuntime", "Getting all Constraints... there are " + constraints.length);
        for(int i = 0; i < constraints.length; i++){
            constraints[i] = getNextConstraint(cursor);     // use a helper method to read next Constraint in Cursor
        }
        return constraints;
    }

    /**
     * get the ScheduleConstraint that matches up to the current date/time, or null if there is no such ScheduleConstraint
     * @return the current Constraint, or null if none exists
     */
    public ScheduleConstraint getCurrentConstraint(){
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 2;    // Calendar uses Sunday as day 1, and I need Monday to be index 0
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(TABLE_NAME);
        query.append(" WHERE (");
        query.append(currentHour);
        query.append(" BETWEEN ");
        query.append(COL_START_HOUR);
        query.append(" AND ");
        query.append(COL_END_HOUR);
        query.append(") AND (");
        query.append(dayColumnSearch(currentDay));      // a helper method to get the day column
        query.append(" IS 1)");
        Cursor result = database.rawQuery(query.toString(), null);
        Log.e("KevinRuntime", "Query for getting the current constraint: " + query.toString());
        if(result.getCount() == 0){
            return null;
        }else{
            Log.e("KevinRuntime", "The number of stops that match the current time/date is " + result.getCount());
            result.moveToFirst();   // setup the cursor for the helper method
            return getNextConstraint(result);
        }
    }

    /**
     * remove a constraint from the database by finding it and deleting it
     * @param constraint the constraint to match and delete
     */
    public void removeConstraint(ScheduleConstraint constraint){
        StringBuilder builder = new StringBuilder("DELETE FROM ");  // SQL command to remove
        builder.append(TABLE_NAME);
        builder.append(" WHERE ");
        builder.append(COL_START_HOUR);
        builder.append(" = ");
        builder.append(constraint.hourStart);       // only one bound is needed. we assume no overlap in database (it was checked on input)
        builder.append(" AND ");
        for(int i = 0; i < ShuttleConstants.DAYS_OF_THE_WEEK; i++){
            if(constraint.daysActive[i]){
                builder.append(dayColumnSearch(i));
                builder.append(" = ");
                builder.append(1);
                break;      // only use the first day active, it is guaranteed to exist and no others will have the same time
            }
        }
        database.execSQL(builder.toString());
        Log.e("KevinRuntime", "Removing Constraint " + constraint.toString() + "SQL statement: " + builder.toString());
    }

    /**
     * A helper method for getting a table column name string for the integer day given
     * @param day an int to describe the current day (0 - Monday ; 5 - Saturday)
     * @return the string name for that day's column
     */
    private String dayColumnSearch(int day){
        switch (day){
            case 0:
                return COL_DAY_1;
            case 1:
                return COL_DAY_2;
            case 2:
                return COL_DAY_3;
            case 3:
                return COL_DAY_4;
            case 4:
                return COL_DAY_5;
            default:
                return COL_DAY_6;   // for safety
        }
    }

    /**
     * A helper method to get a ScheduleConstraint out of a Cursor
     * this method also increments the cursor
     * @param cursor the cursor to pull data from
     * @return the next ScheduleConstraint in the queue
     */
    private ScheduleConstraint getNextConstraint(Cursor cursor){
        boolean[] days = new boolean[ShuttleConstants.DAYS_OF_THE_WEEK];
        for(int j = 0; j < ShuttleConstants.DAYS_OF_THE_WEEK; j++){
            if(cursor.getInt(4 + j) == 1){      // the first 4 columns are for hours and stop/route
                days[j] = true;
            }else{
                days[j] = false;
            }
        }
        ScheduleConstraint ret = new ScheduleConstraint(days, cursor.getInt(2),
                cursor.getInt(3), cursor.getInt(0), cursor.getInt(1));
        cursor.moveToNext();
        return ret;
    }

}

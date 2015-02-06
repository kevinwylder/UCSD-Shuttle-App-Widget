package com.wylder.shuttlewidget;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by kevin on 2/4/15.
 */
public class ConstraintDatabase extends SQLiteOpenHelper {

    private static final int VERSION_NUMBER = 3;
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

    private SQLiteDatabase database;

    public ConstraintDatabase(Context ctx){
        super(ctx, DATABASE_NAME, null, VERSION_NUMBER);
        database = getWritableDatabase();
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

    public boolean constraintConflict(ScheduleConstraint constraint){
        ScheduleConstraint[] constraints = getAllConstraints();
        for(int i = 0; i < constraints.length; i++){
            if(constraints[i].hasOverlap(constraint)){
                return true;
            }
        }
        return false;
    }

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
        for(int i = 0; i < ShuttleConstants.DAYS_OF_THE_WEEK; i++){
            query.append(separator);
            if(constraint.daysActive[i]){
                query.append(1);
            }else{
                query.append(0);
            }
        }
        query.append(" );");
        database.execSQL(query.toString());
        return true;
    }

    public ScheduleConstraint[] getAllConstraints(){
        Cursor cursor = database.rawQuery("Select * from " + TABLE_NAME, null);
        cursor.moveToFirst();
        ScheduleConstraint[] constraints = new ScheduleConstraint[cursor.getCount()];
        Log.e("KevinRuntime", "Getting all Constraints... there are " + constraints.length);
        for(int i = 0; i < constraints.length; i++){
            constraints[i] = getNextConstraint(cursor);
            Log.e("KevinRuntime", "cursor index: " + i);
            if(cursor.isAfterLast()){
                Log.e("KevinRuntime", "cursor position past last in cursor, aborting. if " + i + " = " + constraints.length + ", everything's good");
                break;
            }
        }
        return constraints;
    }

    public ScheduleConstraint getCurrentConstraint(){
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentDay = calendar.get(Calendar.DAY_OF_WEEK) - 2;
        StringBuilder query = new StringBuilder("SELECT * FROM ");
        query.append(TABLE_NAME);
        query.append(" WHERE (");
        query.append(currentHour);
        query.append(" BETWEEN ");
        query.append(COL_START_HOUR);
        query.append(" AND ");
        query.append(COL_END_HOUR);
        query.append(") AND (");
        query.append(dayColumnSearch(currentDay));
        query.append(" IS 1)");
        Cursor result = database.rawQuery(query.toString(), null);
        Log.e("KevinRuntime", "Query for getting the current constraint: " + query.toString());
        if(result.getCount() == 0){
            return null;
        }else{
            Log.e("KevinRuntime", "The number of stops that match the current time/date is " + result.getCount());
            result.moveToFirst();
            return getNextConstraint(result);
        }
    }

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
                return COL_DAY_6;
        }
    }

    private ScheduleConstraint getNextConstraint(Cursor cursor){
        boolean[] days = new boolean[ShuttleConstants.DAYS_OF_THE_WEEK];
        for(int j = 0; j < ShuttleConstants.DAYS_OF_THE_WEEK; j++){
            if(cursor.getInt(4 + j) == 1){
                days[j] = true;
            }else{
                days[j] = false;
            }
        }
        ScheduleConstraint ret =
                new ScheduleConstraint(days, cursor.getInt(2), cursor.getInt(3), cursor.getInt(0), cursor.getInt(1));
        cursor.moveToNext();
        return ret;
    }
}

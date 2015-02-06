package com.wylder.shuttlewidget;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by kevin on 2/4/15.
 */
public class ConstraintDatabase extends SQLiteOpenHelper {

    private static final int VERSION_NUMBER = 2;
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

    public void addConstraint(ScheduleConstraint constraint){
        if(constraint.daysActive.length != ShuttleConstants.DAYS_OF_THE_WEEK){
            Log.e("AndroidRuntime", "invalid constraint length " + constraint.daysActive.length);
            return;
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
    }

    public ScheduleConstraint[] getAllConstraints(){
        Cursor cursor = database.rawQuery("Select * from " + TABLE_NAME, null);
        cursor.moveToFirst();
        ScheduleConstraint[] constraints = new ScheduleConstraint[cursor.getCount()];
        Log.e("KevinRuntime", "" + constraints.length);
        for(int i = 0; i < constraints.length; i++){
            boolean[] days = new boolean[ShuttleConstants.DAYS_OF_THE_WEEK];
            for(int j = 0; j < ShuttleConstants.DAYS_OF_THE_WEEK; j++){
                if(cursor.getInt(4 + j) == 1){
                    days[j] = true;
                }else{
                    days[j] = false;
                }
            }
            constraints[i] = new ScheduleConstraint(days, cursor.getInt(2), cursor.getInt(3), cursor.getInt(0), cursor.getInt(1));
            cursor.moveToNext();
            Log.e("KevinRuntime", "cursor index: " + i);
            if(cursor.isAfterLast()){
                Log.e("KevinRuntime", "cursor past last");
                break;
            }
        }
        return constraints;
    }
}

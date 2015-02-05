package com.wylder.shuttlewidget;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Currency;

/**
 * Created by kevin on 2/4/15.
 */
public class ConstraintDatabase extends SQLiteOpenHelper {

    private static final int VERSION_NUMBER = 1;
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
        database.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        onCreate(database);
    }

    public boolean constraintConflict(ScheduleConstraint constraint){
        ScheduleConstraint[] constraints = getAllConstraints();
        for(int i = 0; i < constraints.length; i++){
            if(constraints[i].hasOverlap(constraint)){
                return false;
            }
        }
        return true;
    }

    public void addConstraint(ScheduleConstraint constraint){
        String separator = ", ";
        StringBuilder query = new StringBuilder("INSERT INTO " + TABLE_NAME + " VALUES ( ");
        query.append(constraint.routeId);
        query.append(separator);
        query.append(constraint.stopId);
        query.append(separator);
        query.append(constraint.hourStart);
        query.append(separator);
        query.append(constraint.hourEnd);
        for(int i = 0; i < constraint.daysActive.length; i++){
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
        Cursor cursor = database.rawQuery("Select * from ?", new String[]{TABLE_NAME});
        cursor.moveToFirst();
        ScheduleConstraint[] constraints = new ScheduleConstraint[cursor.getCount()];
        for(int i = 0; i < constraints.length; i++){
            cursor.move(i);
            boolean[] days = new boolean[6];
            for(int j = 0; j < 6; j++){
                if(cursor.getInt(4 + j) == 1){
                    days[j] = true;
                }else{
                    days[j] = false;
                }
            }
            constraints[i] = new ScheduleConstraint(days, cursor.getInt(2), cursor.getInt(3), cursor.getInt(0), cursor.getInt(1));
        }
        return constraints;
    }
}

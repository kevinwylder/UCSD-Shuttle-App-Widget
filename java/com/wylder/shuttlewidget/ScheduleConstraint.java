package com.wylder.shuttlewidget;

import android.content.Intent;

/**
 * Created by kevin on 2/2/15.
 */
public class ScheduleConstraint {

    public static final int LEGAL_CONSTRAINT = 1;
    public static final int NO_DAYS_SELECTED = 2;
    public static final int BAD_TIME_RANGE = 3;
    public static final int EMPTY_CONSTRAINT = 4;

    private static final String DAYS_ACTIVE = "daysactive";
    private static final String HOUR_START = "hourstart";
    private static final String HOUR_END = "hourend";
    private static final String ROUTE_ID = "routeid";
    private static final String STOP_ID = "stopid";

    public boolean[] daysActive = new boolean[0];
    public int hourStart;
    public int hourEnd;
    public int routeId;
    public int stopId;

    public ScheduleConstraint(Intent intent){
        this.daysActive = intent.getBooleanArrayExtra(DAYS_ACTIVE);
        this.hourStart = intent.getIntExtra(HOUR_START, 0);
        this.hourEnd = intent.getIntExtra(HOUR_END, 0);
        this.routeId = intent.getIntExtra(ROUTE_ID, 0);
        this.stopId = intent.getIntExtra(STOP_ID, 0);
        if(daysActive == null){
            daysActive = new boolean[0];
        }
    }

    public ScheduleConstraint(boolean[] daysActive, int hourStart,
                              int hourEnd, int routeId, int stopId){
        this.daysActive = daysActive;
        this.hourStart = hourStart;
        this.hourEnd = hourEnd;
        this.routeId = routeId;
        this.stopId = stopId;
    }

    @Override
    public String toString(){
        return getBusName(routeId, stopId) + " from " + getTimeString(hourStart)  + " to " + getTimeString(hourEnd);
    }

    public int legality(){
        if(daysActive.length == 0){
            return EMPTY_CONSTRAINT;
        }
        if(hourStart >= hourEnd){
            return BAD_TIME_RANGE;
        }
        if(hourStart < ShuttleConstants.HOUR_START | hourEnd > ShuttleConstants.HOUR_END){
            return BAD_TIME_RANGE;
        }
        boolean hasDay = false;
        for(int i = 0; i < ShuttleConstants.DAYS_OF_THE_WEEK; i++){
            if(daysActive[i]){
                hasDay = true;
                i = ShuttleConstants.DAYS_OF_THE_WEEK;
            }
        }
        if(!hasDay){
            return NO_DAYS_SELECTED;
        }else {
            return LEGAL_CONSTRAINT;
        }
    }

    public Intent setConstraintInfo(Intent intent){
        intent.putExtra(DAYS_ACTIVE, daysActive);
        intent.putExtra(HOUR_START, hourStart);
        intent.putExtra(HOUR_END, hourEnd);
        intent.putExtra(ROUTE_ID, routeId);
        intent.putExtra(STOP_ID, stopId);
        return intent;
    }

    public String getRouteName(){
        return ShuttleConstants.routes[routeId];
    }

    public String getStopName(){
        return ShuttleConstants.stops[stopId];
    }

    public boolean hasOverlap(ScheduleConstraint constraint){
        if(constraint.hourStart >= this.hourEnd || constraint.hourEnd < this.hourStart){
            return false;
        }else{
            return true;
        }
    }

    public static String getBusName(int routeId, int stopId){
        return ShuttleConstants.routes[routeId] + ", " + ShuttleConstants.stops[stopId] + " stop";
    }

    public static String getTimeString(int hour){
        if(hour <= 12){
            return hour + ":00 am";
        }else{
            return hour - 12 + ":00 pm";
        }
    };
}

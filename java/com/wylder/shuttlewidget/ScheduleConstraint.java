package com.wylder.shuttlewidget;

import android.content.Intent;

/**
 * Created by kevin on 2/2/15.
 */
public class ScheduleConstraint {

    public static final String[] routes = new String[]{
            "Counter Campus Loop",
            "Clockwise Campus Loop"
    };

    public static final String[] stops = new String[]{
            "Torrey Pines Center",
            "North Point",
            "Hopkins Parking Structure",
            "Warren Apartments",
            "Canyonview Pool",
            "Pepper Canyon",
            "Gilman & Myers",
            "Gilman & Mandelville",
            "Gilman & Osler",
            "Che Café",
            "Revelle Parking",
            "Pacific Hall",
            "Muir Apartments",
            "Peterson Hall",
            "Pangea Parking Structure",
            "Eleanor Roosevelt College"
    };

    public static final int LEGAL_CONSTRAINT = 1;
    public static final int NO_DAYS_SELECTED = 2;
    public static final int BAD_TIME_RANGE = 3;
    public static final int EMPTY_CONSTRAINT = 4;

    private static final String DAYS_ACTIVE = "daysactive";
    private static final String HOUR_START = "hourstart";
    private static final String MINUTE_START = "minutestart";
    private static final String HOUR_END = "hourend";
    private static final String MINUTE_END = "minuteend";
    private static final String ROUTE_ID = "routeid";
    private static final String STOP_ID = "stopid";

    private boolean[] daysActive;
    private int hourStart;
    private int minuteStart;
    private int hourEnd;
    private int minuteEnd;
    private int routeId;
    private int stopId;

    public ScheduleConstraint(Intent intent){
        this.daysActive = intent.getBooleanArrayExtra(DAYS_ACTIVE);
        this.hourStart = intent.getIntExtra(HOUR_START, 0);
        this.minuteStart = intent.getIntExtra(MINUTE_START, 0);
        this.hourEnd = intent.getIntExtra(HOUR_END, 0);
        this.minuteEnd = intent.getIntExtra(MINUTE_END, 0);
        this.routeId = intent.getIntExtra(ROUTE_ID, 0);
        this.stopId = intent.getIntExtra(STOP_ID, 0);
    }

    public ScheduleConstraint(boolean[] daysActive, int hourStart, int minuteStart,
                              int hourEnd, int minuteEnd, int routeId, int stopId){
        this.daysActive = daysActive;
        this.hourStart = hourStart;
        this.minuteStart = minuteStart;
        this.hourEnd = hourEnd;
        this.minuteEnd = minuteEnd;
        this.routeId = routeId;
        this.stopId = stopId;
    }

    @Override
    public String toString(){
        return getBusName(routeId, stopId) + " from " + hourStart + ":" + minuteStart * 15 +
               " to " + hourEnd + ":" + minuteEnd * 15;
    }

    public int legality(){
        if(daysActive == null){
            return EMPTY_CONSTRAINT;
        }
        if(hourStart > hourEnd){
            return BAD_TIME_RANGE;
        }else if(hourStart == hourEnd){
            if(minuteStart > minuteEnd){
                return  BAD_TIME_RANGE;
            }
        }
        boolean hasDay = false;
        for(int i = 0; i < 7; i++){
            if(daysActive[i]){
                hasDay = true;
                i = 7;
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
        intent.putExtra(MINUTE_START, minuteStart);
        intent.putExtra(HOUR_END, hourEnd);
        intent.putExtra(MINUTE_END, minuteEnd);
        intent.putExtra(ROUTE_ID, routeId);
        intent.putExtra(STOP_ID, stopId);
        return intent;
    }

    public static String getBusName(int routeId, int stopId){
        return routes[routeId] + ", " + stops[stopId] + " stop";
    }
}

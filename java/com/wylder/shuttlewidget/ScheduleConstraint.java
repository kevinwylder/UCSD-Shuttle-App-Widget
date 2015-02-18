package com.wylder.shuttlewidget;

import android.content.Intent;

/**
 * Created by kevin on 2/2/15.
 *
 * Class to hold information about when the widget should display a stop
 * it holds the start/end time, what stop/route, and the days of the week the constraint is active
 */
public class ScheduleConstraint {

    // constants for the legality of a constraint. returned in legality()
    public static final int LEGAL_CONSTRAINT = 1;
    public static final int NO_DAYS_SELECTED = 2;
    public static final int BAD_TIME_RANGE = 3;
    public static final int SHUTTLE_NOT_RUNNING = 4;
    public static final int EMPTY_CONSTRAINT = 5;

    // constants Strings that will be used to write the ScheduleConstraint to an Intent
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

    /**
     * A constructor that will build a ScheduleConstraint from an intent that has been encoded with
     * setConstraintInfo method
     * @param intent the intent that data will be pulled from
     */
    public ScheduleConstraint(Intent intent){
        this.daysActive = intent.getBooleanArrayExtra(DAYS_ACTIVE);
        this.hourStart = intent.getIntExtra(HOUR_START, 0);
        this.hourEnd = intent.getIntExtra(HOUR_END, 0);
        this.routeId = intent.getIntExtra(ROUTE_ID, 0);
        this.stopId = intent.getIntExtra(STOP_ID, 0);
        // never have a null array, even if the constraint is empty from the given Intent
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

    /**
     * A method to encode this ScheduleConstraint's data into an Intent
     * @param intent the intent that data will put into
     * @return the encoded intent, for convenience
     */
    public Intent setConstraintInfo(Intent intent){
        intent.putExtra(DAYS_ACTIVE, daysActive);
        intent.putExtra(HOUR_START, hourStart);
        intent.putExtra(HOUR_END, hourEnd);
        intent.putExtra(ROUTE_ID, routeId);
        intent.putExtra(STOP_ID, stopId);
        return intent;
    }

    /**
     * Check if a given ScheduleConstraint can cooperate with this one
     * @param constraint the constraint to check
     * @return a boolean representing whether there's overlap
     */
    public boolean hasOverlap(ScheduleConstraint constraint){
        // check if the hours overlap
        if(constraint.hourStart >= this.hourEnd || constraint.hourEnd <= this.hourStart){
            return false;   // hours don't overlap
        }else{
            // the hours do overlap, but do the days of the week?
            for(int i = 0; i < ShuttleConstants.DAYS_OF_THE_WEEK; i++){
                if(constraint.daysActive[i] && this.daysActive[i]){
                    return true;    // hours and days overlap
                }
            }
            return false;   // hours overlap, but days don't overlap
        }
    }

    /**
     * a method to check if the constructed ScheduleConstraint is allowed to exist
     * tests for emptiness, ending before start, being in range of shuttle operation, and having days to operate
     * @return an integer representing the legality of the constraint
     */
    public int legality(){
        if(daysActive.length == 0){
            return EMPTY_CONSTRAINT;
        }
        if(hourStart >= hourEnd){
            return BAD_TIME_RANGE;       // ends before start
        }
        if(hourStart < ShuttleConstants.HOUR_START | hourEnd > ShuttleConstants.HOUR_END){
            return SHUTTLE_NOT_RUNNING;      // operates outside shuttle times
        }
        boolean hasDay = false;
        // loop through daysActive and find if there is a single day selected
        for(int i = 0; i < ShuttleConstants.DAYS_OF_THE_WEEK; i++){
            if(daysActive[i]){
                hasDay = true;           // a day is selected, we only need 1
                break;
            }
        }
        if(!hasDay){
            return NO_DAYS_SELECTED;    // nothing in daysActive true
        }else {
            return LEGAL_CONSTRAINT;    // passed all tests
        }
    }

    // getter methods for String representations of different aspects.

    public String getRouteName(){
        return ShuttleConstants.routeNames[routeId];
    }

    public String getStopName(){
        return ShuttleConstants.stopNames[routeId][stopId];
    }

    public int getTextColor(){
        return ShuttleConstants.secondaryColors[routeId];
    }

    public int getBackgroundColor(){
        return ShuttleConstants.primaryColors[routeId];
    }

    /**
     * returns a readable String that displays the time range example inside parentheses (10:00am - 1:00pm)
     */
    public String getTimeRangeString(){
        return getTimeString(hourStart) + " - " + getTimeString(hourEnd);
    }

    /**
     * returns an integer color that corresponds to the route
     */
    public int getWidgetColor(){
        return ShuttleConstants.primaryColors[routeId];
    }

    /**
     * a static method that turns any int into a time
     * it doesn't check for hours > 24 but that's no problem
     * @param hour a 24 hour representation of the time to parse into am/pm
     * @return a String for the hour
     */
    public static String getTimeString(int hour){
        if(hour <= 12){
            return hour + ":00 am";
        }else{
            return hour - 12 + ":00 pm";
        }
    }

    @Override
    public String toString(){
        return getRouteName() + ", " + getStopName() + " from " + getTimeRangeString();
    }
}

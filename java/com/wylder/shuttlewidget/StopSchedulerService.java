package com.wylder.shuttlewidget;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by kevin on 2/1/15
 *
 * This is an asynchronous service that handles requests via intents. It does all the big background
 * requests and sends the result in the form of an Intent to the Widget. The main accessor of this
 * Service is ShuttleWidgetProvider because it needs to do long running tasks like web requests and location tracking
 */
public class StopSchedulerService extends IntentService {

    private static final String SERVICE_NAME = "Get stop info service";

    public static final String BROADCAST_UPDATE_ACTION = "com.wylder.shuttlewidget.HAS_NEW_SHIT";

    // Strings to use when dealing with data in an Intent
    public static final String STOP_NAME = "stop name";
    public static final String STOP_TIME = "stop time";
    public static final String UPDATE_TYPE = "update type";

    // codes for type of request sent to this Service
    public static final int UPDATE_STOP = 10;
    public static final int UPDATE_ARRIVAL = 20;

    public StopSchedulerService(){
        this(SERVICE_NAME);
    }

    public StopSchedulerService(String serviceName){
        super(serviceName);
    }

    /**
     * This method as a part of IntentService handles intents sent to the service in a separate thread
     * this is very useful because database operations, HTTP connections, and potential Location services
     * are needed to find the stop/route time
     * @param intent an intent that asks for either Stop updates, or Time updates
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        int type = intent.getIntExtra(UPDATE_TYPE, UPDATE_STOP);    // get the type of request
        Intent responseIntent = new Intent(BROADCAST_UPDATE_ACTION);
        ConstraintDatabase database = new ConstraintDatabase(this);
        ScheduleConstraint currentConstraint = database.getCurrentConstraint();
        if(type == UPDATE_STOP){
            if(currentConstraint != null) {
                // use the constraint the database found for the current time
                responseIntent.putExtra(STOP_NAME, currentConstraint.getStopName());
            }else{
                // the database has no constraint at this time
                responseIntent.putExtra(STOP_NAME, "Current Location");
            }
            // tell the user to click and update the stop time
            responseIntent.putExtra(STOP_TIME, "Update");
        }else{
            responseIntent.putExtra(STOP_NAME, "");
            responseIntent.putExtra(STOP_TIME, "updating time");
        }
        this.sendBroadcast(responseIntent);
    }
}

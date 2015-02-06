package com.wylder.shuttlewidget;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * Created by kevin on 2/1/15
 */
public class StopSchedulerService extends IntentService {

    private static final String SERVICE_NAME = "Get stop info service";

    public static final String BROADCAST_UPDATE_ACTION = "com.wylder.shuttlewidget.HAS_NEW_SHIT";

    public static final String STOP_NAME = "stop name";
    public static final String STOP_TIME = "stop time";

    public static final String UPDATE_TYPE = "update type";
    public static final int UPDATE_STOP = 10;
    public static final int UPDATE_ARRIVAL = 20;

    public StopSchedulerService(){
        this(SERVICE_NAME);
    }

    public StopSchedulerService(String serviceName){
        super(serviceName);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int type = intent.getIntExtra(UPDATE_TYPE, UPDATE_STOP);
        Intent responseIntent = new Intent(BROADCAST_UPDATE_ACTION);
        ConstraintDatabase database = new ConstraintDatabase(this);
        ScheduleConstraint currentConstraint = database.getCurrentConstraint();
        Log.e("KevinRuntime", currentConstraint.toString());
        if(type == UPDATE_STOP){
            responseIntent.putExtra(STOP_NAME, currentConstraint.getStopName());
            responseIntent.putExtra(STOP_TIME, "Update");
        }else{
            responseIntent.putExtra(STOP_NAME, "");
            responseIntent.putExtra(STOP_TIME, "updating time");
        }
        this.sendBroadcast(responseIntent);
    }
}

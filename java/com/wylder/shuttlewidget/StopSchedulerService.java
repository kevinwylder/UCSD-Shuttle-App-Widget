package com.wylder.shuttlewidget;

import android.app.IntentService;
import android.content.Intent;

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
        super(SERVICE_NAME);
    }

    public StopSchedulerService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int type = intent.getIntExtra(UPDATE_TYPE, UPDATE_STOP);
        Intent responseIntent = new Intent(BROADCAST_UPDATE_ACTION);
        if(type == UPDATE_STOP){
            responseIntent.putExtra(STOP_NAME, "updating stop");
            responseIntent.putExtra(STOP_TIME, "unchanged");
        }else{
            responseIntent.putExtra(STOP_NAME, "unchanged");
            responseIntent.putExtra(STOP_TIME, "updating time");
        }
        this.sendBroadcast(responseIntent);
    }
}

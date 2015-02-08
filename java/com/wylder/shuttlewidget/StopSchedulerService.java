package com.wylder.shuttlewidget;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

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

    private static final int TIMEOUT_MILLIS = 10000;
    private static final int BUFFER_SIZE = 1000;

    // Strings to use when dealing with data in an Intent
    public static final String STOP_NAME = "stop name";
    public static final String STOP_TIME = "stop time";
    public static final String WIDGET_COLOR = "widget color";
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
                responseIntent.putExtra(WIDGET_COLOR, currentConstraint.getWidgetColor());
            }else{
                // the database has no constraint at this time
                responseIntent.putExtra(WIDGET_COLOR, Color.WHITE);
                responseIntent.putExtra(STOP_NAME, "Current Location");
            }
            // tell the user to click and update the stop time
            responseIntent.putExtra(STOP_TIME, "Update");
        }else{
            if(currentConstraint == null){
                // get the stop via location services... we'll do this later
                // in the meantime, the default is CanyonView Pool Counter Clockwise
                currentConstraint = new ScheduleConstraint(new boolean[ShuttleConstants.DAYS_OF_THE_WEEK],
                        ShuttleConstants.HOUR_START, ShuttleConstants.HOUR_END,
                        0, 4);      // 0 and 4 represent Counter and Canyon View respectively
            }
            // do the internet request in helper method
            responseIntent.putExtra(STOP_NAME, currentConstraint.getStopName());
            responseIntent.putExtra(STOP_TIME, getStopTime(currentConstraint));
            responseIntent.putExtra(WIDGET_COLOR, currentConstraint.getWidgetColor());
        }
        this.sendBroadcast(responseIntent);
        database.closeDatabase();
    }

    /**
     * A helper method that will get the arrival time of a given constraint
     * @param constraint
     * @return
     */
    private String getStopTime(ScheduleConstraint constraint){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo == null || !networkInfo.isConnected()){
            return "No Internet";
        }
        int onlineRouteNumber;
        if(constraint.routeId == 0){
            onlineRouteNumber = 1113;
        }else{
            onlineRouteNumber = 1114;
        }
        String stopListUrl = "http://www.ucsdbus.com/m/routes/" + onlineRouteNumber + "/stops/";
        String[] onlineStopIds = getUrlSplit(stopListUrl, "<a href=\"/m/routes/" + onlineRouteNumber + "/stops/", "\">");
        String stopArrivalUrl = stopListUrl + onlineStopIds[constraint.stopId];
        String[] arrivalTime = getUrlSplit(stopArrivalUrl, "<strong>", "</strong>");
        if(arrivalTime[0].contains("min")) {
            return arrivalTime[0];
        }else{
            return "Unavailable";
        }
    }

    /**
     * A helper method that a webpage and returns an array of Strings between two regex splits. usage is
     * to extract information from a webpage between tags split1 and split2
     * @param url the url to request from
     * @param split1 the beginning of the splits
     * @param split2 the end of the split
     * @return an array of Strings between split1 and split2
     */
    private String[] getUrlSplit(String url, String split1, String split2){
        InputStream inputStream = null;
        try{
            URL urlObject = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
            connection.setReadTimeout(TIMEOUT_MILLIS * 2);
            connection.setConnectTimeout(TIMEOUT_MILLIS);
            connection.setRequestMethod("GET");
            connection.connect();
            // if the response isn't ok, throw an exception
            int response = connection.getResponseCode();
            if(response != HttpURLConnection.HTTP_OK){
                throw new Exception("response not 200");
            }
            // get and read the InputStream into a StringBuilder using InputStreamReader
            inputStream = connection.getInputStream();
            Reader inputStreamReader = new InputStreamReader(inputStream);
            char[] buffer = new char[BUFFER_SIZE];
            StringBuilder source = new StringBuilder(16468);
            while(true){
                int charsRead = inputStreamReader.read(buffer, 0, BUFFER_SIZE);
                source.append(buffer);
                if(charsRead < 1){
                    break;
                }
            }
            // close connection and cleanup InputStreams
            connection.disconnect();
            inputStream.close();
            inputStreamReader.close();
            // split the source using split1
            String[] splitArray = source.toString().split(split1);
            String[] finalArray = new String[splitArray.length - 1];
            for(int i = 1; i < splitArray.length; i++){     // start at 1 because the first split is useless
                // replace each element of the split with the first part of split2
                finalArray[i - 1] = splitArray[i].split(split2)[0];
            }
            return finalArray;
        }catch (Exception exception){
            return null;
        }
    }
}

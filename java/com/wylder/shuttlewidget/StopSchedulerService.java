package com.wylder.shuttlewidget;

import android.app.IntentService;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by kevin on 2/1/15
 *
 * This is an asynchronous service that handles requests via intents. It does all the big background
 * requests and sends the result in the form of an Intent to the Widget. The main accessor of this
 * Service is ShuttleWidgetProvider because it needs to do long running tasks like web requests and location tracking
 */
public class StopSchedulerService extends IntentService {

    private static final String SERVICE_NAME = "Get stop info service";

    public static final String BROADCAST_WIDGET_UPDATE = "com.wylder.shuttlewidget.HAS_NEW_SHIT";
    public static final String BROADCAST_SEARCH_RESULT = "com.wylder.shuttlewidget.result";

    // constants for the network requests
    private static final int TIMEOUT_MILLIS = 10000;
    private static final int BUFFER_SIZE = 1000;

    // Strings to use when dealing with data in an Intent
    public static final String STOP_NAME = "stop name";
    public static final String STOP_TIME = "stop time";
    public static final String TEXT_COLOR = "text color";
    public static final String BG_COLOR = "background color";
    public static final String CREATE_STOP_FLAG = "nostop";
    public static final String UPDATE_TYPE = "update type";
    public static final String ROUTE_ID = "routeId";
    public static final String STOP_ID = "stopid";

    // codes for type of request sent to this Service
    public static final int UPDATE_STOP = 10;
    public static final int UPDATE_ARRIVAL = 20;
    public static final int GET_ARRIVAL_TIME = 30;

    public StopSchedulerService(){
        this(SERVICE_NAME);
    }

    public StopSchedulerService(String serviceName){
        super(serviceName);
    }

    /**
     * This method as a part of IntentService handles intents sent to the service in a separate thread
     * this is very useful because database operations and HTTP connections are needed to find the stop/route time
     * @param intent an intent that asks for either Stop updates, or Time updates
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        int type = intent.getIntExtra(UPDATE_TYPE, UPDATE_STOP);    // get the type of request
        if(type == GET_ARRIVAL_TIME){
            Log.e("KevinRuntime", "message received");
            // create a new response Intent to send back to the fragment
            Intent responseIntent = new Intent(BROADCAST_SEARCH_RESULT);
            // create a constraint that holds the route and stop
            ScheduleConstraint artificialConstraint = new ScheduleConstraint(null, 0, 0,
                    intent.getIntExtra(ROUTE_ID, 0), intent.getIntExtra(STOP_ID, 0)     );
            // put the response time in the intent
            responseIntent.putExtra(STOP_TIME, getStopTime(artificialConstraint));
            // resend the intent
            sendBroadcast(responseIntent);
            // stop the function
            return;
        }
        Intent responseIntent = new Intent(BROADCAST_WIDGET_UPDATE);
        ConstraintDatabase database = new ConstraintDatabase(this);
        ScheduleConstraint currentConstraint = database.getCurrentConstraint();
        if(currentConstraint == null){
            // the database has no constraint at this time
            // check if we can add a constraint right now
            Calendar calendar = Calendar.getInstance();
            if(     calendar.get(Calendar.DAY_OF_WEEK) - 2 != -1                        // if not Sunday,
                 && calendar.get(Calendar.HOUR_OF_DAY) >= ShuttleConstants.HOUR_START    // after start hour,
                 && calendar.get(Calendar.HOUR_OF_DAY) <= ShuttleConstants.HOUR_END      // and before end hour
                ){
                responseIntent.putExtra(STOP_TIME, "No Stop");
                responseIntent.putExtra(STOP_NAME, "Touch to add Stop");
                responseIntent.putExtra(CREATE_STOP_FLAG, true);
            }else{
                responseIntent.putExtra(STOP_TIME, "Unavailable");
                responseIntent.putExtra(STOP_NAME, "No Shuttles Running");
            }
            responseIntent.putExtra(TEXT_COLOR, ShuttleConstants.textColors[ShuttleConstants.textColors.length - 1]);
            responseIntent.putExtra(BG_COLOR, ShuttleConstants.widgetColors[ShuttleConstants.widgetColors.length - 1]);
        }else if(type == UPDATE_STOP){
            // the widget asked for an update the current stop. don't worry about arrival time because
            // the user probably isn't there.
            responseIntent.putExtra(STOP_NAME, currentConstraint.getStopName());
            responseIntent.putExtra(TEXT_COLOR, currentConstraint.getTextColor());
            responseIntent.putExtra(BG_COLOR, currentConstraint.getBackgroundColor());
            responseIntent.putExtra(STOP_TIME, "Update");
        }else{
            // the user tapped the update button and now we need to get the time till arrival.
            String stopTime = getStopTime(currentConstraint);
            responseIntent.putExtra(STOP_NAME, currentConstraint.getStopName());
            responseIntent.putExtra(STOP_TIME, stopTime);
            responseIntent.putExtra(TEXT_COLOR, currentConstraint.getTextColor());
            responseIntent.putExtra(BG_COLOR, currentConstraint.getBackgroundColor());
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
        String url = "http://www.ucsdbus.com/m/routes/" + ShuttleConstants.onlineRouteIds[constraint.routeId]
                + "/stops/" + ShuttleConstants.onlineStopIds[constraint.routeId][constraint.stopId];
        String[] arrivalTime = getUrlSplit(url, "<strong>", "</strong>");
        if(arrivalTime == null || arrivalTime.length < 1){
            return "Error";
        }else if(arrivalTime[0].contains("min")) {
            String ret = arrivalTime[0].substring(3) + "utes";
            if(ret.equals("1 minutes")){    // special case
                ret = ret.substring(0, 8);  // cut off the s
            }
            return ret;
        }else if(arrivalTime[0].contains("arriving")){
            return "Arriving";
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

package com.wylder.shuttlewidget;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by kevin on 2/15/15.
 *
 * This is an activity that shows a Map with the current shuttle position on it. The activity takes
 * an int of ShuttleConstant's selected stop. It polls ucsdbus.com to get the current shuttle position.
 */
public class LiveMapActivity extends Activity implements OnMapReadyCallback{

    private static final int UPDATE_INTERVAL = 3;
    private static final int TIMEOUT_MILLIS = 5000;
    private static final int BUFFER_SIZE = 100;
    public static final String EXTRA_ROUTE_ID = "ROUTEid";  // the selected stop in the intent


    private MapFragment mapView;
    private GoogleMap drawingMap;
    private int routeSelectedIndex;
    private String lookupURL;
    private int pathColor;
    private int stopColor;
    private ArrayList<Marker> shuttles = new ArrayList<Marker>();

    // variables to handle the update interval
    private boolean running = true;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            new GetShuttlePosition().execute();
            if (running){
                handler.postDelayed(runnable, UPDATE_INTERVAL * 1000);
            }
        }
    };

    @Override
    public void onCreate(Bundle sis){
        super.onCreate(sis);
        // get the selected map to view
        routeSelectedIndex = getIntent().getIntExtra(EXTRA_ROUTE_ID, 0);
        // set map drawing element colors
        pathColor = ShuttleConstants.primaryColors[routeSelectedIndex];
        stopColor = ShuttleConstants.secondaryColors[routeSelectedIndex];
        // form the url
        lookupURL = "http://www.ucsdbus.com/Route/" + ShuttleConstants.onlineRouteIds[routeSelectedIndex] + "/Vehicles";

        // setup the map
        setContentView(R.layout.map_activity);
        mapView = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapView.getMapAsync(this);
    }

    /**
     * called when a GoogleMap object is ready to use to draw on the map.
     * @param googleMap the object that will draw on the MapFragment
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // create the BitmapDescriptor
        float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        Bitmap bitmap = Bitmap.createBitmap((int) (radius * 2), (int) (radius * 2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(stopColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setColor(pathColor);
        paint.setStyle(Paint.Style.STROKE);
        float offset = radius / 5.0f;
        paint.setStrokeWidth(offset);
        canvas.drawCircle(radius, radius, radius - offset, paint);
        BitmapDescriptor shuttleIcon = BitmapDescriptorFactory.fromBitmap(bitmap);
        // allow access to draw at a later time
        drawingMap = googleMap;
        //  Draw the path of the route
        LatLng[] routeCoords = ShuttleConstants.routePathCoordinates[routeSelectedIndex];
        PolylineOptions options = new PolylineOptions();
        for(int i = 0; i < routeCoords.length; i++){
            options.add(routeCoords[i]);
        }
        options.color(pathColor);
        googleMap.addPolyline(options);
        // draw the stops
        LatLng[] stopsCoords = ShuttleConstants.routeStopCoordinates[routeSelectedIndex];
        for(int i = 0; i < stopsCoords.length; i++){
            MarkerOptions stopMarkerOptions = new MarkerOptions();
            stopMarkerOptions.position(stopsCoords[i]);
            stopMarkerOptions.title(ShuttleConstants.stopNames[routeSelectedIndex][i]);
            stopMarkerOptions.icon(shuttleIcon);
            googleMap.addMarker(stopMarkerOptions);
        }
        // start the update shuttle interval
        handler.post(runnable);
    }

    /**
     * This class will get the shuttle's GPS coordinates and use them to update the UI
     */
    class GetShuttlePosition extends AsyncTask<Void, Void, String>{

        /**
         * Get the shuttles location over the internet
         * @return a JSON string of bus info
         */
        @Override
        protected String doInBackground(Void... voids) {
            try{
                // create a URL location to access using an internet connection
                URL urlObject = new URL(lookupURL);
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
                InputStream inputStream = connection.getInputStream();
                Reader inputStreamReader = new InputStreamReader(inputStream);
                char[] buffer = new char[BUFFER_SIZE];
                StringBuilder source = new StringBuilder();
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

                return source.toString();
            }catch (NullPointerException exception){
                Log.e("AndroidRuntime", "Null Pointer Exception: ");
            } catch (ProtocolException e) {
                Log.e("AndroidRuntime", "protocol error");
            } catch (MalformedURLException e) {
                Log.e("AndroidRuntime", "uri parse error");
            } catch (IOException e) {
                Log.e("AndroidRuntime", "io read error");
            } catch (Exception e){
                Log.e("AndroidRuntime", "response not 200");
            }
            return null;
        }

        /**
         * Use the info to update the map
         * @param source the JSON string from the background method
         */
        @Override
        protected void onPostExecute(String source){
            if(source == null){
                // something bad happened, we quit.
                return;
            }
            try {
                // make a json array out of the page source
                JSONArray shuttleArray = new JSONArray(source);
                // make the array match the number of elements in the Marker array
                while(shuttleArray.length() > shuttles.size()){
                    // add more shuttles
                    JSONObject extraShuttle = shuttleArray.getJSONObject(shuttles.size());
                    LatLng position = new LatLng(extraShuttle.getDouble("Latitude"), extraShuttle.getDouble("Longitude"));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(position);
                    shuttles.add(drawingMap.addMarker(markerOptions));
                }
                while(shuttleArray.length() < shuttles.size()){
                    // remove shuttles
                    shuttles.get(shuttles.size() - 1).setVisible(false);
                    shuttles.remove(shuttles.size() - 1);
                }
                // use the new shuttles array to update the map
                for (int i = 0; i < shuttleArray.length(); i++) {   // for each shuttle
                    JSONObject shuttle = shuttleArray.getJSONObject(i);
                    double lon = shuttle.getDouble("Longitude");
                    double lat = shuttle.getDouble("Latitude");
                    LatLng position = new LatLng(lat, lon);
                    shuttles.get(i).setPosition(position);

                }
            }catch (JSONException exception){
                Log.e("AndroidRuntime", "JSON parse error");
            }
        }
    }



}

package com.wylder.shuttlewidget;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A class that holds and handles the lifecycle of a ViewGroup in the main ViewPager.
 * It sends an Intent to the StopSchedulerService containing a route and stop id requesting
 * the arrival time and displays it.
 */
public class FragmentShuttleLookup extends Fragment {

    // a receiver to handle the response from ShuttleWidgetProvider
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            resultView.setText(intent.getStringExtra(StopSchedulerService.STOP_TIME));
        }
    };

    // handles the spinner logic
    private ArrayAdapter<String> routesAdapter;
    private ArrayAdapter<String> stopsAdapter;
    private ArrayList<String> stopsAdapterDataBackend;
    private AdapterView.OnItemSelectedListener onRouteSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int selected, long l) {
            if(!stopsAdapter.isEmpty()) {
                stopsAdapterDataBackend.clear();
                for(int i = 0; i < ShuttleConstants.stopNames[selected].length; i++){
                    stopsAdapterDataBackend.add(ShuttleConstants.stopNames[selected][i]);
                }
                stopsAdapter.notifyDataSetChanged();
            }
            adapterView.setBackgroundColor(ShuttleConstants.widgetColors[selected]);
            try {
                ((TextView) adapterView.getChildAt(0)).setTextColor(ShuttleConstants.textColors[selected]);
            }catch (NullPointerException exception){
                // view not created yet?
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            if(!stopsAdapter.isEmpty()){
                stopsAdapterDataBackend.clear();
                stopsAdapter.notifyDataSetChanged();
            }
            // the last element in the ShuttleConstants color lists are the default colors
            adapterView.setBackgroundColor(ShuttleConstants.widgetColors[ShuttleConstants.widgetColors.length - 1]);
            ((TextView)adapterView.getChildAt(0)).setTextColor(ShuttleConstants.textColors[ShuttleConstants.textColors.length - 1]);
        }
    };

    private Button updateButton;
    private Button mapButton;
    private TextView resultView;
    private Spinner routeSpinner;
    private Spinner stopSpinner;

    public FragmentShuttleLookup() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shuttle_lookup, container, false);
        updateButton = (Button) view.findViewById(R.id.searchButton);
        mapButton = (Button) view.findViewById(R.id.mapButton);
        resultView = (TextView) view.findViewById(R.id.resultTextView);
        routeSpinner = (Spinner) view.findViewById(R.id.routeSpinner);
        stopSpinner = (Spinner) view.findViewById(R.id.stopSpinner);

        // setup the adapters and their data
        stopsAdapterDataBackend = new ArrayList<String>();
        for(int i = 0; i < ShuttleConstants.stopNames[0].length; i++){
            stopsAdapterDataBackend.add(ShuttleConstants.stopNames[0][i]);
        }
        routesAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                ShuttleConstants.routeNames);
        stopsAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item,
                stopsAdapterDataBackend);

        routeSpinner.setAdapter(routesAdapter);
        stopSpinner.setAdapter(stopsAdapter);
        routeSpinner.setOnItemSelectedListener(onRouteSelectedListener);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lookupStop();
            }
        });
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewLiveMap();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        IntentFilter filter = new IntentFilter(StopSchedulerService.BROADCAST_SEARCH_RESULT);
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().unregisterReceiver(receiver);
    }

    /**
     * A helper method to take the current state and use it to lookup the stop arrival
     */
    private void lookupStop() {
        Intent requestAction = new Intent(getActivity(), StopSchedulerService.class);
        requestAction.putExtra(StopSchedulerService.UPDATE_TYPE, StopSchedulerService.GET_ARRIVAL_TIME);
        requestAction.putExtra(StopSchedulerService.STOP_ID, (int)stopSpinner.getSelectedItemId());
        requestAction.putExtra(StopSchedulerService.ROUTE_ID, (int)routeSpinner.getSelectedItemId());
        getActivity().startService(requestAction);
    }

    /**
     * a helper method to open an LiveMapActivity to show the live map
     */
    private void viewLiveMap() {
        Intent lookAtMap = new Intent(getActivity(), LiveMapActivity.class);
        lookAtMap.putExtra(LiveMapActivity.EXTRA_ROUTE_ID, (int)routeSpinner.getSelectedItemId());
        startActivity(lookAtMap);
    }

    /**
     * A workaround for a bug in FragmentManagerImpl, more info: https://code.google.com/p/android/issues/detail?id=19211
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        //first saving my state, so the bundle wont be empty.
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY",  "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

}

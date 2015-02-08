package com.wylder.shuttlewidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

/**
 * Created by kevin on 2/1/15.
 *
 * Provider for the Shuttle Widget.
 * All this does is receive broadcasts and use the broadcast information to update the widget
 */
public class ShuttleWidgetProvider extends AppWidgetProvider {

    // these values are needed to draw the widget and are written on response from StopSchedulerService
    String stopName = "Default Stop";
    String stopTime = "Default Time";
    int widgetColor = Color.WHITE;
    int textColor = Color.BLACK;
    boolean hasNoStopNow = true;

    /**
     * This overridden method is where the Widget's views will be updated.
     * notice that RemoteViews is used because we don't have access to manipulate the views directly
     * also notice the use of PendingIntents, which are for later execution
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        // find out whather to request update, or open app asking to make a new constraint
        PendingIntent onClickIntent;
        if(hasNoStopNow){
            // create an intent to open the app and make a new constraint
            Intent makeConstraint = new Intent(context, StopSchedulerActivity.class);
            makeConstraint.putExtra(StopSchedulerActivity.ACTION_CREATE_CONSTRAINT, true);
            onClickIntent = PendingIntent.getActivity(context, 0, makeConstraint, PendingIntent.FLAG_ONE_SHOT);
        }else{
            // create intent to update service, then turn it into PendingIntent
            Intent updateTimeIntent = new Intent(context, StopSchedulerService.class);
            updateTimeIntent.putExtra(StopSchedulerService.UPDATE_TYPE, StopSchedulerService.UPDATE_ARRIVAL);
            onClickIntent = PendingIntent.getService(context, 0, updateTimeIntent, PendingIntent.FLAG_ONE_SHOT);
        }

        // update the RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shuttle_widget);
        views.setOnClickPendingIntent(R.id.stopNameText, onClickIntent);
        views.setOnClickPendingIntent(R.id.timeRangeText, onClickIntent);
        views.setOnClickPendingIntent(R.id.widgetBackground, onClickIntent);
        views.setTextViewText(R.id.stopNameText, stopTime);
        views.setTextViewText(R.id.timeRangeText, stopName);

        // set background colors and text colors
        views.setInt(R.id.widgetBackground, "setBackgroundColor", widgetColor);
        views.setInt(R.id.stopNameText, "setTextColor", textColor);
        views.setInt(R.id.timeRangeText, "setTextColor", textColor);

        // signal update to the AppWidgetManager
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    /**
     * this method comes from BroadcastReceiver and is called when there is a request to update the
     * widget (every 15 min or so) and when my StopScheduleService has data to display via the
     * StopSchedulerService.BROADCAST_UPDATE_ACTION action.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent){
        if(intent.getAction().equals(StopSchedulerService.BROADCAST_UPDATE_ACTION)){
            // response to any update to time or stop, runs the onUpdate function
            stopName = intent.getStringExtra(StopSchedulerService.STOP_NAME);
            stopTime = intent.getStringExtra(StopSchedulerService.STOP_TIME);
            widgetColor = intent.getIntExtra(StopSchedulerService.BG_COLOR, Color.WHITE);
            textColor = intent.getIntExtra(StopSchedulerService.TEXT_COLOR, Color.BLACK);
            hasNoStopNow = intent.getBooleanExtra(StopSchedulerService.NO_STOP, false);

            AppWidgetManager gm = AppWidgetManager.getInstance(context);
            int[] ids = gm.getAppWidgetIds(new ComponentName(context, ShuttleWidgetProvider.class));
            this.onUpdate(context, gm, ids);
        }else{
            // happens every 15 min automatically.
            // request the stop from StopSchedulerService class via BROADCAST_UPDATE_ACTION
            Intent askForUpdateIntent = new Intent(context, StopSchedulerService.class);
            askForUpdateIntent.putExtra(StopSchedulerService.UPDATE_TYPE, StopSchedulerService.UPDATE_STOP);
            context.startService(askForUpdateIntent);
        }
    }

}

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

    String stopName = "Default Stop";
    String stopTime = "Default Time";
    int widgetColor = Color.WHITE;
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
        // create intent to update service, then turn it into PendingIntent
        Intent updateTimeIntent = new Intent(context, StopSchedulerService.class);
        updateTimeIntent.putExtra(StopSchedulerService.UPDATE_TYPE, StopSchedulerService.UPDATE_ARRIVAL);
        PendingIntent updatePendingIntent = PendingIntent.getService(context, 0, updateTimeIntent, PendingIntent.FLAG_ONE_SHOT);

        // update the RemoteViews
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shuttle_widget);
        views.setOnClickPendingIntent(R.id.stopNameText, updatePendingIntent);
        views.setOnClickPendingIntent(R.id.timeRangeText, updatePendingIntent);
        views.setTextViewText(R.id.stopNameText, stopTime);
        views.setTextViewText(R.id.timeRangeText, stopName);
        // get a text color that contrasts the background color
        int textColor = Color.BLACK;
        if(widgetColor != Color.WHITE){
            textColor = Color.WHITE;
        }
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
            widgetColor = intent.getIntExtra(StopSchedulerService.WIDGET_COLOR, Color.WHITE);

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

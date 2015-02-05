package com.wylder.shuttlewidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by kevin on 2/1/15.
 */
public class ShuttleWidgetProvider extends AppWidgetProvider {

    String stopName = "Default Stop";
    String stopTime = "Default Time";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        Intent updateTimeIntent = new Intent(context, StopSchedulerService.class);
        updateTimeIntent.putExtra(StopSchedulerService.UPDATE_TYPE, StopSchedulerService.UPDATE_ARRIVAL);
        PendingIntent updatePendingIntent = PendingIntent.getService(context, 0, updateTimeIntent, PendingIntent.FLAG_ONE_SHOT);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shuttle_widget);
        views.setOnClickPendingIntent(R.id.textView, updatePendingIntent);
        views.setOnClickPendingIntent(R.id.textView2, updatePendingIntent);
        views.setTextViewText(R.id.textView, stopTime);
        views.setTextViewText(R.id.textView2, stopName);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    @Override
    public void onReceive(Context context, Intent intent){
        if(intent.getAction().equals(StopSchedulerService.BROADCAST_UPDATE_ACTION)){
            // response to any update to time or stop, runs the onUpdate function
            stopName = intent.getStringExtra(StopSchedulerService.STOP_NAME);
            stopTime = intent.getStringExtra(StopSchedulerService.STOP_TIME);

            AppWidgetManager gm = AppWidgetManager.getInstance(context);
            int[] ids = gm.getAppWidgetIds(new ComponentName(context, ShuttleWidgetProvider.class));
            this.onUpdate(context, gm, ids);
        }else{
            // happens every 15 min and just updates the stop
            Intent askForUpdateIntent = new Intent(context, StopSchedulerService.class);
            askForUpdateIntent.putExtra(StopSchedulerService.UPDATE_TYPE, StopSchedulerService.UPDATE_STOP);
            context.startService(askForUpdateIntent);
        }
    }

}

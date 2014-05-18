
package edu.dartmouth.cs.myruns5;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * @author jried
 * The BroadcastReciever Class which extends the AppWidgetProvider
 * Note: On update() handles updating all instances of the widget (eg: home/lock screen instances)
 */
public class UVGWidgetProvider extends AppWidgetProvider {
    
	public static String CLOCK_WIDGET_UPDATE = "com.src.widget.uvg";
	private static final DateFormat df = new SimpleDateFormat("hh:mm:ss");
   
	/* (non-Javadoc)
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
	 * NOTE: Onreiceve must be finished within 5 sec
	 */
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.e("JERRID", "Clock update");
		super.onReceive(context, intent);
	}
	

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Log.e("JERRID", "Widget Provider disabled. Turning off timer");
	}
	
	@Override 
	public void onEnabled(Context ctx) {
		//startService instantiates the service whether an app's bound to it or not
		Intent intent = new Intent("com.src.service.RideKeeperMonitorService");
        ctx.getApplicationContext().startService(intent);
		
				
		//Intent intent = new Intent("com.src.service.RideKeeperMonitorService");
        //ctx.getApplicationContext().bindService(intent, rideKeeperMonitorServiceConnection, Context.BIND_NOT_FOREGROUND);	 
		Log.e("JERRID", "Widget Provider enabled.  Starting timer to update widget every second");
		super.onEnabled(ctx);
	}
	
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		
		Log.e("JERRID", "Updating Example Widgets.");
		for (int i = 0; i < appWidgetIds.length; i++) {
			int appWidgetId = appWidgetIds[i];

			final Intent intent = new Intent(context, TrackingService.class);
			
			// Get the layout for the App Widget and attach an on-click listener to button
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.uvg_widgit);
			intent.setAction(Globals.VOICE_COMMAND);
			final PendingIntent voicePendingIntent = PendingIntent.getService(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.speakbtn, voicePendingIntent);
			
			intent.setAction(Globals.TRACK_COMMAND);
			final PendingIntent trackingPendingIntent = PendingIntent.getService(context, 0, intent, 0);
			views.setOnClickPendingIntent(R.id.trackbtn, trackingPendingIntent);

			// Tell the AppWidgetManager to perform an update on the current app
			// widget
			appWidgetManager.updateAppWidget(appWidgetId, views);

			

			// Update The clock label using a shared method
			updateAppWidget(context, appWidgetManager, appWidgetId);
		}
	}

	public static void updateAppWidget(Context context,	AppWidgetManager appWidgetManager, int appWidgetId) {

	}

}

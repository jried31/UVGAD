package edu.dartmouth.cs.myruns5;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CurrentUVIFragment extends Fragment {
	Messenger myService = null;
	boolean isBound;
	private Timer timer;
	private UVIBroadcastReciever reciever;
	private IntentFilter filter;
	private View v;
	private float currentUVI = 0;
	private ExecutorService executorService;
	private float[] data;
	private Context context;
	private NotificationManager nm;
	private static final int ALERT_NOTIFICATION = 9;
	private long[] vibrate = {400, 1000, 400, 1000, 400, 1000};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		context = this.getActivity();
		nm = ( NotificationManager ) context.getSystemService(Context.NOTIFICATION_SERVICE);
		executorService = Executors.newSingleThreadExecutor();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				updateUVIWidget();
			}
		});

		v = inflater.inflate(R.layout.uvg_fragment_current_uvi, container,
				false);
		if (v == null)
			System.out.println("View is null!");
		else
			v.setOnTouchListener(new SwipeListener(context, this
					.getFragmentManager(), this, ((MainActivity) context)
					.getChartFragment(), ((MainActivity) context)
					.getRecommendFragment(), 1));

		updateDisplay(v);
		return v;
	}

	private TimerTask updateUVITask = new TimerTask() {
		@Override
		public void run() {
			// Call the UVI Service to grab the current UVI
			updateUVIWidget();
		}
	};
	final int updateInterval = 15;// minutes

	public UVIBroadcastReciever getReceiver() {
		return reciever;
	}

	private void updateUVIWidget() {
		final Intent currentUVIIntent = new Intent(getActivity(),
				UltravioletIndexService.class);
		currentUVIIntent.setAction(UltravioletIndexService.CURRENT_UV_INDEX);
		getActivity().startService(currentUVIIntent);
	}

	private void updateDisplay(View v) {
		//currentUVI = 11;
		TextView currentUVIView = (TextView) v.findViewById(R.id.current_uvi);
		currentUVIView.setText(Float.toString(currentUVI));
		switch ((int) currentUVI) {
		case 0:
		case 1:
		case 2:
			currentUVIView.setBackgroundResource(R.drawable.circle_green);
			break;
		case 3:
		case 4:
		case 5:
			currentUVIView.setBackgroundResource(R.drawable.circle_yellow);
			break;
		case 6:
		case 7:
			currentUVIView.setBackgroundResource(R.drawable.circle_orange);
			break;
		case 8:
		case 9:
		case 10:
			currentUVIView.setBackgroundResource(R.drawable.circle_red);
			break;
		case 11:
			currentUVIView.setBackgroundResource(R.drawable.circle_purple);
			break;
		default:
			currentUVIView.setBackgroundResource(R.drawable.circle_purple);
			break;
		}
		
		if(currentUVI >= 8){
			NotificationCompat.Builder mBuilder =
			        new NotificationCompat.Builder(context)
			        .setSmallIcon(R.drawable.alert)
			        .setAutoCancel(true)
			        .setContentTitle("UVI Alert")
			        .setContentText("Your local UVI level is very high, please take care!")
			        .setLights(0xFF00FF00, 400, 400)
			        .setVibrate(vibrate);
			Notification notif = mBuilder.build();
			nm.notify(ALERT_NOTIFICATION, notif);
		}
	}

	public int getUVI() {
		return (int) currentUVI;
	}

	public float[] getWebUVI() {
		if(data == null)
			return new float[13];
		return data;
	}

	// Recieves the current UVI broadcast updates from the Service
	class UVIBroadcastReciever extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			currentUVI = arg1.getExtras().getFloat(
					UltravioletIndexService.CURRENT_UV_INDEX);
			data = arg1.getExtras().getFloatArray(
					UltravioletIndexService.WEB_UVI);
			updateDisplay(v);
		}
	}

	private void registerReciever() {
		filter = new IntentFilter(UltravioletIndexService.CURRENT_UV_INDEX);
		reciever = new UVIBroadcastReciever();
		getActivity().registerReceiver(reciever, filter);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReciever();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (reciever != null) {
			getActivity().unregisterReceiver(reciever);
			reciever = null;
			filter = null;
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if (reciever != null) {
			getActivity().unregisterReceiver(reciever);
			reciever = null;
			filter = null;
		}
	}

}

package edu.dartmouth.cs.myruns5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class UltravioletIndexService extends Service implements LocationListener {

	private LocationManager myTracksLocationManager;
	private Geocoder gc;
	private static float HOURLY_UVI_FORECAST[] = new float[13];// UV Hourly
																// Forecast from
																// 6am-6pm
	public static final String CURRENT_UV_INDEX = "CURRENT_UVI";
	public static final String HOURLY_UV_INDEX = "HOURLY_UVI";
	public static final String WEB_UVI = "WEB_UVI";
	public static final String UVI_RECOMMENDATION = "UVI_RECOMMENDATION";
	public static final long MIN_TIME_BW_UPDATES = 10000;
	public static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5000;

	public static enum DAY_OF_WEEK {
		SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;
		@Override
		public String toString() {
			// return Upper Case of Day of Week
			String s = super.toString();
			return s.toUpperCase(Locale.getDefault());
		}
	};

	private boolean hasData = false;
	// private boolean looperCalled=false;
	private Context mContext;
	private float uvIndex;
	private String option;
	private NotificationManager nm;

	private Location location = null;

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		setLocation(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		System.out.println("No provider enabled!");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		System.out.println("GPS provider enabled!");
		setLocation(myTracksLocationManager.getLastKnownLocation(provider));
	}

	@Override
	public void onStatusChanged(String provider, int status,Bundle extras) {
		// TODO Auto-generated method stub
	}
	
	// Timer to periodically invoke updateUVITask
	private final Timer timer = new Timer();
	private TimerTask updateUVITask = new TimerTask() {
		@Override
		public void run() {
			updateHourlyUVI(location);
			sendData();
		}
	};

	public void setUVI(float uvi) {
		uvIndex = uvi;
	}

	public float getUVI() {
		return uvIndex;
	}

	private void updateHourlyUVI(final Location location) {
		Handler handler = new Handler(Looper.getMainLooper());

		if (location == null) {
			NotificationCompat.Builder n = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.runner)
					.setContentTitle("UV notification")
					.setAutoCancel(true)
					.setContentText(
							"GPS locating service is off, please turn it on!");
			nm.notify(0, n.build());

			handler.post(new Runnable() {
				public void run() {
					Toast.makeText(
							mContext,
							"GPS function is off. Please enable GPS locating service!",
							Toast.LENGTH_LONG).show();
				}
			});
			System.out.println("Location is null!");
			return;
		} else {
			nm.cancel(0);
			handler.post(new Runnable() {
				public void run() {
					Toast.makeText(
							mContext,
							"Position located! Longitude: "
									+ location.getLongitude() + ", Latitude: "
									+ location.getLatitude(), Toast.LENGTH_LONG)
							.show();
				}
			});

			ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("UVData");
			query.orderByDescending("createdAt");
			query.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> objectList, ParseException e) {
					Date now = new Date();
					Date refer;
					int sum = 0, num = 0;
					long time1, time2;
					time1 = now.getTime();
					if (e == null) {
						for (ParseObject obj : objectList) {
							refer = obj.getUpdatedAt();
							time2 = refer.getTime();
							if (time1 - time2 <= 120000) {
								num++;
								sum += obj.getInt("UV_index");
							} else
								break;
						}
						if (num > 0)
							sum /= num;
						setUVI(sum);

						hasData = true;
					} else {
						e.printStackTrace();
					}
				}
			});

			getDataFromWeb(location);
		}
	}

	public void getDataFromWeb(Location location) {
		// Get the time and day of week
		Calendar now = Calendar.getInstance();
		int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

		String responseString = null;
		try {
			String postCode = getPostcode(location);

			// curl --referer
			// http://www.uvawareness.com/uv-index/uv-index.php?location=ucla

			// URL to the main website
			if (postCode.equals("")) {
				System.out.println("Post Code is empty!");
				return;
			}
			String website = "http://www.uvawareness.net/s/index.php", referrerWebsite = "http://www.uvawareness.com/uv-index/uv-index.php";
			Uri.Builder uri = Uri.parse(referrerWebsite).buildUpon();
			uri.appendQueryParameter("location", postCode);

			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httppost = new HttpGet(website);
			httppost.addHeader("Referer", uri.build().toString());// "http://www.uvawareness.com/uv-index/uv-index.php?location=3450%20sawtelle%20blvd");

			// Execute HTTP Post Request
			org.apache.http.HttpResponse response = httpclient
					.execute(httppost);
			HttpEntity entity = response.getEntity();
			InputStream instream = entity.getContent();
			InputStreamReader isr = new InputStreamReader(instream);
			BufferedReader rd = new BufferedReader(isr);
			StringBuffer buffer = new StringBuffer();

			try {
				String line = "";
				while ((line = rd.readLine()) != null) {
					buffer.append(line);
				}
				responseString = buffer.toString();
				isr.close();
			} finally {
				instream.close();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (responseString != null) {
			Document doc = Jsoup.parse(responseString);
			Elements content = doc.getElementsByClass("fcDayCon");

			for (Element fcDayCon : content) {
				// Found the UVI data for the proper day
				Element dayCon = fcDayCon.getElementsByClass("fcDate").first();

				String day = dayCon.html().toUpperCase(Locale.US);
				if (day.equals(DAY_OF_WEEK.values()[dayOfWeek - 1].name())) {
					int it = 0;
					Elements contents = fcDayCon.getElementsByClass("uval");
					// Iteration of UVal values go from
					// 6am,7,8,9,10,11,12pm,1,2,3,4,5,6pm
					for (int i = 0; i < 13; i++) {
						// If hour is 6am, 5pm and 6pm, no UVI then
						if(i == 0 || i == 11 || i == 12){
							HOURLY_UVI_FORECAST[i] = 0;
							continue;
						}
						Element uval = contents.get(i);
						String val = uval.html();

						// val will return an empty string if no value present
						if (val.equals(""))
							HOURLY_UVI_FORECAST[i] = 1;
						else {
							HOURLY_UVI_FORECAST[i] = Float.parseFloat(val);
						}
					}

					break;
				}
			}
			hasData = true;
		}
	}

	public String getPostcode(Location location) {
		Geocoder geoCoder = new Geocoder(getApplicationContext(),
				Locale.getDefault());
		List<Address> address = null;
		String postCode = "";
		
		if (geoCoder != null) {
			try {
				address = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (address.size() > 0) {
				postCode = address.get(0).getPostalCode();
			}
		}
		
		return postCode;
	}


	public void sendData() {
		if (hasData) {
			if (option.equals(CURRENT_UV_INDEX)) {
				// float uvi = UltravioletIndexService.getCurrentUVI();
				float uvi = getUVI();
				if (uvi >= 0) {
					Intent i = new Intent(CURRENT_UV_INDEX).putExtra(
							CURRENT_UV_INDEX, uvi);
					i.putExtra(WEB_UVI, HOURLY_UVI_FORECAST);
					sendBroadcast(i);
				}

			} else if (option.equals(HOURLY_UV_INDEX)) {
				Intent i = new Intent(HOURLY_UV_INDEX);
				i.putExtra(CURRENT_UV_INDEX, HOURLY_UVI_FORECAST);
				sendBroadcast(i);
			} else if (option.equals(UVI_RECOMMENDATION)) {

			}
		}
		hasData = false;
	}

	final int updateInterval = 60;

	@Override
	public void onCreate() {
		mContext = getApplicationContext();
		gc = new Geocoder(this);
		myTracksLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_LOW);
		String bestProvider = myTracksLocationManager.getBestProvider(criteria, true);
		myTracksLocationManager.requestLocationUpdates(bestProvider,MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this);
		setLocation(myTracksLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		/*
		 * Calendar now = Calendar.getInstance(); int minute =
		 * now.get(Calendar.MINUTE);// 24 hr format long firstExecutionDelay =
		 * (updateInterval - minute) Globals.ONE_MINUTE;
		 */
		timer.scheduleAtFixedRate(updateUVITask, 0, 10000);
		// super.onCreate();
		
	}


	@Override
	public void onDestroy() {
		myTracksLocationManager.removeUpdates(this);
	}
	
	private void setLocation(Location loc) {
		location = loc;
	}

	/*
	 * private static float getCurrentUVI() { float currUVI = 0; Calendar now =
	 * Calendar.getInstance(); int hour = now.get(Calendar.HOUR_OF_DAY);// 24 hr
	 * format int nextHour = hour + 1;
	 * 
	 * if (hour < 6)// Hr < 6am return currUVI; else if (hour > 18)// Hr > 6pm
	 * return currUVI; else {
	 * 
	 * long currTime = now.getTimeInMillis(); Calendar prevHr =
	 * Calendar.getInstance(); prevHr.set(now.get(Calendar.YEAR),
	 * now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), hour, 0, 0);
	 * 
	 * Calendar currHr = Calendar.getInstance();
	 * currHr.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
	 * now.get(Calendar.DATE), hour, 0);
	 * 
	 * Calendar nextHr = Calendar.getInstance();
	 * nextHr.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
	 * now.get(Calendar.DATE), nextHour, 0);
	 * 
	 * float dtime = (currTime - prevHr.getTimeInMillis()) /
	 * (nextHr.getTimeInMillis() - prevHr.getTimeInMillis());
	 * 
	 * // y=mx+b assuming linear scale time increase hour -= 6; nextHour -= 6;
	 * // simple correction for the last index if (nextHour ==
	 * HOURLY_UVI_FORECAST.length) nextHour = hour;
	 * 
	 * if (HOURLY_UVI_FORECAST[nextHour] > HOURLY_UVI_FORECAST[hour]) return
	 * dtime (HOURLY_UVI_FORECAST[nextHour] - HOURLY_UVI_FORECAST[hour]) +
	 * HOURLY_UVI_FORECAST[hour]; else return HOURLY_UVI_FORECAST[hour] - dtime
	 * (HOURLY_UVI_FORECAST[hour] - HOURLY_UVI_FORECAST[nextHour]); } }
	 */

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null)
			return 0;
		option = intent.getAction();

		if (hasData) {
			if (option.equals(CURRENT_UV_INDEX)) {
				float uvi = getUVI();
				if (uvi > 0) {
					Intent i = new Intent(CURRENT_UV_INDEX).putExtra(
							CURRENT_UV_INDEX, uvi);
					i.putExtra(WEB_UVI, HOURLY_UVI_FORECAST);
					sendBroadcast(i);
				}

			} else if (option.equals(HOURLY_UV_INDEX)) {
				Intent i = new Intent(HOURLY_UV_INDEX);
				i.putExtra(CURRENT_UV_INDEX, HOURLY_UVI_FORECAST);
				sendBroadcast(i);
			} else if (option.equals(UVI_RECOMMENDATION)) {

			}
		}
		return START_STICKY;
	}

	/*
	 * Handler to handle messages coming in from the android application. The
	 * android App binds to the service
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {

			Bundle data = msg.getData();
			String option = data.getString("option");

			if (option.equals(CURRENT_UV_INDEX)) {
				// float uvi = UltravioletIndexService.getCurrentUVI();
				float uvi = getUVI();
				if (uvi > 0) {
					Intent i = new Intent(CURRENT_UV_INDEX);
					i.putExtra(CURRENT_UV_INDEX, uvi);
					sendBroadcast(i);
				}

			} else if (option.equals(HOURLY_UV_INDEX)) {
				Intent i = new Intent(HOURLY_UV_INDEX);
				i.putExtra(CURRENT_UV_INDEX, HOURLY_UVI_FORECAST);
				sendBroadcast(i);
			} else if (option.equals(UVI_RECOMMENDATION)) {

			}
		}
	}

	// References an instance of the Messenger object that passes messages
	final Messenger myMessenger = new Messenger(new IncomingHandler());

	// When an app binds to the service it gets a reference to the messenger to
	// bind to the service

	@Override
	public IBinder onBind(Intent arg0) {
		return myMessenger.getBinder();
	}
}

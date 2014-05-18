package edu.dartmouth.cs.myruns5;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import edu.dartmouth.cs.myruns5.util.SunAngle;
import edu.dartmouth.cs.myruns5.util.uv.ParseUVReading;

public class UltravioletIndexService extends Service implements LocationListener {

	private LocationManager myTracksLocationManager;
	private Geocoder gc;
	private static float HOURLY_UVI_FORECAST[] = new float[13];// UV Hourly
																// Forecast from
																// 6am-6pm
	public static final String CURRENT_UV_INDEX = "CURRENT_UVI";
	public static final String CURRENT_SUN_ANGLE = "CURRENT_SUN_ANGLE";
	public static final String HOURLY_UV_INDEX = "HOURLY_UVI";
	public static final String WEB_UVI = "WEB_UVI";
	public static final String UVI_RECOMMENDATION = "UVI_RECOMMENDATION";
	public static final long MIN_TIME_BW_UPDATES = 10000;
	public static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5000;
	public static final String CURRENT_UV_INDEX_SUN = "CURRENT_UVI_SUN";
	public static final String CURRENT_UV_INDEX_SHADE = "CURRENT_UVI_SHADE";
	public static final String CURRENT_UV_INDEX_CLOUD = "CURRENT_UVI_CLOUD";
	public static final String AZIMUTH_ANGLE = "AZIMUTH_ANGLE";
	public static final String ELEVATION_ANGLE = "ELEVATION_ANGLE";
	public static final String SOLAR_ZENITH_ANGLE = "SOLAR_ZENITH_ANGLE";
	
	public static final String CURRENT_UV_INDEX_ALL = "uvi_all";
	public static Location location = null;
	public static double uvIrradianceSun=0,uvIrradianceShade=0,uvIrradianceCloud=0;

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
	private String option;
	private NotificationManager nm;

	@Override
	public void onLocationChanged(Location location) {
		UltravioletIndexService.location=location;
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
		UltravioletIndexService.location=(myTracksLocationManager.getLastKnownLocation(provider));
	}

	@Override
	public void onStatusChanged(String provider, int status,Bundle extras) {
		// TODO Auto-generated method stub
	}
	
	// Timer to periodically invoke updateUVITask
	private final Timer uviTimer = new Timer();
	private TimerTask updateUVITask = new TimerTask() {
		@Override
		public void run() {
			updateHourlyUVI();
			sendData();
		}
	};
	
	
	private final ScheduledExecutorService sunAngleTimer = Executors.newScheduledThreadPool(1);

	private Runnable updateSunAngleTask = new Runnable() {
		@Override
		public void run() {
			try {
				reverseGeocodedLocation();
				updateSunAngle();
	            sunAngleTimer.schedule(updateSunAngleTask, Globals.ONE_MINUTE*12, TimeUnit.MILLISECONDS);
			} catch (ClientProtocolException e) {
				sunAngleTimer.schedule(updateSunAngleTask, MIN_TIME_BW_UPDATES, TimeUnit.MILLISECONDS);
				e.printStackTrace();
			} catch (IOException e) {
				sunAngleTimer.schedule(updateSunAngleTask, MIN_TIME_BW_UPDATES, TimeUnit.MILLISECONDS);
				e.printStackTrace();
			} catch (JSONException e) {
				sunAngleTimer.schedule(updateSunAngleTask, MIN_TIME_BW_UPDATES, TimeUnit.MILLISECONDS);
				e.printStackTrace();
			}
		}
	};
	
    private void updateCurrentTime() {
        GregorianCalendar now = new GregorianCalendar();
        month=Integer.toString(now.get(Calendar.MONTH)+1);
        day=Integer.toString(now.get(Calendar.DAY_OF_MONTH));
        year=Integer.toString(now.get(Calendar.YEAR));
        hour=Integer.toString(now.get(Calendar.HOUR_OF_DAY));
        minute=Integer.toString(now.get(Calendar.MINUTE));
    }
    
	//----------SUN ANGLE VARIABLES ----------
    private String month;
    private String day;
    private String year;
    private String city = "Los Angeles";
    private String state = "CA";
    private String hour = null;
    private String minute = null;
    public static HashMap<String,SunAngle> sunAngles = null;
    private Calendar time; 
    private static final String TIME24HOURS_PATTERN = "([01]?[0-9]|2[0-3]):[0-5][0-9].*";
    private final String interval = "10";
    private String referrerURI="http://aa.usno.navy.mil/data/docs/AltAz.php";
    private String url ="http://aa.usno.navy.mil/cgi-bin/aa_altazw.pl";
    //private String url ="http://aa.usno.navy.mil/cgi-bin/aa_altazw.pl?FFX=1&obj=INTERVAL&xxy=2014&xxm=2&xxd=25&xxi=10&st=CA&place=los+angeles&ZZZ=END";
	//---------------------------
    private void updateSunAngle() throws ClientProtocolException, IOException {

    	//if(location != null){
	        updateCurrentTime();
	        HttpClient httpclient = new DefaultHttpClient();
	        HttpPost httppost = new HttpPost(url);
	        httppost.addHeader("Referer", referrerURI);
	        //httppost.addHeader("Host","aa.usno.navy.mil");
	        String responseString = null;
	
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(10);
	        nameValuePairs.add(new BasicNameValuePair("FFX", "1"));
	        nameValuePairs.add(new BasicNameValuePair("ZZZ", "END"));
	        nameValuePairs.add(new BasicNameValuePair("sun", "10"));
	        nameValuePairs.add(new BasicNameValuePair("place", city));
	        nameValuePairs.add(new BasicNameValuePair("st", state));
	        nameValuePairs.add(new BasicNameValuePair("obj", interval));
	        nameValuePairs.add(new BasicNameValuePair("xxi", interval));
	        nameValuePairs.add(new BasicNameValuePair("xxd", day));//day
	        nameValuePairs.add(new BasicNameValuePair("xxy", year));//year
	        nameValuePairs.add(new BasicNameValuePair("xxm", month));//month
	
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	            //System.out.println(httppost.getURI().toString());
	
	        HttpResponse response = httpclient.execute(httppost);
	        StatusLine statusLine = response.getStatusLine();
	        String[] token;
	        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
	            ByteArrayOutputStream out = new ByteArrayOutputStream();
	            response.getEntity().writeTo(out);
	            out.close();
	            responseString = out.toString();
	
	            token = responseString.split("\n");
	            UltravioletIndexService.sunAngles = new HashMap<String, SunAngle>();
	            for (String row : token) {
	                boolean val = row.matches(TIME24HOURS_PATTERN);
	                if (val == true) {
	                    String data[] = row.split("\\s+");
	                    UltravioletIndexService.sunAngles.put(data[0], new SunAngle(data[0], Float.valueOf(data[1]), Float.valueOf(data[2])));
	                    //System.out.println(row + " " +val);
	                }
	            }
	            
	        } else {
	            //Closes the connection.
	            response.getEntity().getContent().close();
	            throw new IOException(statusLine.getReasonPhrase());
	        }
    	//}else
    	//	throw new IOException("Locataion not found in updateSunAngleFunction");
    }
    
    
	boolean dailyUVIRetrieved = false;
	private void updateHourlyUVI() {
		Handler handler = new Handler(Looper.getMainLooper());

		if (location == null) 
		{
			/*NotificationCompat.Builder n = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.runner)
					.setContentTitle("UV notification")
					.setAutoCancel(true)
					.setContentText("GPS locating service is off, please turn it on!");
			nm.notify(0, n.build());

			handler.post(new Runnable() {
				public void run() {
					Toast.makeText(
							mContext,
							"GPS function is off. Please enable GPS locating service!",
							Toast.LENGTH_LONG).show();
				}
			});
			*/
			System.out.println("Location is null!");
			//return;
		} 
		else {
			/*
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
			*/

			System.out.println("Position located! Longitude: "
					+ location.getLongitude() + ", Latitude: ");
		}//NOTE: THIS BRACKET IS TEMPOARY. THE ORIGINAL BRACKET IS IN THE BOTTOM OF THE METHOD
			ParseQuery<ParseObject> querySun = new ParseQuery<ParseObject>("UVData"),
					queryShade = new ParseQuery<ParseObject>("UVData"),
					queryCloud = new ParseQuery<ParseObject>("UVData");
			
			querySun.whereEqualTo(ParseUVReading.ENVIRONMENT, Globals.CLASS_LABEL_IN_SUN);
			querySun.orderByDescending("timestamp");
			querySun.setLimit(30);
			querySun.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> objectList, ParseException e) {
					
					if (e == null) {
						long time1, time2;
						Date now = new Date();
						time1 = now.getTime();
						double meanUVISun = 0,sunCount=0;
						
						for (ParseObject obj: objectList) {
							ParseUVReading reading = (ParseUVReading)obj;
							Date timestamp = reading.getTimestamp();
							String environment = reading.getEnvironment();
							double uvi = reading.getUVI();
							time2 = timestamp.getTime();
							
							//if (time1 - time2 <= 120000) {
							
							if(environment.equals(Globals.CLASS_LABEL_IN_SUN)){
								if(sunCount++ < 1)
									meanUVISun = uvi*1.0;
				                else
				                	meanUVISun = (uvi + meanUVISun*(sunCount-1))/sunCount;
							}
							//}
						}

						UltravioletIndexService.uvIrradianceSun = meanUVISun * (100*100 / 1000);
						hasData = true;
					} else {
						e.printStackTrace();
					}
				}
			});
			queryCloud.whereEqualTo(ParseUVReading.ENVIRONMENT, Globals.CLASS_LABEL_IN_CLOUD);
			queryCloud.orderByDescending("timestamp");
			queryCloud.setLimit(30);
			queryCloud.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> objectList, ParseException e) {
					
					if (e == null) {
						long time1, time2;
						Date now = new Date();
						time1 = now.getTime();
						double meanUVICloud=0,cloudCount=0;
						
						for (ParseObject obj: objectList) {
							ParseUVReading reading = (ParseUVReading)obj;
							Date timestamp = reading.getTimestamp();
							String environment = reading.getEnvironment();
							double uvi = reading.getUVI();
							time2 = timestamp.getTime();
							
							//if (time1 - time2 <= 120000) {
							
							if(environment.equals(Globals.CLASS_LABEL_IN_CLOUD)){
								if(cloudCount++ < 1)
									meanUVICloud = uvi*1.0;
				                else
				                	meanUVICloud = (uvi + meanUVICloud*(cloudCount-1))/cloudCount;
							}
							//}
						}
						UltravioletIndexService.uvIrradianceCloud =meanUVICloud * (100*100 / 1000);

						//hasData = true;
					} else {
						e.printStackTrace();
					}
				}
			});
			
			queryShade.whereEqualTo(ParseUVReading.ENVIRONMENT, Globals.CLASS_LABEL_IN_SHADE);
			queryShade.orderByDescending("timestamp");
			queryShade.setLimit(30);
			queryShade.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> objectList, ParseException e) {
					
					if (e == null) {
						long time1, time2;
						Date now = new Date();
						time1 = now.getTime();
						double meanUVIShade=0,shadeCount=0;
						
						for (ParseObject obj: objectList) {
							ParseUVReading reading = (ParseUVReading)obj;
							Date timestamp = reading.getTimestamp();
							String environment = reading.getEnvironment();
							double uvi = reading.getUVI();
							time2 = timestamp.getTime();
							
							//if (time1 - time2 <= 120000) {
							
							if(environment.equals(Globals.CLASS_LABEL_IN_SHADE)){
								if(shadeCount++ < 1)
									meanUVIShade = uvi*1.0;
				                else
				                	meanUVIShade = (uvi + meanUVIShade*(shadeCount-1))/shadeCount;
							}
							//}
						}
						UltravioletIndexService.uvIrradianceShade = meanUVIShade * (100*100 / 1000);
						//hasData = true;
					} else {
						e.printStackTrace();
					}
				}
			});

			//if(!dailyUVIRetrieved)
				getDataFromWeb();
		//}
	}
	
	

	public void getDataFromWeb() {
		// Get the time and day of week
		Calendar now = Calendar.getInstance();
		int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);

		String responseString = null;
		try {
			String postCode = "90024";//getPostcode(location);

			// curl --referer
			// http://www.uvawareness.com/uv-index/uv-index.php?location=ucla

			// URL to the main website
			if (postCode != null) {
				String website = "http://www.uvawareness.net/s/index.php", referrerWebsite = "http://www.uvawareness.com/uv-index/uv-index.php";
				Uri.Builder uri = Uri.parse(referrerWebsite).buildUpon();
				uri.appendQueryParameter("location", postCode);
	
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet httppost = new HttpGet(website);
				httppost.addHeader("Referer", uri.build().toString());// "http://www.uvawareness.com/uv-index/uv-index.php?location=3450%20sawtelle%20blvd");
	
				// Execute HTTP Post Request
				org.apache.http.HttpResponse response = httpclient.execute(httppost);
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
				
				if (responseString != null) 
				{
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

					dailyUVIRetrieved = true;
				}
			}else{				
				System.out.println("Post Code is empty!");
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getPostcode(Location location) {
		Geocoder geoCoder = new Geocoder(getApplicationContext(),
				Locale.getDefault());
		List<Address> address = null;
		String postCode = null;
		
		if (geoCoder != null) {
			try {
				address = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				if (address.size() > 0) {
					postCode = address.get(0).getPostalCode();
				}
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
		return postCode;
	}


	public void sendData() {
		if (hasData) {
			if (option.equals(CURRENT_UV_INDEX)) {
				double uvi = uvIrradianceSun;
				if (uvi >= 0) {
					Intent i = new Intent(CURRENT_UV_INDEX).putExtra(CURRENT_UV_INDEX, uvi);
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
		//hasData = false;
	}

	final int updateInterval = 60;
	public static float elevationAngle=Float.MIN_VALUE;
	public static float azimuthAngle=Float.MIN_VALUE;
	public static float solarZenithAngle=Float.MIN_VALUE;

	@Override
	public void onCreate() {
		mContext = getApplicationContext();
		gc = new Geocoder(this);
		myTracksLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_LOW);
		String bestProvider = myTracksLocationManager.getBestProvider(criteria, true);
		myTracksLocationManager.requestLocationUpdates(bestProvider,MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,this);

		location = myTracksLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null ? myTracksLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER):location;
		
		nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		uviTimer.scheduleAtFixedRate(updateUVITask, 500, MIN_TIME_BW_UPDATES);
		
		//Schedule update for sunangles every 2 hrs
		sunAngleTimer.schedule(updateSunAngleTask, 0, TimeUnit.MILLISECONDS);
		// super.onCreate();
		
		if(location != null)
			getDataFromWeb();
	}


	@Override
	public void onDestroy() {
		myTracksLocationManager.removeUpdates(this);
	}
	

    private void reverseGeocodedLocation() throws IOException, JSONException {
    	if(location != null)
    	{
	        //Get Reverse Geoposition information
	        HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng=" + location.getLatitude() + "," + location.getLongitude() + "&sensor=false");
	        HttpClient client = new DefaultHttpClient();
	        HttpResponse response;
	        StringBuilder stringBuilder = new StringBuilder();
	
	        response = client.execute(httpGet);
	        HttpEntity entity = response.getEntity();
	        InputStream stream = entity.getContent();
	        int b;
	        while ((b = stream.read()) != -1) {
	            stringBuilder.append((char) b);
	        }
	
	        JSONObject jsonObject = new JSONObject();
	        jsonObject = new JSONObject(stringBuilder.toString());
	
	        // get lat and lng value
	        String location_string = null;
	        //Get JSON Array called "results" and then get the 0th complete object as JSON        
	        JSONObject address = jsonObject.getJSONArray("results").getJSONObject(0);
	        // Get the value of the attribute whose name is "formatted_string"
	        JSONObject addressComponent = address.getJSONArray("address_components").getJSONObject(0);
	
	        JSONArray component = address.getJSONArray("address_components");
	        boolean cityFound = false, stateFound = false;
	        for (int i = 0; (i < component.length()) && (!cityFound || !stateFound); i++) {
	            addressComponent = component.getJSONObject(i);
	            JSONArray types = addressComponent.getJSONArray("types");
	            for (int j = 0; j < types.length(); j++) {
	                if (types.getString(j).equals("locality")) {
	                    city = addressComponent.getString("long_name");
	                    cityFound = true;
	                }
	
	                if (types.getString(j).equals("administrative_area_level_1")) {
	                    state = addressComponent.getString("short_name");
	                    stateFound = true;
	                }
	            }
	        }
    	}//else throw new IOException("Location is null in updateGeoLocation()");
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null)
			return 0;
		option = intent.getAction();

		if (hasData) {
			if (option.equals(CURRENT_UV_INDEX_ALL)) {
					Intent i = new Intent(CURRENT_UV_INDEX_ALL);
					i.putExtra(CURRENT_UV_INDEX_SUN, uvIrradianceSun);
					i.putExtra(CURRENT_UV_INDEX_SHADE, uvIrradianceShade);
					sendBroadcast(i);
			} else if( option.equals(CURRENT_SUN_ANGLE)){
				if(getCurrentSunAngle()){
					Intent i = new Intent(CURRENT_SUN_ANGLE);
					i.putExtra(AZIMUTH_ANGLE, azimuthAngle);
					i.putExtra(ELEVATION_ANGLE, elevationAngle);
					i.putExtra(SOLAR_ZENITH_ANGLE, solarZenithAngle);
					sendBroadcast(i);
				}else{
					
				}
				
			}else if (option.equals(CURRENT_UV_INDEX)) {
				double uvi = uvIrradianceSun;
				if (uvi > 0) {
					Intent i = new Intent(CURRENT_UV_INDEX).putExtra(CURRENT_UV_INDEX, uvi);
					i.putExtra(WEB_UVI, HOURLY_UVI_FORECAST);
					sendBroadcast(i);
				}
			} else if (option.equals(CURRENT_UV_INDEX_SUN)) {
				double uvi = uvIrradianceSun;
				if (uvi > 0) {
					Intent i = new Intent(CURRENT_UV_INDEX_SUN).putExtra(CURRENT_UV_INDEX_SUN, uvi);
					sendBroadcast(i);
				}
			}  else if (option.equals(CURRENT_UV_INDEX_SHADE)) {
				double uvi = uvIrradianceShade;
				if (uvi > 0) {
					Intent i = new Intent(CURRENT_UV_INDEX_SHADE).putExtra(CURRENT_UV_INDEX_SHADE, uvi);
					sendBroadcast(i);
				}
			} else if (option.equals(CURRENT_UV_INDEX_CLOUD)) {
				double uvi = uvIrradianceCloud;
				if (uvi > 0) {
					Intent i = new Intent(CURRENT_UV_INDEX_CLOUD).putExtra(CURRENT_UV_INDEX_CLOUD, uvi);
					sendBroadcast(i);
				}
			}else if (option.equals(HOURLY_UV_INDEX)) {
				Intent i = new Intent(HOURLY_UV_INDEX);
				i.putExtra(CURRENT_UV_INDEX, HOURLY_UVI_FORECAST);
				sendBroadcast(i);
			} else if (option.equals(UVI_RECOMMENDATION)) {

			}
		}
		return START_STICKY;
	}
	
	private boolean getCurrentSunAngle() {
		boolean result = false;
		if(sunAngles != null){
		    updateCurrentTime();
		    String time =   ((hour.length()==1?"0"+hour:hour)+":"+(minute.length()==1?"00":(minute.charAt(0)+"0")));//"13:30";//
		    SunAngle angle = this.sunAngles.get(time);
		    if(angle != null){
		    	azimuthAngle = angle.getAzimuth();
		    	elevationAngle = angle.getAltitude();
		    	elevationAngle = elevationAngle < 0 ? 0:elevationAngle;
		    	
		    	solarZenithAngle = Math.abs(90-elevationAngle);
		       //System.out.println("Azimuth Angle: "+this.azimuthAngle+ " Elevation Angle: "+this.elevationAngle + "\nSolar Zenith Angle: "+ this.solarZenithAngle);
		       result = true;
			}
		}
	    return result;
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
				double uvi = uvIrradianceSun;
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

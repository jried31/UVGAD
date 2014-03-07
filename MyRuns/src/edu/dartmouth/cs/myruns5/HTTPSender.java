package edu.dartmouth.cs.myruns5;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.AsyncTask;

public class HTTPSender extends AsyncTask<JSONObject, Void, Boolean>{
	final String SERVER_URI = "http://192.168.0.192/";
	
	@Override
	protected Boolean doInBackground(JSONObject... jsonObjs) {
		HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(SERVER_URI);
        
        for (int i = 0; i < jsonObjs.length; i++) {
        	JSONObject json = jsonObjs[i];
			try {
				StringEntity se = new StringEntity(json.toString());
				httpPost.setEntity(se);
	            HttpResponse response = httpClient.execute(httpPost);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
}

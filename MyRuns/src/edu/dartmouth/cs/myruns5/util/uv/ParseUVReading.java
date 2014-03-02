package edu.dartmouth.cs.myruns5.util.uv;

import java.util.Date;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName(DBGlobals.PARSE_UV_TBL)
public class ParseUVReading extends ParseObject{
	public static final String UVI = "uvi",
			LOCATION = "location",
			TIMESTAMP = "timestamp";
	
	public ParseUVReading() {
	}

	public int getUVI(){
		return this.getInt(UVI);
	}
	
	public ParseGeoPoint getLocation(){
		return this.getParseGeoPoint(LOCATION);
	}
	
	public void setLocation(ParseGeoPoint point){
		 put(LOCATION,point);
	}
	
	public void setUVI(int uvi){
		put(UVI, uvi);
	}
	
	public Date getTimestamp(){
		return this.getDate(TIMESTAMP);
	}
	
	public void setTimestamp(Date timestamp){
		put(TIMESTAMP,timestamp);
	}
}

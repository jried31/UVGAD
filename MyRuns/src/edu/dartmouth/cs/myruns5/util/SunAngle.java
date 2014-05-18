package edu.dartmouth.cs.myruns5.util;


public class SunAngle {
    private String time;
    private float altitude;
    private float azimuth;

    public String getTime(){return time;}
    public float getAltitude(){return altitude;}
    public float getAzimuth(){return azimuth;}
    
    public SunAngle(String time, float altitude, float azimuth) {
        this.time=time;
        this.altitude=altitude;
        this.azimuth=azimuth;
    }
    
	
}

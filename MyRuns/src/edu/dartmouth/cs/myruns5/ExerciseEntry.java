package edu.dartmouth.cs.myruns5;

import java.util.ArrayList;
import java.util.Date;

import android.location.Location;

public class ExerciseEntry {
	
	private Long id;
	
	private long remoteId;
	private int inputType;
	private int activityType;
	private Date dateTime;
	private int duration;
	private double distance;
	private String sweatrate;
	private double avgPace; 
	private double avgSpeed;
	private int calorie;
	private double climb;
	private int heartrate;
	private double uvExposureCum;
	private double vitaminDExposureCum;
	private int gender;
	private int skinTone;
	private int spf;
	private float clothingCover;
	//private SpriteHeadApparel.HeadApparelType headApparel;
	//private SpriteUpperApparel.UpperApparelType upperApparel;
	//private SpriteLowerApparel.LowerApparelType lowerApparel;

	private String comment;
    private Location[] myTrack; // Location array
    private ArrayList<Location> mLocationList;
	
	public ExerciseEntry(){
		this.remoteId = -1L;
		this.inputType = -1;
		this.activityType = -100;
		this.dateTime = new Date(System.currentTimeMillis());
		this.duration = 0;
		this.distance = 0;
		this.sweatrate = "";
		this.avgPace = 0;
		this.avgSpeed = 0;
		this.calorie = 0;
		this.climb = 0;
		this.heartrate = 0;
		this.vitaminDExposureCum=0;
		this.uvExposureCum=0;
		this.comment = "";
		this.gender = 0;
		this.skinTone = 0;
		this.spf = 0;
		this.clothingCover = 0.0f;
		//this.headApparel = SpriteHeadApparel.HeadApparelType.NONE;
		//this.upperApparel = SpriteUpperApparel.UpperApparelType.NONE;
		//this.lowerApparel = SpriteLowerApparel.LowerApparelType.NONE;
	}
	public Location[] getTrack(){ return myTrack;}
	public void setTrack(Location[] track){myTrack = track;}
	public ArrayList<Location> getLocationList(){return mLocationList;}
	public void setLocationList(ArrayList<Location> locationList){mLocationList = locationList;}
	

	public double getVitaminDExposureCumulative() {
		return vitaminDExposureCum;
	}
	public void setVitaminDExposureCumulative(double vitaminDExposureCum) {
		this.vitaminDExposureCum = vitaminDExposureCum;
	}
	public double getUVExposureCumulative() {
		return uvExposureCum;
	}
	public void setUvExposureCumulative(double uvExposureCum) {
		this.uvExposureCum = uvExposureCum;
	}
	
	public long getRemoteId() {
		return remoteId;
	}

	public void setRemoteId(long remoteId) {
		this.remoteId = remoteId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getInputType() {
		return inputType;
	}

	public void setInputType(int inputType) {
		this.inputType = inputType;
	}

	public int getActivityType() {
		return activityType;
	}

	public void setActivityType(int activityType) {
		this.activityType = activityType;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public String getSweatRate() {
		return sweatrate;
	}
	
	public void setSweatRate(String sweatRate) {
		this.sweatrate = sweatRate;
	}

	public double getAvgPace() {
		return avgPace;
	}

	public void setAvgPace(double avgPace) {
		this.avgPace = avgPace;
	}

	public double getAvgSpeed() {
		return avgSpeed;
	}

	public void setAvgSpeed(double avgSpeed) {
		this.avgSpeed = avgSpeed;
	}

	public int getCalorie() {
		return calorie;
	}

	public void setCalorie(int calorie) {
		this.calorie = calorie;
	}

	public double getClimb() {
		return climb;
	}

	public void setClimb(double climb) {
		this.climb = climb;
	}

	public int getHeartrate() {
		return heartrate;
	}

	public void setHeartrate(int heartrate) {
		this.heartrate = heartrate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public int getGender() {
		return gender;
	}
	
	public void setGender(int gender) {
		this.gender = gender;
	}
	
	public int getSkinTone() {
		return skinTone;
	}
	
	public void setSkinTone(int skinTone) {
		this.skinTone = skinTone;
	}
	
	public int getSPF() {
		return spf;
	}
	
	public void setSPF(int spf) {
		this.spf = spf;
	}
	
	public float getClothingCover() {
		return clothingCover;
	}
	
	public void setClothingCover(float clothingCover) {
		this.clothingCover = clothingCover;
	}
	
	/*
	public SpriteHeadApparel.HeadApparelType getHeadApparel() {
		return headApparel;
	}
	
	public void setHeadApparel(SpriteHeadApparel.HeadApparelType headApparel) {
		this.headApparel = headApparel;
	}

	public SpriteUpperApparel.UpperApparelType getUpperApparel() {
		return upperApparel;
	}
	
	public void setUpperApparel(SpriteUpperApparel.UpperApparelType upperApparel) {
		this.upperApparel = upperApparel;
	}
	
	public SpriteLowerApparel.LowerApparelType getLowerApparel() {
		return lowerApparel;
	}
	
	public void setLowerApparel(SpriteLowerApparel.LowerApparelType lowerApparel) {
		this.lowerApparel = lowerApparel;
	}
	*/
	
	@Override
	public String toString() {
		return super.toString();
	}
	
}

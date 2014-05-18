package edu.dartmouth.cs.myruns5;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.os.Parcel;
import android.os.Parcelable;

public class Person implements Parcelable{
	public double cumulativeFaceExposure=0,
			cumulativeNeckExposure=0,
			cumulativeChestExposure=0,
			cumulativeBackExposure=0,
			cumulativeForearmExposure=0,
			cumulativeDorsalHandExposure=0,
			cumulativeLegExposure=0,
			cumulativeHorizontalExposure=0,
			
			//UVI Sample Values
			currentUVISun=0,
			currentUVIShade;
	
	public Person(Parcel in) {			
		this.cumulativeBackExposure = in.readDouble();
		cumulativeChestExposure = in.readDouble();
		cumulativeFaceExposure = in.readDouble();
		cumulativeLegExposure = in.readDouble();
		cumulativeDorsalHandExposure = in.readDouble();
		cumulativeForearmExposure = in.readDouble();
		cumulativeNeckExposure = in.readDouble();
		cumulativeHorizontalExposure = in.readDouble();
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
	    public Person createFromParcel(Parcel in) {
	        return new Person(in);
	    }
	
	    public Person[] newArray(int size) {
	        return new Person[size];
	    }
	};

	public static enum Gender {
		MALE, FEMALE
	}
	
	public Person (){
		this.timestamp = new GregorianCalendar();
	}
	
	// Head apparel for covering face. Numbers represent % of total body surface area covered (ie: face)
	public static enum HeadApparelType {NONE(0f), BASEBALLCAP(2.5f);
		
		float cover;
		HeadApparelType(float cover) {
			this.cover = cover;
		}

		public float getCover() {
			return cover;
		}
		
		public enum LowerApparelMaleType {
			NONE(0f), SHORTS(0.25f), JEANS(0.5f);
			
			float cover;
			LowerApparelMaleType(float cover) {
				this.cover = cover;
			}

			public float getCover() {
				return cover;
			}
		}
		
		public enum LowerApparelFemaleType {
			NONE(0f), BIKINI_BOTTOM(.1f), SHORTS(0.23f), JEANS(0.5f);
			
			float cover;
			LowerApparelFemaleType(float cover) {
				this.cover = cover;
			}

			public float getCover() {
				return cover;
			}
		}
		
		public enum UpperApparelMaleType {
			NONE(0f), TANK_TOP(0.20f),TEESHIRT_SHORTSLEEVE(0.25f), TEESHIRT_LONGSLEEVE(0.35f);

			float cover;
			UpperApparelMaleType(float cover) {
				this.cover = cover;
			}

			public float getCover() {
				return cover;
			}
		}

		public enum UpperApparelFemaleType {
			NONE(0f), BIKINI_TOP(.1f),SPORTS_BRA(0.05f),TANK_TOP(0.20f),TEESHIRT_SHORTSLEEVE(0.25f),TEESHIRT_LONGSLEEVE(0.35f);

			float cover;
			UpperApparelFemaleType(float cover) {
				this.cover = cover;
			}

			public float getCover() {
				return cover;
			}
		}
	}
	
	private Calendar timestamp;
	private float azimuthAngle=Float.MAX_VALUE;
	private float zenithAngle=Float.MAX_VALUE;
	private float elevationAngle=Float.MAX_VALUE;
	
	
	public float getRelativeFaceAngle() {
		return relativeFaceAngle;
	}

	public void setRelativeFaceAngle(float relativeFaceAngle) {
		this.relativeFaceAngle = relativeFaceAngle;
	}

	public float getRelativeChestAngle() {
		return relativeChestAngle;
	}

	public float getRelativeNeckAngle() {
		return relativeNeckAngle;
	}

	public float getRelativeDorsalHandAngle() {
		return relativeDorsalHandAngle;
	}

	public float getRelativeForearmAngle() {
		return relativeForearmAngle;
	}

	public float getRelativeBackAngle() {
		return relativeBackAngle;
	}

	public float getRelativeLegAngle() {
		return relativeLegAngle;
	}

	private float relativeFaceAngle=.26f;
	private float relativeNeckAngle=.23f;
	private float relativeChestAngle=.23f;
	private float relativeBackAngle=.23f;
	private float relativeForearmAngle=.13f;
	private float relativeDorsalHandAngle=.30f;
	private float relativeLegAngle=.12f;

	public void updateCumulativeUVExposure(String environmentClassification) {
		Calendar time = new GregorianCalendar();
		double timeElapsedSeconds = (time.getTimeInMillis() - timestamp.getTimeInMillis())/Globals.ONE_SECOND;
		// Calculate running total of UV Exposure w.r.t. second
		double uviValue = 0;
		if (environmentClassification.equals(Globals.CLASS_LABEL_IN_SUN)){
			uviValue = currentUVISun;
		}else if (environmentClassification.equals(Globals.CLASS_LABEL_IN_SHADE)){
			uviValue = currentUVIShade;
		}else if (environmentClassification.equals(Globals.CLASS_LABEL_IN_DOORS)){
			uviValue = 0;
		}
		
		updateRelativeExposurePercentages();
		cumulativeHorizontalExposure+= uviValue * timeElapsedSeconds;
		cumulativeFaceExposure+= uviValue * relativeFaceAngle * timeElapsedSeconds;
		cumulativeNeckExposure+= uviValue * relativeNeckAngle * timeElapsedSeconds;
		cumulativeChestExposure+= uviValue * relativeChestAngle * timeElapsedSeconds;
		cumulativeBackExposure+= uviValue * relativeBackAngle * timeElapsedSeconds;
		cumulativeForearmExposure+= uviValue * relativeForearmAngle * timeElapsedSeconds;
		cumulativeDorsalHandExposure+= uviValue * relativeDorsalHandAngle * timeElapsedSeconds;
		cumulativeLegExposure+= uviValue * relativeLegAngle * timeElapsedSeconds;

		timestamp = time;
	}
	
	private void updateRelativeExposurePercentages() {
		if(this.zenithAngle >= 0 && this.zenithAngle <= 30){
			relativeFaceAngle=.26f;
			relativeNeckAngle=.23f;
			relativeChestAngle=.23f;
			relativeBackAngle=.23f;
			relativeForearmAngle=.13f;
			relativeDorsalHandAngle=.30f;
			relativeLegAngle=.12f;
		}else if(this.zenithAngle >= 31 && this.zenithAngle <= 50){
			relativeFaceAngle=.39f;
			relativeNeckAngle=.36f;
			relativeChestAngle=.36f;
			relativeBackAngle=.36f;
			relativeForearmAngle=.17f;
			relativeDorsalHandAngle=.35f;
			relativeLegAngle=.23f;
		}else if(this.zenithAngle >= 51 && this.zenithAngle <= 80){
			relativeFaceAngle=.48f;
			relativeNeckAngle=.59f;
			relativeChestAngle=.59f;
			relativeBackAngle=.59f;
			relativeForearmAngle=.41f;
			relativeDorsalHandAngle=.42f;
			relativeLegAngle=.47f;
		}
	}

	public double getCumulativeChestExposure() {
		return cumulativeChestExposure;
	}

	public void setCumulativeChestExposure(double cumulativeChestExposure) {
		this.cumulativeChestExposure = cumulativeChestExposure;
	}

	public double getCumulativeFaceExposure() {
		return cumulativeFaceExposure;
	}

	public double getCumulativeNeckExposure() {
		return cumulativeNeckExposure;
	}

	public double getCumulativeBackExposure() {
		return cumulativeBackExposure;
	}

	public double getCumulativeForearmExposure() {
		return cumulativeForearmExposure;
	}

	public double getCumulativeDorsalHandExposure() {
		return cumulativeDorsalHandExposure;
	}

	public double getCumulativeLegExposure() {
		return cumulativeLegExposure;
	}

	public void setCurrentUVIShade(double mCurrentUVIShade) {
		currentUVIShade = mCurrentUVIShade;
	}
	
	public void setCurrentUVISun(double mCurrentUVISun) {
		currentUVISun = mCurrentUVISun;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) { dest.writeStringArray(new String[] {
			Double.toString(this.cumulativeBackExposure),
			Double.toString(this.cumulativeChestExposure),
			Double.toString(this.cumulativeFaceExposure),
			Double.toString(this.cumulativeLegExposure),
			Double.toString(this.cumulativeDorsalHandExposure),
			Double.toString(this.cumulativeForearmExposure),
			Double.toString(this.cumulativeNeckExposure),
			Double.toString(this.cumulativeHorizontalExposure)});
	}

	public void updateZenithAngle(float zenithAngle) {
		this.zenithAngle=zenithAngle;
	}
	public void updateElevationAngle(float elevationAngle) {
		this.elevationAngle=elevationAngle;
	}
	public void updateAzimuthAngle(float azimuthAngle) {
		this.azimuthAngle=azimuthAngle;
	}

	public double getCumulativeHorizontalExposure() {
		return this.cumulativeHorizontalExposure;
	}
}

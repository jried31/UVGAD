package edu.dartmouth.cs.myruns5;

public class LumenDataPoint {

    private long timestamp;
    public long getTimestamp() {
      return timestamp;
    }

    public double getPitch() {
      return pitch;
    }

    public double getIntensity() {
      return intensity;
    }

    public float getUvi() {
      return uvi;
    }

    private double pitch;
    private double intensity;
    private float uvi;
    
    public LumenDataPoint(long timestamp, double pitch, double intensity, float uvi) {
      super();
      this.timestamp = timestamp;
      this.pitch = pitch;
      this.intensity = intensity;
      this.uvi = uvi;
    }
}

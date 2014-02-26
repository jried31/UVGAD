package edu.dartmouth.cs.myrunscollector;

public class LumenDataPoint {

    private long timestamp;
    public long getTimestamp() {
      return timestamp;
    }

    public float[] getPitch() {
      return pitch;
    }

    public double getIntensity() {
      return intensity;
    }


    private float pitch[];
    private double intensity;
    
    public LumenDataPoint(long timestamp, float[] pitchReading, double intensity) {
      super();
      this.timestamp = timestamp;
      this.pitch = pitchReading;
      this.intensity = intensity;
    }
}

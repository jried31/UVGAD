package edu.dartmouth.cs.myruns5;

public class LumenDataPoint {

    private long timestamp;
    public long getTimestamp() {
      return timestamp;
    }

    public double[] getPitch() {
      return pitch;
    }

    public double getIntensity() {
      return intensity;
    }
    private double pitch[];
    private double intensity;
    
    public LumenDataPoint(long timestamp, double[] pitch, double intensity) {
      super();
      this.timestamp = timestamp;
      this.pitch = pitch;
      this.intensity = intensity;
    }
}

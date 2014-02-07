package edu.dartmouth.cs.myruns5;

/**
 * Interface for caller of this dialog fragment.
 * 
 * @author apoorvn
 */
public interface ChooseSPFLevelCaller {

  /**
   * Called when choose spf level is done.
   */
  public void onChooseSPFLevelDone(String iconValue);
  public void onChooseSPFLevelDone(int position);
}
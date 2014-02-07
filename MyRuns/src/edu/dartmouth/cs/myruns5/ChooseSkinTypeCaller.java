package edu.dartmouth.cs.myruns5;

/**
 * Interface for caller of this dialog fragment.
 * 
 * @author apoorvn
 */
public interface ChooseSkinTypeCaller {

  /**
   * Called when choose spf level is done.
   */
  public void onChooseSkinTypeDone(String iconValue);
  public void onChooseSkinTypeDone(int position);
}
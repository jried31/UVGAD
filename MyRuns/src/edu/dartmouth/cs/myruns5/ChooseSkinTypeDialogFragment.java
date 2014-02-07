/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.dartmouth.cs.myruns5;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

/**
 * A DialogFragment to choose an skin type.
 * 
 * @author apoorvn
 */
public class ChooseSkinTypeDialogFragment extends DialogFragment {

  /**
   * Interface for caller of this dialog fragment.
   * 
   * @author apoorvn
   */
  public interface ChooseSkinTypeCaller {

    /**
     * Called when choose skin type is done.
     */
    public void onChooseSkinTypeDone(String iconValue);
  }

  public static final String CHOOSE_SKIN_TYPE_DIALOG_TAG = "chooseSkinType";

  private ChooseSkinTypeCaller caller;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      caller = (ChooseSkinTypeCaller) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement "
          + ChooseSkinTypeCaller.class.getSimpleName());
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    GridView gridView = (GridView) getActivity()
        .getLayoutInflater().inflate(R.layout.choose_skin_type, null);

    final List<String> iconValues = SkinTypeIconUtils.getAllIconValues();
    List<Integer> imageIds = new ArrayList<Integer>();
    for (String iconValue : iconValues) {
      imageIds.add(SkinTypeIconUtils.getIconDrawable(iconValue));
    }

    ChooseSkinTypeImageAdapter imageAdapter = new ChooseSkinTypeImageAdapter(
        getActivity(), imageIds);
    gridView.setAdapter(imageAdapter);
    gridView.setOnItemClickListener(new OnItemClickListener() {
        @Override
      public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        dismiss();
        caller.onChooseSkinTypeDone(iconValues.get(position));
      }
    });

    return new AlertDialog.Builder(getActivity()).setNegativeButton(R.string.cancel, null)
        .setTitle(R.string.uvg_skin_type_hint).setView(gridView).create();
  }
}
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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Image adapter for choosing an activity type.
 * 
 * @author apoorvn
 */
public class ChooseSkinTypeImageAdapter extends BaseAdapter {

  private final Context context;
  private final List<Integer> imageIds;

  public ChooseSkinTypeImageAdapter(Context context, List<Integer> imageIds) {
    this.context = context;
    this.imageIds = imageIds;
  }

  @Override
  public int getCount() {
    return imageIds.size();
  }

  @Override
  public Object getItem(int position) {
    return null;
  }

  @Override
  public long getItemId(int position) {
    return 0;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ImageView imageView;
    if (convertView == null) {
      imageView = new ImageView(context);
    } else {
      imageView = (ImageView) convertView;
    }
    imageView.setImageResource(imageIds.get(position));
    return imageView;
  }
}

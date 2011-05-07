/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.gsapidemo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.gesturesearchapidemo.R;

/**
 * The example shows how to use Gesture Search in an application and to search
 * application specific data.
 */
public class GestureSearchAPIDemo extends Activity {
  private static final int GESTURE_SEARCH_ID = 1;
  /** 
   * Optionally, specify what should be shown when launching Gesture Search.
   * If this is not specified, SHOW_HISTORY will be used as a default value.
   */
  private static String SHOW_MODE = "show";
  /** Possible values for invoking mode */
  // Show the visited items
  private static final int SHOW_HISTORY = 0;
  // Show nothing (a blank screen)
  private static final int SHOW_NONE = 1;
  // Show all of date items
  private static final int SHOW_ALL = 2;
  
  /**
   * The theme of Gesture Search can be light or dark
   */
  private static final String THEME = "theme";
  private static final int THEME_LIGHT = 0;
  private static final int THEME_DARK = 1;
  
  /** Keys for results returned by Gesture Search */
  private static final String SELECTED_ITEM_ID = "selected_item_id";
  private static final String SELECTED_ITEM_NAME = "selected_item_name";
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(0, GESTURE_SEARCH_ID, 0, R.string.menu_gesture_search)
        .setShortcut('0', 'g').setIcon(android.R.drawable.ic_menu_search);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case GESTURE_SEARCH_ID:
        try {
          Intent intent = new Intent();
          intent.setAction("com.google.android.apps.gesturesearch.SEARCH");
          intent.setData(CountryProvider.CONTENT_URI);
          intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          intent.putExtra(SHOW_MODE, SHOW_ALL);
          intent.putExtra(THEME, THEME_LIGHT);
          startActivityForResult(intent, GESTURE_SEARCH_ID);
        } catch (ActivityNotFoundException e) {
          Log.e("GestureSearchExample", "Gesture Search is not installed");
        }
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK) {
      switch (requestCode) {
        case GESTURE_SEARCH_ID:
          long selectedItemId = data.getLongExtra(SELECTED_ITEM_ID, -1);
          String selectedItemName = data.getStringExtra(SELECTED_ITEM_NAME);
          // Print out the Id and name of the item that is selected 
          // by the user in Gesture Search
          Log.d("GestureSearchExample", selectedItemId + ": " + selectedItemName);
          break;
      }   
    }
  }
}

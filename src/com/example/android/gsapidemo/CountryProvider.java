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

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.gesturesearchapidemo.R;

import java.util.HashMap;

/**
 * This example shows how to create a data provider that can be searched by
 * Gesture Search.
 */
public class CountryProvider extends ContentProvider {

  static final String AUTHORITY = "com.example.android.gsapidemo.suggestion";
  private DatabaseHelper mOpenHelper;
  private static final String DATABASE_NAME = "suggestion.db";
  private static final int DATABASE_VERSION = 2;
  private static final String TAG = "SuggestionProvider";
  private static final String DEFAULT_SUGGESTION_SORT_ORDER = "_id ASC";
  
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/suggestion");

  public static final String CONTENT_TYPE = 
      "vnd.android.cursor.dir/vnd.android.example.gsapidemo.suggestion";

  public static final String CONTENT_ITEM_TYPE = 
      "vnd.android.cursor.item/vnd.android.example.gsapidemo.suggestion";

  private static final String TABLE_NAME = "suggestion";

  private static HashMap<String, String> sSuggestionProjectionMap;
  static {
    sSuggestionProjectionMap = new HashMap<String, String>();
    sSuggestionProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
    sSuggestionProjectionMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, 
        SearchManager.SUGGEST_COLUMN_TEXT_1);
    sSuggestionProjectionMap.put(SearchManager.SUGGEST_COLUMN_TEXT_2, 
        SearchManager.SUGGEST_COLUMN_TEXT_2);
    sSuggestionProjectionMap.put(SearchManager.SUGGEST_COLUMN_ICON_1, 
        SearchManager.SUGGEST_COLUMN_ICON_1);
  }

  private static final int URI_MATCH_SUGGESTION = 0;

  private static final int URI_MATCH_SUGGESTION_ID = 10;

  private static final UriMatcher sUriMatcher;
  static {
    sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    sUriMatcher.addURI(AUTHORITY, TABLE_NAME, URI_MATCH_SUGGESTION);
    sUriMatcher.addURI(AUTHORITY, TABLE_NAME + "/#", URI_MATCH_SUGGESTION_ID);
  }

  static final String[] COUNTRIES = new String[] {
    "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra",
    "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina",
    "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan",
    "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium",
    "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia",
    "Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory",
    "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burundi",
    "Cote d'Ivoire", "Cambodia", "Cameroon", "Canada", "Cape Verde",
    "Cayman Islands", "Central African Republic", "Chad", "Chile", "China",
    "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo",
    "Cook Islands", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czech Republic",
    "Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic",
    "East Timor", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea",
    "Estonia", "Ethiopia", "Faeroe Islands", "Falkland Islands", "Fiji", "Finland",
    "Former Yugoslav Republic of Macedonia", "France", "French Guiana", "French Polynesia",
    "French Southern Territories", "Gabon", "Georgia", "Germany", "Ghana", "Gibraltar",
    "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau",
    "Guyana", "Haiti", "Heard Island and McDonald Islands", "Honduras", "Hong Kong", "Hungary",
    "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica",
    "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Kuwait", "Kyrgyzstan", "Laos",
    "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg",
    "Macau", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands",
    "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia", "Moldova",
    "Monaco", "Mongolia", "Montserrat", "Morocco", "Mozambique", "Myanmar", "Namibia",
    "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand",
    "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "North Korea", "Northern Marianas",
    "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru",
    "Philippines", "Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar",
    "Reunion", "Romania", "Russia", "Rwanda", "Sqo Tome and Principe", "Saint Helena",
    "Saint Kitts and Nevis", "Saint Lucia", "Saint Pierre and Miquelon",
    "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Saudi Arabia", "Senegal",
    "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands",
    "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "South Korea",
    "Spain", "Sri Lanka", "Sudan", "Suriname", "Svalbard and Jan Mayen", "Swaziland", "Sweden",
    "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "The Bahamas",
    "The Gambia", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey",
    "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Virgin Islands", "Uganda",
    "Ukraine", "United Arab Emirates", "United Kingdom",
    "United States", "United States Minor Outlying Islands", "Uruguay", "Uzbekistan",
    "Vanuatu", "Vatican City", "Venezuela", "Vietnam", "Wallis and Futuna", "Western Sahara",
    "Yemen", "Yugoslavia", "Zambia", "Zimbabwe"
  };
  
  /**
   * This class helps open, create, and upgrade the database file.
   */
  private static class DatabaseHelper extends SQLiteOpenHelper {
    
    DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + BaseColumns._ID
          + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
          SearchManager.SUGGEST_COLUMN_TEXT_1 + " TEXT, " + 
          SearchManager.SUGGEST_COLUMN_TEXT_2 + " TEXT, " + 
          SearchManager.SUGGEST_COLUMN_ICON_1 + " TEXT);");
      ContentValues map = new ContentValues();
      for (String country : COUNTRIES) {
        // Each item needs to have a name
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, country);
        // Optionally, an item can have additional information and an icon
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_2, "additional information about the item");
        map.put(SearchManager.SUGGEST_COLUMN_ICON_1, R.raw.search);
        db.insert(TABLE_NAME, SearchManager.SUGGEST_COLUMN_TEXT_1, map);
      }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
      db.execSQL("DROP TABLE IF EXISTS suggestion");
      onCreate(db);
    }
  }

  @Override
  public boolean onCreate() {
    mOpenHelper = new DatabaseHelper(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
    int match = sUriMatcher.match(uri);
    if (match == -1) {
      throw new IllegalArgumentException(uri + " -- Unknown URL");
    }

    String orderBy = sortOrder;
    if (match == URI_MATCH_SUGGESTION || match == URI_MATCH_SUGGESTION_ID) {
      qb.setProjectionMap(sSuggestionProjectionMap);
      if (TextUtils.isEmpty(orderBy)) {
        orderBy = DEFAULT_SUGGESTION_SORT_ORDER;
      }
    }

    SQLiteDatabase db = mOpenHelper.getReadableDatabase();
    qb.setTables(TABLE_NAME);
    Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

    c.setNotificationUri(getContext().getContentResolver(), uri);
    return c;
  }

  @Override
  public String getType(Uri url) {
    int match = sUriMatcher.match(url);
    switch (match) {

      case URI_MATCH_SUGGESTION:
        return CONTENT_TYPE;

      case URI_MATCH_SUGGESTION_ID:
        return CONTENT_ITEM_TYPE;

      default:
        throw new IllegalArgumentException("Unknown URL");
    }
  }

  @Override
  public Uri insert(Uri url, ContentValues initialValues) {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    int match = sUriMatcher.match(url);
    Uri uri = null;
    switch (match) {

      case URI_MATCH_SUGGESTION: {
        long rowID = db.insert(TABLE_NAME, CONTENT_ITEM_TYPE, initialValues);
        if (rowID > 0) {
          uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
        }
        break;
      }

      default:
        throw new IllegalArgumentException("Unknown URL");
    }

    if (uri == null) {
      throw new IllegalArgumentException("Unknown URL");
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return uri;
  }


  @Override
  public int delete(Uri url, String where, String[] whereArgs) {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();

    int match = sUriMatcher.match(url);
    if (match == -1) {
      throw new IllegalArgumentException("Unknown URL");
    }

    if (url.getPathSegments().size() > 1) {
      StringBuilder sb = new StringBuilder();
      if (where != null && where.length() > 0) {
        sb.append("( ");
        sb.append(where);
        sb.append(" ) AND ");
      }
      sb.append("_id = ");
      sb.append(url.getPathSegments().get(1));
      where = sb.toString();
    }

    int count = db.delete(TABLE_NAME, where, whereArgs);
    getContext().getContentResolver().notifyChange(url, null);
    return count;
  }

  @Override
  public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
    SQLiteDatabase db = mOpenHelper.getWritableDatabase();

    int match = sUriMatcher.match(url);
    if (match == -1) {
      throw new IllegalArgumentException("Unknown URL");
    }

    if (url.getPathSegments().size() > 1) {
      StringBuilder sb = new StringBuilder();
      if (where != null && where.length() > 0) {
        sb.append("( ");
        sb.append(where);
        sb.append(" ) AND ");
      }
      sb.append("_id = ");
      sb.append(url.getPathSegments().get(1));
      where = sb.toString();
    }

    int ret = db.update(TABLE_NAME, values, where, whereArgs);
    getContext().getContentResolver().notifyChange(url, null);
    return ret;
  }
}

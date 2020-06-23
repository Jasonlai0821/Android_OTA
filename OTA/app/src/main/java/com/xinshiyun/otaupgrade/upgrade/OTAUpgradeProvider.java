package com.xinshiyun.otaupgrade.upgrade;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class OTAUpgradeProvider extends ContentProvider implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = "com.skyworth.ota";

    private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        matcher.addURI(TAG, "isExist", 1);
    }

    @Override
    public boolean onCreate() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("update", Context.MODE_MULTI_PROCESS);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = null;
        if (matcher.match(uri) == 1) {
            cursor = new MatrixCursor(new String[]{"_COUNT"});
            cursor.addRow(new Object[]{OTAUpgradeSharePreference.getSysUpgradeExist(getContext())});
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("exist")){
            getContext().getContentResolver().notifyChange(Uri.parse("content://com.skyworth.ota/isExist"), null);
        }
    }
}

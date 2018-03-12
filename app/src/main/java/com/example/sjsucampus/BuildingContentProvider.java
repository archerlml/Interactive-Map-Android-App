package com.example.sjsucampus;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import com.example.sjsucampus.map.MapConfig;
import com.example.sjsucampus.map.MapControllor;
import com.example.sjsucampus.util.Log;

import java.util.List;

/**
 * Created by chitoo on 10/29/16.
 */

public class BuildingContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.example.google.places.search_suggestion_provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/search");
    private static final int SEARCH_SUGGEST = 1;
    private static final UriMatcher uriMatcher;
    private static final String[] SEARCH_SUGGEST_COLUMNS = {
            BaseColumns._ID,
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID
    };

    @Override
    public boolean onCreate() {
        return true;
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        switch (uriMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                String query = uri.getLastPathSegment().toLowerCase();
                Log.i("query = " + query);
                MatrixCursor cursor = new MatrixCursor(SEARCH_SUGGEST_COLUMNS, 1);

                List<MapConfig.Building> buildingList = MapControllor.get().getMapConfig().buildings;
                for (int i = 0; i < buildingList.size(); ++i) {
                    MapConfig.Building building = buildingList.get(i);
                    if (building.name.toLowerCase().indexOf(query) >= 0 ||
                            building.abbr.toLowerCase().indexOf(query) >= 0) {
                        cursor.addRow(new String[]{String.valueOf(cursor.getCount() + 1), building.abbr, building.name, String.valueOf(i)});
                    }
                }
                return cursor;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Nullable
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
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SEARCH_SUGGEST:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }
}

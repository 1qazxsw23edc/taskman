package com.charles.taskmantest.datahandler;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by charles on 11/10/13.
 */
public class TaskManContentProvider extends ContentProvider {

    private TaskManDatabaseHelper database;

    //used for UriMatcher
    private static final int FENCES = 10;
    private static final int FENCES_ID = 20;
    private static final int INGRESS = 30;
    private static final int INGRESS_ID = 40;
    private static final int EGRESS = 50;
    private static final int EGRESS_ID = 60;

    private static final String AUTHORITY = "com.charles.taskmantest.datahandler.TaskManContentProvider";

    private static final String FENCE_PATH = "fences_table";
    public static final Uri FENCE_URI = Uri.parse("content://" + AUTHORITY + "/" + FENCE_PATH);

    private static final String INGRESS_PATH = "ingress_table";
    public static final Uri INGRESS_URI = Uri.parse("content://" + AUTHORITY + "/" + INGRESS_PATH);

    private static final String EGRESS_PATH = "egress_table";
    public static final Uri EGRESS_URI = Uri.parse("content://" + AUTHORITY + "/" + EGRESS_PATH);


    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/fences_tables";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/fences_table";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, FENCE_PATH, FENCES);
        sURIMatcher.addURI(AUTHORITY, FENCE_PATH + "/#", FENCES_ID);
        sURIMatcher.addURI(AUTHORITY, INGRESS_PATH, INGRESS);
        sURIMatcher.addURI(AUTHORITY, INGRESS_PATH + "/#", INGRESS_ID);
        sURIMatcher.addURI(AUTHORITY, EGRESS_PATH, EGRESS);
        sURIMatcher.addURI(AUTHORITY, EGRESS_PATH + "/#", EGRESS_ID);
    }
    public boolean onCreate() {
        Log.v("Content Provider", "Content Provider Created");
        database = new TaskManDatabaseHelper(getContext());
        database.getWritableDatabase();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //Using SQLiteQueryBuilder instad of query() methos
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        //check if the caller has requested a column which does not exist
        checkColumns(projection);

        int uriType = sURIMatcher.match(uri);
        switch(uriType) {
            case FENCES_ID:
                //adding the ID to the original query
                queryBuilder.appendWhere(GeoFenceTable.ID + "=" + uri.getLastPathSegment());
                //$FALL-THROUGH$
            case FENCES:
                queryBuilder.setTables(GeoFenceTable.TABLE_GEOFENCE);
                break;
            case INGRESS_ID:
                queryBuilder.appendWhere(IngressTable.ID + "=" + uri.getLastPathSegment());
            case INGRESS:
                queryBuilder.setTables(IngressTable.INGRESS_TABLE);
                break;
            case EGRESS_ID:
                queryBuilder.appendWhere(EgressTable.ID + "=" + uri.getLastPathSegment());
            case EGRESS:
                queryBuilder.setTables(EgressTable.EGRESS_TABLE);
                break;
            default:
                throw new IllegalArgumentException("Unkown URI: " + uri);

        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        //make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        int rowsDeleted = 0;
        Uri returnUri = null;
        long id = 0;
        switch (uriType) {
            case FENCES:
                id = db.insert(GeoFenceTable.TABLE_GEOFENCE, null, values);
                returnUri = Uri.parse(FENCES + "/" + id);
                break;
            case INGRESS:
                id = db.insert(IngressTable.INGRESS_TABLE, null, values);
                returnUri = Uri.parse(INGRESS + "/" + id);
                break;
            case EGRESS:
                id = db.insert(EgressTable.EGRESS_TABLE, null, values);
                returnUri = Uri.parse(EGRESS + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Log.v("Insertion", "Successful Insertion");
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        int rowsDeleted = 0;
        String id = null;
        switch (uriType) {
            case FENCES:
                rowsDeleted = db.delete(GeoFenceTable.TABLE_GEOFENCE, selection, selectionArgs);
                break;
            case FENCES_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(GeoFenceTable.TABLE_GEOFENCE, GeoFenceTable.ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(GeoFenceTable.TABLE_GEOFENCE, GeoFenceTable.ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case INGRESS:
                rowsDeleted = db.delete(IngressTable.INGRESS_TABLE, selection, selectionArgs);
                break;
            case INGRESS_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(IngressTable.INGRESS_TABLE, IngressTable.ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(IngressTable.INGRESS_TABLE, IngressTable.ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case EGRESS:
                rowsDeleted = db.delete(EgressTable.EGRESS_TABLE, selection, selectionArgs);
                break;
            case EGRESS_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(EgressTable.EGRESS_TABLE, EgressTable.ID + "=" + id, null);
                } else {
                    rowsDeleted = db.delete(EgressTable.EGRESS_TABLE, EgressTable.ID     + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Uknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase db = database.getWritableDatabase();
        String id = null;
        int rowsUpdated = 0;
        switch (uriType) {
            case FENCES:
                rowsUpdated = db.update(GeoFenceTable.TABLE_GEOFENCE, values, selection, selectionArgs);
                break;
            case FENCES_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(GeoFenceTable.TABLE_GEOFENCE, values, GeoFenceTable.ID + "=" + id, null);
                } else {
                    rowsUpdated = db.update(GeoFenceTable.TABLE_GEOFENCE, values, GeoFenceTable.ID + "=" + id
                    + " and "
                    + selection,
                    selectionArgs);
                }
                break;
            case INGRESS:
                rowsUpdated = db.update(IngressTable.INGRESS_TABLE, values, selection, selectionArgs);
                break;
            case INGRESS_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(IngressTable.INGRESS_TABLE, values, IngressTable.ID  + "=" +id, null);
                } else {
                    rowsUpdated = db.update(IngressTable.INGRESS_TABLE, values, IngressTable.ID + "=" +id
                    + " and "
                    + selection,
                    selectionArgs);
                }
                break;
            case EGRESS:
                rowsUpdated = db.update(EgressTable.EGRESS_TABLE, values, selection, selectionArgs);
                break;
            case EGRESS_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(EgressTable.EGRESS_TABLE, values, EgressTable.ID + "=" + id, null);
                } else {
                    rowsUpdated = db.update(EgressTable.EGRESS_TABLE, values, EgressTable.ID + "=" + id
                    + " and "
                    + selection,
                    selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unkown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = {GeoFenceTable.LONGITUDE, GeoFenceTable.LATITUDE, GeoFenceTable.EXPIRATION,
        GeoFenceTable.ID, GeoFenceTable.RADIUS, GeoFenceTable.TRANSITION, GeoFenceTable.NAME, EgressTable.ID,
                EgressTable.CONSTRUCT, IngressTable.CONSTRUCT, IngressTable.ID, };
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            //check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}

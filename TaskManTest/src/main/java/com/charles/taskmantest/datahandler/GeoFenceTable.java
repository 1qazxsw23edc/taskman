package com.charles.taskmantest.datahandler;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by charles on 11/10/13.
 */
public class GeoFenceTable {

    public static final String TABLE_GEOFENCE = "fences_table";
    public static final String ID = "id";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String RADIUS = "radius";
    public static final String EXPIRATION = "exipiration";
    public static final String TRANSITION = "transition";


    private static final String DATABASE_CREATE = "create table "
            + TABLE_GEOFENCE
            +"("
            + ID + " text primary key not null, "
            + LATITUDE + " real not null, "
            + LONGITUDE + " real not null, "
            + RADIUS + " real not null, "
            + EXPIRATION + " integer default null,"
            + TRANSITION + " integer default null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(GeoFenceTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GEOFENCE);
        onCreate(database);
    }
}
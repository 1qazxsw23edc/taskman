package com.charles.taskmantest.datahandler;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by charles on 11/10/13.
 * This is the primary table, it stores the main information about each of the fences.
 */
public class GeoFenceTable {

    public static final String TABLE_GEOFENCE = "fences_table";
    public static final String ID = "_id";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String RADIUS = "radius";
    public static final String EXPIRATION = "exipiration";
    public static final String TRANSITION = "transition";
    public static final String NAME = "name";


    private static final String DATABASE_CREATE = "create table "
            + TABLE_GEOFENCE
            +"("
            + ID + " integer primary key autoincrement, "
            + NAME + " text not null, "
            + LATITUDE + " real not null default 0, "
            + LONGITUDE + " real not null default 0, "
            + RADIUS + " real not null default 100, "
            + EXPIRATION + " integer default null,"
            + TRANSITION + " integer default null"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    //This is the method to modify the table when I create a new version
    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(GeoFenceTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GEOFENCE);
        onCreate(database);
    }
}

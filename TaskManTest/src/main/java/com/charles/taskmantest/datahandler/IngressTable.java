package com.charles.taskmantest.datahandler;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by charles on 11/10/13.
 */
public class IngressTable {
    public static final String INGRESS_TABLE = "ingress_table";
    public static final String ID = "_id";
    public static final String AIRPLANE = "airplane";
    public static final String WIFI = "wifi";
    public static final String SMS = "sms";
    public static final String TIMEFRAME = "timeframe";
    public static final String SOUND = "sound";

    private static final String DATABASE_CREATE = "create table "
            + INGRESS_TABLE
            +"("
            + ID + " integer not null, "
            + AIRPLANE + " integer, "
            + WIFI + " text, "
            + SMS + " text, "
            + TIMEFRAME + " text, "
            + SOUND  + " text "
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(GeoFenceTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + INGRESS_TABLE);
        onCreate(database);
    }

}

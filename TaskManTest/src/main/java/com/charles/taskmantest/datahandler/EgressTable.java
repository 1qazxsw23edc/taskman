package com.charles.taskmantest.datahandler;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by charles on 11/10/13.
 * Creates a table to manage the data that drives the actions taken when you leave a fenced area
 */
public class EgressTable {
    public static final String EGRESS_TABLE = "egress_table";
    public static final String ID = "_id";
    public static final String CONSTRUCT = "construct";

    private static final String DATABASE_CREATE = "create table "
            + EGRESS_TABLE
            +"("
            + ID + " integer primary key not null, "
            + CONSTRUCT + " text"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        Log.v("SQL", DATABASE_CREATE);
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(GeoFenceTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + EGRESS_TABLE);
        onCreate(database);
    }
}

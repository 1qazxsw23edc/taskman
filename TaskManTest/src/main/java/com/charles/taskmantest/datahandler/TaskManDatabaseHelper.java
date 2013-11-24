package com.charles.taskmantest.datahandler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by charles on 11/10/13.
 */
public class TaskManDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "taskman.db";
    private static final int DATABASE_VERSION = 7;

    public TaskManDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    //Called during creation of database
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v("Creating DB", "Creating DB");
        GeoFenceTable.onCreate(db);
        IngressTable.onCreate(db);
        EgressTable.onCreate(db);
    }

    //Called during upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v("Upgrading Database", "Upgrading Database");
        GeoFenceTable.onUpgrade(db, oldVersion, newVersion);
        IngressTable.onUpgrade(db, oldVersion, newVersion);
        EgressTable.onUpgrade(db, oldVersion, newVersion);
    }
}

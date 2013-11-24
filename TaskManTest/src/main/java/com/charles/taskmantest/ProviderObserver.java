package com.charles.taskmantest;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Created by charles on 11/18/13.
 */
public class ProviderObserver extends ContentObserver {
    public ProviderObserver(Handler handler) {
        super(handler);
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        //Do stuff here
    }
}

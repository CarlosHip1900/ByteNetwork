package com.carloship.craft.sdk.database.local;


import com.carloship.craft.sdk.database.SDKSyncDatabase;

import java.time.Duration;

public abstract class FileDatabase<O> implements SDKSyncDatabase<O> {

    protected abstract void refreshDatabase();

    @Deprecated
    @Override
    public boolean save(String key, O value, Duration timeout) {
        return false;
    }

    @Deprecated
    @Override
    public boolean delete(String key) {
        return false;
    }
}

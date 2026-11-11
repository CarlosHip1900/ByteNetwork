package com.carloship.craft.sdk.database;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SDKAsyncDatabase<T> {

    CompletableFuture<T> fetchAsync(String key);

    CompletableFuture<List<T>> fetchAllAsync();

    CompletableFuture<Boolean> saveAsync(String key, T value);

    CompletableFuture<Boolean> saveAsync(String key, T value, Duration timeout);

    CompletableFuture<Boolean> deleteAsync(String key);

}

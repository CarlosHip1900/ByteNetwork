package com.carloship.craft.sdk.database.cloud;

import com.carloship.craft.sdk.annotations.database.Column;
import com.carloship.craft.sdk.annotations.database.MongoId;
import com.carloship.craft.sdk.database.SDKAsyncDatabase;
import com.carloship.craft.sdk.database.SDKSyncDatabase;
import com.carloship.craft.sdk.exception.MongoDatabaseException;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.Sorts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import com.carloship.craft.sdk.mapper.SDKMongoMapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.bson.codecs.configuration.CodecRegistries.*;

@Slf4j
public class MongoDatabase<T> implements SDKAsyncDatabase<T>, SDKSyncDatabase<T> {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    @Getter
    private final MongoCollection<T> collection;

    private final Field idField;
    private final String mongoIdName;

    public MongoDatabase(
            MongoClient client,
            String database,
            Class<T> clazz,
            String collectionName) {
        CodecRegistry codecRegistry = buildCodecRegistry(clazz);

        this.collection = client
                .getDatabase(database)
                .withCodecRegistry(codecRegistry)
                .getCollection(collectionName, clazz);

        this.idField = findIdField(clazz);
        this.idField.setAccessible(true);
        this.mongoIdName = resolveIdName(idField);
    }

    private CodecRegistry buildCodecRegistry(Class<T> clazz) {
        CodecRegistry customCodecs = fromCodecs(SDKMongoMapper.toArray());

        CodecRegistry pojoRegistry = fromProviders(
                PojoCodecProvider.builder()
                        .automatic(true)
                        .build()
        );

        return fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                customCodecs,
                pojoRegistry
        );
    }

    public void indexes(List<IndexModel> indexes) {
        collection.createIndexes(indexes);
    }

    public void indexes(IndexModel... indexes) {
        collection.createIndexes(List.of(indexes));
    }

    private Object convertKey(String key) {
        Class<?> type = idField.getType();

        try {
            if (type == UUID.class) {
                return UUID.fromString(key);
            }
            if (type == Long.class || type == long.class) {
                return Long.parseLong(key);
            }
            if (type == Integer.class || type == int.class) {
                return Integer.parseInt(key);
            }
            return key;
        } catch (Exception e) {
            throw new MongoDatabaseException("Invalid key type for id field: " + key, e);
        }
    }


    private String resolveIdName(Field idField) {
        if (idField.isAnnotationPresent(MongoId.class)) {
            return "_id";
        }
        if (idField.isAnnotationPresent(Column.class)) {
            return idField.getAnnotation(Column.class).name();
        }
        return "_id";
    }

    private Field findIdField(Class<T> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(MongoId.class))
                return field;
            if (field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).id())
                return field;
        }
        throw new IllegalStateException("No @MongoId or @Column(id=true) found in class " + clazz.getName());
    }

    private Object getIdValue(T object) {
        try {
            return idField.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access id field", e);
        }
    }

    @Override
    public T fetch(String key) {
        try {
            Object id = convertKey(key);
            return collection.find(Filters.eq(mongoIdName, id)).first();
        } catch (Exception e) {
            throw new MongoDatabaseException("Cannot find id field " + e.getMessage());
        }
    }

    public T fetchFilter(Bson filters) {
        try {
            return collection.find(filters).first();
        } catch (Exception e) {
            throw new MongoDatabaseException("Cannot find id field", e);
        }
    }

    @Override
    public List<T> fetchAll() {
        try {
            List<T> list = new ArrayList<>();
            collection.find().into(list);
            return list;
        } catch (Exception e) {
            throw new MongoDatabaseException("Cannot fetch all", e);
        }
    }

    public CompletableFuture<List<T>> fetchAllAsyncFilter(Bson filter) {
        return CompletableFuture.supplyAsync(() -> {
            List<T> list = new ArrayList<>();
            collection.find(filter).into(list);
            return list;
        });
    }

    @Override
    @Deprecated
    public boolean save(String key, T value) {
        return false;
    }

    public boolean save(T value) {
        Object id = getIdValue(value);

        if (id == null) {
            throw new MongoDatabaseException("MongoId value cannot be null");
        }

        ReplaceOptions options = new ReplaceOptions().upsert(true);
        collection.replaceOne(
                Filters.eq(mongoIdName, id),
                value,
                options
        );
        return true;
    }


    @Override
    public boolean save(String key, T value, Duration timeout) {
        return save(key, value);
    }

    @Override
    public boolean delete(String key) {
        try {
            Object id = convertKey(key);
            return collection.deleteOne(Filters.eq(mongoIdName, id)).getDeletedCount() > 0;
        } catch (Exception e) {
            throw new MongoDatabaseException("Cannot delete document with id " + key, e);
        }
    }

    @Override
    public CompletableFuture<T> fetchAsync(String key) {
        return CompletableFuture.supplyAsync(() -> fetch(key), EXECUTOR);
    }

    public CompletableFuture<T> fetchAsyncFilter(Bson bson) {
        return CompletableFuture.supplyAsync(() -> fetchFilter(bson), EXECUTOR);
    }

    @Override
    public CompletableFuture<List<T>> fetchAllAsync() {
        return CompletableFuture.supplyAsync(this::fetchAll, EXECUTOR);
    }

    public CompletableFuture<List<T>> fetchLast(int limit, String sortField) {
        return CompletableFuture.supplyAsync(() -> {
            List<T> result = new ArrayList<>();

            collection.find()
                    .sort(Sorts.descending(sortField))
                    .limit(limit)
                    .into(result);

            return result;
        });
    }

    public CompletableFuture<List<T>> fetchBefore(
            int limit,
            String sortField,
            long beforeValue
    ) {
        return CompletableFuture.supplyAsync(() -> {
            List<T> result = new ArrayList<>();

            collection.find(Filters.lt(sortField, beforeValue))
                    .sort(Sorts.descending(sortField))
                    .limit(limit)
                    .into(result);

            return result;
        });
    }

    @Override
    @Deprecated
    public CompletableFuture<Boolean> saveAsync(String key, T value) {
        return CompletableFuture.supplyAsync(() -> save(value), EXECUTOR);
    }

    public CompletableFuture<Boolean> saveAsync(T value) {
        return CompletableFuture.supplyAsync(() -> save(value), EXECUTOR);
    }

    @Override
    public CompletableFuture<Boolean> saveAsync(String key, T value, Duration timeout) {
        return CompletableFuture.supplyAsync(() -> save(key, value, timeout), EXECUTOR);
    }

    @Override
    public CompletableFuture<Boolean> deleteAsync(String key) {
        return CompletableFuture.supplyAsync(() -> delete(key), EXECUTOR);
    }

    public CompletableFuture<Long> countAsync(Bson filter) {
        return CompletableFuture.supplyAsync(() ->
                collection.countDocuments(filter)
        );
    }
}

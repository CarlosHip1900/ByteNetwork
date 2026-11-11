package com.carloship.craft.sdk.database.provider;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.UuidRepresentation;

public class SDKMongoClientProvider {

    private static volatile MongoClient client;
    public static MongoClient get(String url) {
        if (client == null) {
            synchronized (SDKMongoClientProvider.class) {
                if (client == null) {
                    MongoClientSettings settings = MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString(url))
                            .uuidRepresentation(UuidRepresentation.STANDARD)
                            .build();

                    client = MongoClients.create(settings);
                }
            }
        }
        return client;
    }

}

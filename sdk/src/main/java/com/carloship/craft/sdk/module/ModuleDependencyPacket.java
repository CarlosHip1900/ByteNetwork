package com.carloship.craft.sdk.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModuleDependencyPacket {

    private RedisClient redis;
    private MongoClient mongo;
    private File directory;

    private ObjectMapper objectMapper;

    private RedisAsyncCommands<String, String> asyncCommands;
    private RedisCommands<String, String> commands;

    private RedisPubSubAsyncCommands<String, String> pubSubAsyncCommands;
    private RedisPubSubCommands<String, String> pubSubCommands;

}

package com.carloship.craft.sdk.module;

import com.carloship.craft.sdk.database.provider.SDKMongoClientProvider;
import com.carloship.craft.sdk.mapper.SDKJsonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.Getter;

import java.io.File;

@Getter
public final class ModuleService {

    private final ModuleHolder holder;
    private final RedisClient redisClient;
    private final ModuleServiceRegistry registry;

    public ModuleService(File folder, String redisUrl, String mongoUrl) {
        this.registry = new ModuleServiceRegistry();
        ObjectMapper mapper = SDKJsonMapper.create();

        RedisClient redisClient = createRedisClient(redisUrl);

        MongoClient mongoClient = SDKMongoClientProvider.get(mongoUrl);

        this.redisClient = redisClient;

        ModuleDependencyPacket dependencyPacket = createDependencyPacket(folder, mapper, redisClient, mongoClient);

        registerDependencies(dependencyPacket);

        this.holder = new ModuleHolder(registry);
    }


    private RedisClient createRedisClient(String redisUrl) {
        ClientResources resources = DefaultClientResources.builder().build();

        return RedisClient.create(resources, redisUrl);
    }

    private ModuleDependencyPacket createDependencyPacket(File folder, ObjectMapper mapper, RedisClient redisClient, MongoClient mongoClient) {
        ModuleDependencyPacket packet = new ModuleDependencyPacket();

        packet.setRedis(redisClient);
        packet.setMongo(mongoClient);
        packet.setDirectory(folder);
        packet.setObjectMapper(mapper);

        StatefulRedisPubSubConnection<String, String> pubSub = redisClient.connectPubSub();

        packet.setPubSubAsyncCommands(pubSub.async());
        packet.setPubSubCommands(pubSub.sync());

        StatefulRedisConnection<String, String> connection = redisClient.connect();

        packet.setAsyncCommands(connection.async());
        packet.setCommands(connection.sync());

        return packet;
    }

    private void registerDependencies(ModuleDependencyPacket packet) {
        registry.register(ModuleDependencyPacket.class, packet);

        //Connections
        registry.register(RedisClient.class, packet.getRedis());
        registry.register(MongoClient.class, packet.getMongo());

        //Data
        registry.register(File.class, packet.getDirectory());

        //Redis
        registry.register(ObjectMapper.class, packet.getObjectMapper());
        registry.register(RedisPubSubAsyncCommands.class, packet.getPubSubAsyncCommands());
        registry.register(RedisPubSubCommands.class, packet.getPubSubCommands());
        registry.register(RedisAsyncCommands.class, packet.getAsyncCommands());
        registry.register(RedisCommands.class, packet.getCommands());
    }

    public void enable() {
        holder.startModules();
    }

    public void disable() {
        holder.stopModules();
    }

}
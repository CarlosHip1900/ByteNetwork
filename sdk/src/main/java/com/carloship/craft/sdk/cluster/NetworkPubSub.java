package com.carloship.craft.sdk.cluster;

import com.carloship.craft.sdk.cluster.payload.PlayerDisconnectPayload;
import com.carloship.craft.sdk.cluster.payload.PlayerTransferPayload;
import com.carloship.craft.sdk.cluster.payload.ServerRegisterPayload;
import com.carloship.craft.sdk.cluster.payload.ServerUnregisterPayload;
import com.carloship.craft.sdk.cluster.topic.PlayerDisconnectTopic;
import com.carloship.craft.sdk.cluster.topic.PlayerTransferTopic;
import com.carloship.craft.sdk.cluster.topic.ServerRegisterTopic;
import com.carloship.craft.sdk.cluster.topic.ServerUnregisterTopic;
import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.pubsub.channel.AsyncPubSub;
import com.carloship.craft.sdk.pubsub.message.OutgoingMessage;
import com.carloship.craft.sdk.pubsub.middleware.MiddlewarePipeline;
import com.carloship.craft.sdk.pubsub.serializer.JsonMessageSerializer;
import com.carloship.craft.sdk.pubsub.serializer.MessageSerializer;
import com.carloship.craft.sdk.pubsub.topic.MessageTopic;
import com.carloship.craft.sdk.pubsub.topic.TopicRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
public class NetworkPubSub {

    private static final String SERVER_REGISTER   = "network:server:register:";
    private static final String SERVER_UNREGISTER = "network:server:unregister:";
    private static final String PLAYER_TRANSFER   = "network:player:transfer:";
    private static final String PLAYER_DISCONNECT = "network:player:disconnect:";

    private final AsyncPubSub pubSub;

    private final ServerRegisterTopic serverRegisterTopic;
    private final ServerUnregisterTopic serverUnregisterTopic;
    private final PlayerTransferTopic playerTransferTopic;
    private final PlayerDisconnectTopic playerDisconnectTopic;

    public NetworkPubSub(
            ModuleDependencyPacket packet,
            Consumer<ServerRegisterPayload> onServerRegister,
            Consumer<ServerUnregisterPayload> onServerUnregister,
            Consumer<PlayerTransferPayload>   onPlayerTransfer,
            Consumer<PlayerDisconnectPayload> onPlayerDisconnect
    ) {
        TopicRegistry registry     = new TopicRegistry();
        MessageSerializer serializer = new JsonMessageSerializer(packet.getObjectMapper());
        MiddlewarePipeline pipeline = new MiddlewarePipeline();

        this.pubSub = new AsyncPubSub(packet.getPubSubAsyncCommands(), registry, serializer, pipeline);

        this.serverRegisterTopic   = new ServerRegisterTopic(SERVER_REGISTER, onServerRegister);
        this.serverUnregisterTopic = new ServerUnregisterTopic(SERVER_UNREGISTER, onServerUnregister);
        this.playerTransferTopic   = new PlayerTransferTopic(PLAYER_TRANSFER, onPlayerTransfer);
        this.playerDisconnectTopic = new PlayerDisconnectTopic(PLAYER_DISCONNECT, onPlayerDisconnect);

        pubSub.registerTopic(serverRegisterTopic);
        pubSub.registerTopic(serverUnregisterTopic);
        pubSub.registerTopic(playerTransferTopic);
        pubSub.registerTopic(playerDisconnectTopic);
    }

    public CompletableFuture<Long> publishServerRegister(ServerRegisterPayload payload) {
        return publish(serverRegisterTopic, payload);
    }

    public CompletableFuture<Long> publishServerUnregister(ServerUnregisterPayload payload) {
        return publish(serverUnregisterTopic, payload);
    }

    public CompletableFuture<Long> publishPlayerTransfer(UUID uuid, String targetServerId) {
        return publish(playerTransferTopic, new PlayerTransferPayload(uuid, targetServerId));
    }

    public CompletableFuture<Long> publishPlayerDisconnect(UUID uuid, String serverId) {
        return publish(playerDisconnectTopic, new PlayerDisconnectPayload(uuid, serverId));
    }


    private <T> CompletableFuture<Long> publish(MessageTopic<T> topic, T payload) {
        try {
            return pubSub.publish(topic, new OutgoingMessage<>(payload));
        } catch (Exception e) {
            log.error("Failed to publish to topic {}", topic.getName(), e);
            return CompletableFuture.failedFuture(e);
        }
    }
}
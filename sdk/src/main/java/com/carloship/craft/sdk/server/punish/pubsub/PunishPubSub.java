package com.carloship.craft.sdk.server.punish.pubsub;

import com.carloship.craft.sdk.pubsub.channel.AsyncPubSub;
import com.carloship.craft.sdk.pubsub.message.IncomingMessage;
import com.carloship.craft.sdk.pubsub.message.OutgoingMessage;
import com.carloship.craft.sdk.pubsub.middleware.MiddlewarePipeline;
import com.carloship.craft.sdk.pubsub.serializer.JsonMessageSerializer;
import com.carloship.craft.sdk.pubsub.topic.MessageTopic;
import com.carloship.craft.sdk.pubsub.topic.TopicRegistry;
import com.carloship.craft.sdk.server.punish.PunishModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

@Slf4j
public class PunishPubSub {

    private final PunishModule punishModule;
    private final AsyncPubSub asyncPubSub;

    private final RefreshTopic refreshTopic = new RefreshTopic();

    private final List<BiConsumer<UUID, RefreshPunishPlayerPacket>> revokeListeners = new CopyOnWriteArrayList<>();

    public PunishPubSub(PunishModule punishModule, ObjectMapper objectMapper, RedisPubSubAsyncCommands<String, String> cmd) {
        this.punishModule = punishModule;
        this.asyncPubSub = new AsyncPubSub(cmd, new TopicRegistry(), new JsonMessageSerializer(objectMapper), new MiddlewarePipeline());

        this.asyncPubSub.registerTopic(refreshTopic);
    }

    public void registerRevokeListener(BiConsumer<UUID, RefreshPunishPlayerPacket> listener) {
        this.revokeListeners.add(listener);
    }

    public CompletableFuture<Long> sendRefreshMessage(RefreshPunishPlayerPacket payload) {
        try {
            return asyncPubSub.publish(refreshTopic, new OutgoingMessage<>(payload));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private class RefreshTopic extends MessageTopic<RefreshPunishPlayerPacket> {

        protected RefreshTopic() {
            super("punish-refresh-topic", RefreshPunishPlayerPacket.class);
        }

        @Override
        public void onMessage(IncomingMessage<RefreshPunishPlayerPacket> message) {
            if (message == null || message.data() == null) return;

            RefreshPunishPlayerPacket data = message.data();
            UUID uuid = data.userUuid();

            if (data.revoke() || data.punished()) {
                punishModule.uncachePlayer(uuid);

                if (data.revoke()) {
                    revokeListeners.forEach(listener -> {
                        try {
                            listener.accept(uuid, data);
                        } catch (Exception e) {
                            log.error("Error executing a revocation listener for player {}", uuid, e);
                        }
                    });
                }

                if (data.punished()) {
                    punishModule.getLastPlayerPunish(uuid).exceptionally(ex -> {
                        log.error("Error refreshing player punishment cache for {}", uuid, ex);
                        return null;
                    });
                }
            }
        }
    }
}
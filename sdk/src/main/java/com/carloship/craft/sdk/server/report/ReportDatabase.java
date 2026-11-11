package com.carloship.craft.sdk.server.report;

import com.carloship.craft.sdk.database.cloud.MongoDatabase;
import com.carloship.craft.sdk.database.cloud.RedisDatabase;
import com.carloship.craft.sdk.server.report.data.ReportEntry;
import com.carloship.craft.sdk.server.report.data.ReportStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.Indexes;
import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ReportDatabase {

    private final RedisDatabase<ReportEntry> redis;
    private final MongoDatabase<ReportEntry> mongo;

    private final AsyncLoadingCache<String, List<ReportEntry>> globalOpenReportsCache;

    public ReportDatabase(RedisClient redisClient, MongoClient mongoClient, ObjectMapper objectMapper) {
        this.redis = new RedisDatabase<>(redisClient, ReportEntry.class, objectMapper);
        this.mongo = new MongoDatabase<>(mongoClient, "byte-network", ReportEntry.class, "reports");

        mongo.indexes(new IndexModel(Indexes.ascending("status")));
        mongo.indexes(new IndexModel(Indexes.ascending("userUuid")));

        this.globalOpenReportsCache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(30))
                .maximumSize(1)
                .buildAsync((key, executor) -> fetchOpenReportsFromBackend());
    }

    public CompletableFuture<Void> createReport(ReportEntry report) {
        String reportKey = "reports:all:" + report.getId().toString();
        String openIndexKey = "reports:index:OPEN";

        return mongo.saveAsync(report).thenCompose(v ->
                redis.saveAsync(reportKey, report, Duration.ofDays(2))
        ).thenAccept(v -> {
            redis.getAsyncCommands().sadd(openIndexKey, report.getId().toString());
        }).exceptionally(ex -> {
            log.error("Critical error while creating report {}", report.getId(), ex);
            return null;
        });
    }

    public CompletableFuture<Void> updateReportStatus(UUID reportId, ReportStatus newStatus, UUID staffUuid, String staffName, String reason) {
        String reportKey = "reports:all:" + reportId.toString();

        return redis.fetchAsync(reportKey).thenCompose(report -> {
            if (report == null) return CompletableFuture.completedFuture(null); // Denúncia expirou do Redis

            ReportStatus oldStatus = report.getStatus();
            report.setStatus(newStatus);
            report.setStatusUserUuid(staffUuid);
            report.setStatusUsername(staffName);
            report.setStatusReason(reason);
            report.setStatusTimestamp(System.currentTimeMillis());

            // Atualiza de volta nos bancos
            return mongo.saveAsync(report)
                    .thenCompose(v -> redis.saveAsync(reportKey, report, Duration.ofDays(2)))
                    .thenRun(() -> {
                        // Remove do índice antigo e gerencia os índices de estado no Redis
                        redis.getAsyncCommands().srem("reports:index:" + oldStatus.name(), reportId.toString());
                        if (newStatus == ReportStatus.OPEN) {
                            redis.getAsyncCommands().sadd("reports:index:OPEN", reportId.toString());
                        }
                    });
        });
    }

    private CompletableFuture<List<ReportEntry>> fetchOpenReportsFromBackend() {
        return redis.getAsyncCommands().smembers("reports:index:OPEN")
                .toCompletableFuture()
                .thenCompose(ids -> {
                    if (ids.isEmpty()) return CompletableFuture.completedFuture(List.of());

                    List<CompletableFuture<ReportEntry>> futures = new ArrayList<>();
                    for (String id : ids) {
                        futures.add(redis.fetchAsync("reports:all:" + id));
                    }

                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenApply(v -> futures.stream()
                                    .map(CompletableFuture::join)
                                    .filter(Objects::nonNull)
                                    .sorted(Comparator.comparingLong(ReportEntry::getTimestamp)) // Mais antigas primeiro (prioridade)
                                    .toList()
                            );
                });
    }

    public CompletableFuture<List<ReportEntry>> getOpenReports() {
        return globalOpenReportsCache.get("GLOBAL");
    }

    public CompletableFuture<List<ReportEntry>> getReportUserMongo(UUID userUuid) {
        var filter = Filters.eq("userUuid", userUuid);

        return mongo.fetchAllAsyncFilter(filter)
                .thenApply(list -> {
                    if (list == null) return List.<ReportEntry>of();

                    return list.stream()
                            .sorted(Comparator.comparingLong(ReportEntry::getTimestamp).reversed())
                            .toList();
                })
                .exceptionally(ex -> {
                    log.error("Error retrieving report history for user {} from MongoDB", userUuid, ex);
                    return List.of();
                });
    }

    public CompletableFuture<ReportEntry> fetchReportPlayer(UUID uuid, UUID id) {
        return getReportUserMongo(uuid).thenApply(list -> list.stream().filter(report -> report.getId().equals(id)).findFirst().orElse(null));
    }

}

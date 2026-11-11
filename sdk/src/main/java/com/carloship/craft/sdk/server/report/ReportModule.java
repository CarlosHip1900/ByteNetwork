package com.carloship.craft.sdk.server.report;

import com.carloship.craft.sdk.annotations.SDKModuleInfo;
import com.carloship.craft.sdk.module.ModulePriority;
import com.carloship.craft.sdk.module.SDKModule;
import com.carloship.craft.sdk.server.report.data.ReportEntry;
import com.carloship.craft.sdk.server.report.data.ReportStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import io.lettuce.core.RedisClient;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@SDKModuleInfo(
        id = "report",
        name = "Report",
        priority = ModulePriority.VERY_LATE,
        createDefault = false
)
public class ReportModule implements SDKModule {

    private final ReportDatabase reportDatabase;

    public ReportModule(RedisClient redisClient, MongoClient mongoClient, ObjectMapper objectMapper){
        this.reportDatabase = new ReportDatabase(redisClient, mongoClient, objectMapper);
    }

    @Override
    public void createModule() {
    }

    @Override
    public void disableModule() {
    }

    public CompletableFuture<Void> createReport(ReportEntry report) {
        return reportDatabase.createReport(report);
    }

    public CompletableFuture<Void> updateReportStatus(UUID reportId, ReportStatus newStatus, UUID staffUuid, String staffName, String reason) {
        return reportDatabase.updateReportStatus(reportId, newStatus, staffUuid, staffName, reason);
    }

    public CompletableFuture<List<ReportEntry>> getOpenReports() {
        return reportDatabase.getOpenReports();
    }

    public CompletableFuture<List<ReportEntry>> getReportUserMongo(UUID userUuid) {
        return reportDatabase.getReportUserMongo(userUuid);
    }

    public CompletableFuture<ReportEntry> fetchReportPlayer(UUID userUuid, UUID reportId){
        return reportDatabase.fetchReportPlayer(userUuid, reportId);
    }
}

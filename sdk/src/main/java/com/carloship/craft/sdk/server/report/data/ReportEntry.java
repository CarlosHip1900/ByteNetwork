package com.carloship.craft.sdk.server.report.data;

import com.carloship.craft.sdk.annotations.database.MongoId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportEntry {

    @MongoId
    @BsonId
    private UUID id;

    private UUID userUuid;
    private String username;

    private UUID reporterUuid;
    private String reporterUsername;

    private long timestamp;
    private String reason;
    private String serverId;
    private ReportStatus status;

    private List<String> evidences; // Links de chat logs, logs de inventário ou clips (se houver)
    private int duplicateCount = 1;  // Se 10 pessoas denunciarem o mesmo hacker pelo mesmo motivo, incrementamos aqui em vez de poluir a database com 10 entradas separadas.

    private String statusReason;
    private long statusTimestamp;
    private UUID statusUserUuid;
    private String statusUsername;

    @JsonIgnore
    public boolean hasViewer() {
        return statusUserUuid != null && statusUsername != null;
    }

}
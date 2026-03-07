package com.aihoneypot.dashboard.dto;

import com.aihoneypot.core.model.ClientType;
import com.aihoneypot.core.model.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object for threat session information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreatSessionDTO {
    private Long id;
    private String sessionId;
    private String ipAddress;
    private Instant timestamp;
    private ClientType clientType;
    private Double confidence;
    private Severity severity;
    private Boolean isThreat;
    private String userAgent;
    private String firstUri;
    private Integer requestCount;
    private String explanation;
    private Boolean canaryTrapTriggered;
}


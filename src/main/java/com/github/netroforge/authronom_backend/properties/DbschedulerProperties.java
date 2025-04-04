package com.github.netroforge.authronom_backend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "db-scheduler")
public class DbschedulerProperties {
    private Duration pollingInterval;
    private Duration shutdownMaxWait;
    private Duration heartbeatInterval;

    private Integer missedHeartbeatsLimit;

    private Duration checkOldLogRecordsInterval;
    private Duration retentionOfLogRecords;
}


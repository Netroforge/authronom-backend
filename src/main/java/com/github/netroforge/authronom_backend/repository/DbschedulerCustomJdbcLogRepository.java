package com.github.netroforge.authronom_backend.repository;

import com.github.kagkarlsson.jdbc.JdbcRunner;
import com.github.kagkarlsson.jdbc.SQLRuntimeException;
import com.github.kagkarlsson.scheduler.jdbc.AutodetectJdbcCustomization;
import com.github.kagkarlsson.scheduler.jdbc.JdbcCustomization;
import com.github.kagkarlsson.scheduler.serializer.JavaSerializer;
import com.github.kagkarlsson.scheduler.serializer.Serializer;
import com.zaxxer.hikari.HikariDataSource;
import io.rocketbase.extension.ExecutionLog;
import io.rocketbase.extension.LogRepository;
import io.rocketbase.extension.jdbc.IdProvider;
import io.rocketbase.extension.jdbc.Snowflake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.NotSerializableException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.time.Duration;
import java.time.Instant;


// Inspired by JdbcLogRepository
@Slf4j
@Repository
public class DbschedulerCustomJdbcLogRepository implements LogRepository {
    public static final String DEFAULT_TABLE_NAME = "scheduled_execution_logs";

    private final JdbcRunner jdbcRunner;
    private final Serializer serializer;
    private final String tableName;
    private final JdbcCustomization jdbcCustomization;
    private final IdProvider idProvider;

    public DbschedulerCustomJdbcLogRepository(
            HikariDataSource primaryDataSource
    ) {
        this.tableName = DEFAULT_TABLE_NAME;
        this.jdbcRunner = new JdbcRunner(primaryDataSource, true);
        this.serializer = new JavaSerializer();
        this.jdbcCustomization = new AutodetectJdbcCustomization(primaryDataSource);
        this.idProvider = new Snowflake();
    }

    @Override
    public boolean createIfNotExists(ExecutionLog executionLog) {
        try {
            jdbcRunner.execute(
                    "insert into " + tableName + "(id, task_name, task_instance, task_data, picked_by, time_started, time_finished, succeeded, duration_ms, exception_class, exception_message, exception_stacktrace) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    (PreparedStatement p) -> {
                        p.setLong(1, idProvider.nextId());
                        p.setString(2, executionLog.taskInstance.getTaskName());
                        p.setString(3, executionLog.taskInstance.getId());
                        p.setObject(4, serialize(executionLog.taskInstance.getData()));
                        p.setString(5, executionLog.pickedBy);
                        jdbcCustomization.setInstant(p, 6, executionLog.timeStarted);
                        jdbcCustomization.setInstant(p, 7, executionLog.timeFinished);
                        p.setBoolean(8, executionLog.succeeded);
                        p.setLong(9, Duration.between(executionLog.timeStarted, executionLog.timeFinished).toMillis());
                        p.setString(10, executionLog.cause != null ? executionLog.cause.getClass().getName() : null);
                        p.setString(11, executionLog.cause != null ? executionLog.cause.getMessage() : null);
                        p.setString(12, getStacktrace(executionLog.cause));
                    });
            return true;
        } catch (SQLRuntimeException e) {
            log.error("Exception when inserting execution-log", e);
            return false;
        }
    }

    public void deleteOldRecords(Duration retentionOfLogRecords) {
        try {
            log.info("Deleting old records from '{}' ...", tableName);
            jdbcRunner.execute(
                    "delete from " + tableName + " where time_finished < ?",
                    (PreparedStatement p) -> {
                        jdbcCustomization.setInstant(p, 1, Instant.now().minus(retentionOfLogRecords));
                    });
            log.info("Deleting old records from '{}' completed.", tableName);
        } catch (SQLRuntimeException e) {
            log.error("Exception when deleting old records in execution-log", e);
        }
    }

    protected String getStacktrace(Throwable cause) {
        if (cause == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        cause.printStackTrace(out);
        return writer.toString();
    }

    protected byte[] serialize(Object value) {
        if (serializer == null || value == null) {
            return null;
        }
        try {
            return serializer.serialize(value);
        } catch (Exception e) {
            if (e instanceof NotSerializableException) {
                log.warn("object is not serializable - you need to add Serializable");
            } else {
                log.error("serialization failed for {} -> {}", value.getClass(), e.getMessage());
            }
            return null;
        }
    }
}

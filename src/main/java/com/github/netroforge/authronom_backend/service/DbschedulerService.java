package com.github.netroforge.authronom_backend.service;

import com.github.kagkarlsson.scheduler.Scheduler;
import com.github.kagkarlsson.scheduler.task.SchedulableInstance;
import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.netroforge.authronom_backend.properties.DbschedulerProperties;
import com.github.netroforge.authronom_backend.repository.DbschedulerCustomJdbcLogRepository;
import com.zaxxer.hikari.HikariDataSource;
import io.rocketbase.extension.stats.LogStatsPlainRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DbschedulerService implements InitializingBean, DisposableBean {
    private final HikariDataSource primaryDataSource;
    private final DbschedulerProperties dbschedulerProperties;
    private final DbschedulerCustomJdbcLogRepository dbSchedulerCustomJdbcLogRepository;
    private final List<OneTimeTask<?>> knownOneTimeTasks;
    private final List<RecurringTask<?>> knownRecurringTaskTasks;

    private Scheduler scheduler;

    public DbschedulerService(
            HikariDataSource primaryDataSource,
            DbschedulerProperties dbschedulerProperties,
            DbschedulerCustomJdbcLogRepository dbSchedulerCustomJdbcLogRepository,
            List<OneTimeTask<?>> knownOneTimeTasks,
            List<RecurringTask<?>> knownRecurringTaskTasks
    ) {
        this.primaryDataSource = primaryDataSource;
        this.dbschedulerProperties = dbschedulerProperties;
        this.dbSchedulerCustomJdbcLogRepository = dbSchedulerCustomJdbcLogRepository;
        this.knownOneTimeTasks = knownOneTimeTasks;
        this.knownRecurringTaskTasks = knownRecurringTaskTasks;
    }

    @Override
    public void afterPropertiesSet() {
        scheduler = Scheduler
                .create(
                        primaryDataSource,
                        knownOneTimeTasks
                                .stream()
                                .map((Function<OneTimeTask<?>, Task<?>>) oneTimeTask -> oneTimeTask)
                                .collect(Collectors.toList())
                )
                .pollingInterval(dbschedulerProperties.getPollingInterval())
                .shutdownMaxWait(dbschedulerProperties.getShutdownMaxWait())
                .heartbeatInterval(dbschedulerProperties.getHeartbeatInterval())
                .missedHeartbeatsLimit(dbschedulerProperties.getMissedHeartbeatsLimit())
                .enablePriority()
                .enableImmediateExecution()
                .statsRegistry(new LogStatsPlainRegistry(dbSchedulerCustomJdbcLogRepository))
                .startTasks(knownRecurringTaskTasks)
                .build();

        scheduler.start();
    }

    @Override
    public void destroy() {
        log.info("Shutting down db-scheduler...");
        scheduler.stop();
        log.info("Shutting down of db-scheduler completed.");
    }

    public void scheduleIfNotExists(SchedulableInstance<?> schedulableInstance) {
        scheduler.scheduleIfNotExists(schedulableInstance);
    }
}

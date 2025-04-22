package com.github.netroforge.authronom_backend.service.task;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import com.github.netroforge.authronom_backend.properties.DbschedulerProperties;
import com.github.netroforge.authronom_backend.db.repository.DbschedulerCustomJdbcLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DbschedulerDeleteOldLogRecordsTask extends RecurringTask<Void> {
    private static final TaskDescriptor<Void> TASK_DESCRIPTOR =
            TaskDescriptor.of(DbschedulerDeleteOldLogRecordsTask.class.getSimpleName(), Void.class);

    private final DbschedulerProperties dbschedulerProperties;
    private final DbschedulerCustomJdbcLogRepository dbschedulerCustomJdbcLogRepository;

    public DbschedulerDeleteOldLogRecordsTask(
            DbschedulerProperties dbschedulerProperties,
            DbschedulerCustomJdbcLogRepository dbschedulerCustomJdbcLogRepository
    ) {
        super(
                TASK_DESCRIPTOR.getTaskName(),
                FixedDelay.of(dbschedulerProperties.getCheckOldLogRecordsInterval()),
                TASK_DESCRIPTOR.getDataClass()
        );
        this.dbschedulerProperties = dbschedulerProperties;
        this.dbschedulerCustomJdbcLogRepository = dbschedulerCustomJdbcLogRepository;
    }

    public static TaskDescriptor<Void> getTaskDescriptor() {
        return TASK_DESCRIPTOR;
    }

    @Override
    public void executeRecurringly(
            TaskInstance<Void> taskInstance,
            ExecutionContext executionContext
    ) {
        dbschedulerCustomJdbcLogRepository.deleteOldRecords(
                dbschedulerProperties.getRetentionOfLogRecords()
        );
    }
}

package com.github.netroforge.authronom_backend.service.task;

import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.RecurringTask;
import com.github.kagkarlsson.scheduler.task.schedule.FixedDelay;
import com.github.netroforge.authronom_backend.properties.UserRegistrationProperties;
import com.github.netroforge.authronom_backend.db.repository.primary.UserEmailVerificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
public class UserEmailDanglingVerificationsCleanupTask extends RecurringTask<Void> {
    private static final TaskDescriptor<Void> TASK_DESCRIPTOR =
            TaskDescriptor.of(UserEmailDanglingVerificationsCleanupTask.class.getSimpleName(), Void.class);

    private final UserRegistrationProperties userRegistrationProperties;
    private final UserEmailVerificationRepository userEmailVerificationRepository;

    public UserEmailDanglingVerificationsCleanupTask(
            UserRegistrationProperties userRegistrationProperties,
            UserEmailVerificationRepository userEmailVerificationRepository
    ) {
        super(
                TASK_DESCRIPTOR.getTaskName(),
                FixedDelay.of(userRegistrationProperties.getDanglingConfirmationCodeDeletionInterval()),
                TASK_DESCRIPTOR.getDataClass()
        );
        this.userRegistrationProperties = userRegistrationProperties;
        this.userEmailVerificationRepository = userEmailVerificationRepository;
    }

    public static TaskDescriptor<Void> getTaskDescriptor() {
        return TASK_DESCRIPTOR;
    }

    @Override
    public void executeRecurringly(
            TaskInstance<Void> taskInstance,
            ExecutionContext executionContext
    ) {
        try {
            userEmailVerificationRepository.deleteAllOldRecords(
                    LocalDateTime.now(ZoneOffset.UTC).minus(
                            userRegistrationProperties
                                    .getDanglingConfirmationCodeRetentionThreshold()
                    )
            );
        } catch (Exception e) {
            log.error("Error", e);
            throw new IllegalStateException(e);
        }
    }
}

package com.github.netroforge.authronom_backend.service.task;

import com.fasterxml.uuid.Generators;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.helper.OneTimeTask;
import com.github.netroforge.authronom_backend.properties.UserRegistrationProperties;
import com.github.netroforge.authronom_backend.repository.UserEmailVerificationRepository;
import com.github.netroforge.authronom_backend.repository.entity.UserEmailVerification;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
public class UserEmailConfirmationSendTask extends OneTimeTask<UserEmailConfirmationSendTaskData> {
    private static final TaskDescriptor<UserEmailConfirmationSendTaskData> TASK_DESCRIPTOR =
            TaskDescriptor.of(UserEmailConfirmationSendTask.class.getSimpleName(), UserEmailConfirmationSendTaskData.class);

    private final JavaMailSender javaMailSender;
    private final UserRegistrationProperties userRegistrationProperties;
    private final UserEmailVerificationRepository userEmailVerificationRepository;
    private final SpringTemplateEngine springTemplateEngine;

    public UserEmailConfirmationSendTask(
            JavaMailSender javaMailSender,
            UserRegistrationProperties userRegistrationProperties,
            UserEmailVerificationRepository userEmailVerificationRepository,
            SpringTemplateEngine springTemplateEngine
    ) {
        super(TASK_DESCRIPTOR.getTaskName(), TASK_DESCRIPTOR.getDataClass());
        this.javaMailSender = javaMailSender;
        this.userRegistrationProperties = userRegistrationProperties;
        this.userEmailVerificationRepository = userEmailVerificationRepository;
        this.springTemplateEngine = springTemplateEngine;
    }

    public static TaskDescriptor<UserEmailConfirmationSendTaskData> getTaskDescriptor() {
        return TASK_DESCRIPTOR;
    }

    @Override
    public void executeOnce(
            TaskInstance<UserEmailConfirmationSendTaskData> taskInstance,
            ExecutionContext executionContext
    ) {
        try {
            // Save email confirmation code to db
            UserEmailVerification userEmailVerification = new UserEmailVerification(
                    Generators.timeBasedEpochGenerator().generate().toString(),
                    taskInstance.getData().getEmail(),
                    taskInstance.getData().getConfirmationCode(),
                    LocalDateTime.now(ZoneOffset.UTC),
                    LocalDateTime.now(ZoneOffset.UTC),
                    true
            );
            userEmailVerificationRepository.save(userEmailVerification);

            // Form email content in html
            Context context = new Context();
            context.setVariable("confirmationCode", taskInstance.getData().getConfirmationCode());
            String htmlContent = springTemplateEngine.process("email-confirmation", context);

            // Form email
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setContent(htmlContent, "text/html; charset=utf-8");

            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setFrom(userRegistrationProperties.getConfirmationEmailFrom(), userRegistrationProperties.getConfirmationEmailFromName());
            helper.setSubject(userRegistrationProperties.getConfirmationEmailSubject());
            helper.setTo(taskInstance.getData().getEmail());

            // Send email
            javaMailSender.send(message);
        } catch (MailException mailException) {
            log.error("Error sending email confirmation code: {}", mailException.toString());
            throw new IllegalStateException("Error sending email confirmation code: " + mailException);
        } catch (Exception e) {
            log.error("Error", e);
            throw new IllegalStateException(e);
        }
    }
}

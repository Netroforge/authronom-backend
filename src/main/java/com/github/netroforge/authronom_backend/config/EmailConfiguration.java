package com.github.netroforge.authronom_backend.config;

import com.github.netroforge.authronom_backend.properties.EmailProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfiguration {

    /**
     * Inspired by https://www.baeldung.com/spring-email
     */
    @Bean
    public JavaMailSender javaMailSender(
            EmailProperties emailProperties
    ) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailProperties.getServer());
        mailSender.setPort(emailProperties.getPort());

        mailSender.setUsername(emailProperties.getUsername());
        mailSender.setPassword(emailProperties.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", emailProperties.isSsl());
        //props.put("mail.debug", "true");

        return mailSender;
    }

}

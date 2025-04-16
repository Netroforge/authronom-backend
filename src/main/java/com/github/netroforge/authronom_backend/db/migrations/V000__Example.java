package com.github.netroforge.authronom_backend.db.migrations;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

@Slf4j
@Component()
public class V000__Example extends PrimaryBaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        log.info("Flyway: complete dummy V000__Example migration");
    }
}

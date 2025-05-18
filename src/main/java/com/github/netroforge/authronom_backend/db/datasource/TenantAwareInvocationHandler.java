package com.github.netroforge.authronom_backend.db.datasource;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;

@Slf4j
public class TenantAwareInvocationHandler implements InvocationHandler {
    private final Connection target;

    public TenantAwareInvocationHandler(Connection target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "getTargetConnection":
                return target;
            case "close":
                // Clear tenant ID before closing connection
                try (Statement statement = target.createStatement()) {
                    statement.execute("RESET app.current_tenant");
                } catch (Exception e) {
                    log.error("Error resetting tenant_id", e);
                }
                return method.invoke(target, args);
            default:
                return method.invoke(target, args);
        }
    }
}

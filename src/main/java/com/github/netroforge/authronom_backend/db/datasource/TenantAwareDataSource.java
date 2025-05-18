package com.github.netroforge.authronom_backend.db.datasource;

import com.github.netroforge.authronom_backend.utils.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class TenantAwareDataSource extends DelegatingDataSource {

    public TenantAwareDataSource(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        final Connection connection = getTargetDataSource().getConnection();
        setTenantId(connection);
        return getTenantAwareConnectionProxy(connection);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        final Connection connection = getTargetDataSource().getConnection(username, password);
        setTenantId(connection);
        return getTenantAwareConnectionProxy(connection);
    }

    private void setTenantId(Connection connection) {
        String tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null && !tenantId.isEmpty()) {
            try (Statement statement = connection.createStatement()) {
                log.debug("Setting tenant_id to: {}", tenantId);
                statement.execute("SET app.current_tenant = '" + tenantId + "'");
            } catch (Exception e) {
                log.error("Error setting tenant_id", e);
            }
        }
    }

    private Connection getTenantAwareConnectionProxy(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                ConnectionProxy.class.getClassLoader(),
                new Class[]{ConnectionProxy.class},
                new TenantAwareInvocationHandler(connection));
    }
}

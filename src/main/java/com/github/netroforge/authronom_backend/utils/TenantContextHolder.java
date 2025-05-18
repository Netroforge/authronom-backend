package com.github.netroforge.authronom_backend.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TenantContextHolder {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();
    
    public static void setTenantId(String tenantId) {
        log.debug("Setting tenant ID: {}", tenantId);
        CONTEXT.set(tenantId);
    }
    
    public static String getTenantId() {
        return CONTEXT.get();
    }
    
    public static void clear() {
        CONTEXT.remove();
    }
}

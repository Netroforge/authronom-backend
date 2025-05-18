package com.github.netroforge.authronom_backend.service;

import com.fasterxml.uuid.Generators;
import com.github.netroforge.authronom_backend.db.repository.TenantRepository;
import com.github.netroforge.authronom_backend.db.repository.entity.Tenant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    public Tenant createTenant(String name) {
        Tenant tenant = new Tenant();
        tenant.setUid(Generators.timeBasedEpochGenerator().generate().toString());
        tenant.setName(name);
        tenant.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        tenant.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC));
        tenant.setNew(true);
        return tenantRepository.save(tenant);
    }

    public Tenant getTenantById(String id) {
        return tenantRepository.findByUid(id);
    }

    public Tenant getTenantByName(String name) {
        return tenantRepository.findByName(name);
    }

    public List<Tenant> getAllTenants() {
        return StreamSupport.stream(tenantRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public void deleteTenant(String id) {
        tenantRepository.deleteById(id);
    }
}

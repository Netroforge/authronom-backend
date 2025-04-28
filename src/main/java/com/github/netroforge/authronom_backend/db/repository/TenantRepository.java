package com.github.netroforge.authronom_backend.db.repository;

import com.github.netroforge.authronom_backend.db.repository.entity.Tenant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends CrudRepository<Tenant, String> {
    Tenant findByUid(String uid);
    Tenant findByName(String name);
}

package com.github.netroforge.authronom_backend.db.repository;


import com.github.netroforge.authronom_backend.db.repository.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
    User findByEmail(String email);

    User findByUid(String uid);

    User findByGoogleId(String googleId);
}


package com.coremvc.repository;

import com.coremvc.model.Role;
import com.coremvc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUuid(String uuid);
    boolean existsByEmail(String email);
    Optional<User> findByRole(Role role);
}
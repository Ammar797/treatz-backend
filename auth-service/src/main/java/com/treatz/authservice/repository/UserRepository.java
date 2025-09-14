package com.treatz.authservice.repository;

import com.treatz.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA is smart enough to create the query for us just from the method name!
    Optional<User> findByEmail(String email);
}
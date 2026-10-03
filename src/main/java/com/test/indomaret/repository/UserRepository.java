package com.test.indomaret.repository;

import com.test.indomaret.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameAndIsActiveTrueAndIsDeletedFalse(String username);

    Optional<User> findByEmailAndIsActiveTrueAndIsDeletedFalse(String email);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

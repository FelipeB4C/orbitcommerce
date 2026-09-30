package com.orbitcommerce.identity.repository;

import com.orbitcommerce.identity.enums.UserStatus;
import com.orbitcommerce.identity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    User findByEmail(String email);

    Optional<User> findByEmailIgnoreCaseAndStatusNotIn(String email, Collection<UserStatus> statuses);
}

package com.fedeherrera.spring_secure_api_starter.repository;

import com.fedeherrera.spring_secure_api_starter.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}

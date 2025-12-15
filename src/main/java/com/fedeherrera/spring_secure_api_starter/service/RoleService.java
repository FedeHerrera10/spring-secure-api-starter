package com.fedeherrera.spring_secure_api_starter.service;

import com.fedeherrera.spring_secure_api_starter.entity.Role;

import java.util.Optional;

public interface RoleService {

    Optional<Role> findByName(String name);
}

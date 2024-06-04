package com.snelson.cadenceAPI.repository;

import com.snelson.cadenceAPI.model.ERole;
import com.snelson.cadenceAPI.model.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String>{
    Optional<Role> findByName(ERole name);
}

package com.snelson.cadenceAPI.repository;

import com.snelson.cadenceAPI.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<User, String>{
    @Query("{ 'username' : ?0 }")
    User findByUsername(String username);
}

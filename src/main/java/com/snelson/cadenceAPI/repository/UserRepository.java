package com.snelson.cadenceAPI.repository;

import com.snelson.cadenceAPI.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<User, String>{

    public User findByEmail(String email);
    public User findByUsername(String username);
    public User findByUsernameAndPassword(String username, String password);
}

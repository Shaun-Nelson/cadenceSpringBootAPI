package com.snelson.cadenceAPI.repository;

import com.snelson.cadenceAPI.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface UserRepository extends MongoRepository<User, String>{

    @Query("{ 'email' : ?0 }")
    User findByEmail(String email);

    @Query("{ 'username' : ?0 }")
    User findByUsername(String username);

    @Query("{ 'accessToken' : ?0 }")
    User findByAccessToken(String accessToken);

    @Query("{ 'refreshToken' : ?0 }")
    User findByRefreshToken(String refreshToken);

    @Query("{ 'username' : ?0, 'password' : ?1 }")
    User findByUsernameAndPassword(String username, String password);
}

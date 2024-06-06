package com.snelson.cadenceAPI.repository;

import com.snelson.cadenceAPI.model.RefreshToken;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, ObjectId> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}

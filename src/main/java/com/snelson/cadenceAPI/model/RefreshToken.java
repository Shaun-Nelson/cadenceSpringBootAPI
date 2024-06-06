package com.snelson.cadenceAPI.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.Instant;

@Document
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {

    @MongoId
    private ObjectId id;

    private String refreshToken;
    private Instant expiryTime;

    @DBRef
    private User user;
}

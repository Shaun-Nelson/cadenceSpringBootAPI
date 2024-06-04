package com.snelson.cadenceAPI.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role {

    @MongoId
    ObjectId id;

    ERole name;

    public Role(String authority) {
        this.name = ERole.valueOf(authority);
    }
}

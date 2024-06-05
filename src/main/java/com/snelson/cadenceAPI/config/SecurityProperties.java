package com.snelson.cadenceAPI.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "security.jwt")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityProperties {

    private String key;
}

package com.snelson.cadenceAPI;

import jakarta.annotation.PostConstruct;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.concurrent.Executor;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class})
@EnableMongoRepositories
@EnableMongoAuditing
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:5173", "cadence.technology"}, allowCredentials = "true")
@EnableConfigurationProperties(SecurityProperties.class)
@EnableAsync
@Log
public class CadenceApiApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(CadenceApiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Application started");
	}
}

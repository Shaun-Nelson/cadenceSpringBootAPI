package com.snelson.cadenceAPI;

import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.UserRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@SpringBootApplication
@EnableMongoRepositories
@EnableMongoAuditing
@Log
public class CadenceApiApplication implements CommandLineRunner {

	@Autowired
	UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(CadenceApiApplication.class, args);
	}

	void createUsers() {
		System.out.println("Creating users...");
		userRepository.save(new User("1", "user1", "password1", "user@test.com", null));
		System.out.println("...Users created.");
	}

	@Override
	public void run(final String... args) {
		userRepository.deleteAll();
		createUsers();
		log.info("User repository: " + userRepository);
	}
}

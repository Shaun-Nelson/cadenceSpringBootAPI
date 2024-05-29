package com.snelson.cadenceAPI;

import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.PlaylistRepository;
import com.snelson.cadenceAPI.repository.SongRepository;
import com.snelson.cadenceAPI.repository.UserRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;

import javax.sql.DataSource;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, DataSourceAutoConfiguration.class})
@EnableMongoRepositories
@EnableMongoAuditing
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
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

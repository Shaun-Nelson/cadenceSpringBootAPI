package com.snelson.cadenceAPI;

import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.PlaylistRepository;
import com.snelson.cadenceAPI.repository.SongRepository;
import com.snelson.cadenceAPI.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CadenceApiApplicationTests {

	@Autowired
	UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void findUserByEmail() {
		User user = userRepository.findByEmail("user1@test.com");
		assert (user != null);
	}

	@Test
	void findUserByUsername() {
		User user = userRepository.findByUsername("user1");
		assert (user != null);
	}

	@Test
	void findUserByUsernameAndPassword() {
		User user = userRepository.findByUsernameAndPassword("user1", "password1");
		assert (user != null);
	}
}

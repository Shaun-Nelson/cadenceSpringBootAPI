package com.snelson.cadenceAPI.controller;

import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public User getUserById(@PathVariable("id") String id) {
        return userRepository.findById(id).orElse(null);
    }

    @PostMapping
    public ResponseEntity<String> createUser(@Valid @RequestBody User user) {
        try {
            userRepository.save(user);

            return ResponseEntity.ok("User created");
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            return ResponseEntity.badRequest().body("User creation failed");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable("id") String id, @Valid @RequestBody User user) {
        try {
            user.setId(id);
            userRepository.save(user);
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            return ResponseEntity.badRequest().body("User update failed");
        }

        return ResponseEntity.ok("User updated");
    }

        @DeleteMapping("/{id}")
        public void deleteUser (@PathVariable("id") String id) {
            userRepository.deleteById(id);
        }
    }


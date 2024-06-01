package com.snelson.cadenceAPI.controller;

import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.UserRepository;
import com.snelson.cadenceAPI.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        try {
            if (userRepository.findByUsername(user.getUsername()) != null) {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

            User newUser = userService.createUser(user);

            if (newUser == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(newUser, HttpStatus.CREATED);
            }
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/login", produces = "application/json")
    public ResponseEntity<User> loginUser(@Valid @RequestBody User user) {
        try {
            userService.login(user);

            return new ResponseEntity<User>(user, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println("Error logging in user: " + e.getMessage());
            return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable("id") String id, @Valid @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            if (updatedUser == null) {
                return ResponseEntity.badRequest().body("User update failed");
            } else {
                return ResponseEntity.ok(updatedUser.toString());
            }
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            return ResponseEntity.badRequest().body("User update failed");
        }
    }

    @DeleteMapping("/{id}")
    public boolean deleteUser (@PathVariable("id") String id) {
        return userService.deleteUser(id);
    }
}


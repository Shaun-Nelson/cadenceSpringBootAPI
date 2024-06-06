package com.snelson.cadenceAPI.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.snelson.cadenceAPI.dto.LoginRequest;
import com.snelson.cadenceAPI.dto.LoginResponse;
import com.snelson.cadenceAPI.model.RefreshToken;
import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.UserRepository;
import com.snelson.cadenceAPI.service.MongoAuthUserDetailService;
import com.snelson.cadenceAPI.service.TokenService;
import com.snelson.cadenceAPI.service.UserService;
import com.snelson.cadenceAPI.utils.CustomGsonExclusionStrategy;
import com.snelson.cadenceAPI.utils.InstantTypeAdapter;
import com.snelson.cadenceAPI.utils.SecureRandomTypeAdapter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private MongoAuthUserDetailService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserService userService;

    private final Gson gson = new GsonBuilder()
            .setExclusionStrategies(new CustomGsonExclusionStrategy())
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
            .registerTypeAdapter(SecureRandom.class, new SecureRandomTypeAdapter())
            .create();

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);

        if (authenticationResponse.isAuthenticated()) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            userService.login(userService.getUserByUsername(loginRequest.getUsername()));
            String accessToken = tokenService.generateAccessToken(authenticationResponse);
            RefreshToken refreshToken = tokenService.generateRefreshToken(userDetails.getUsername());
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getRefreshToken())
                    .username(userDetails.getUsername())
                    .build();

            return ResponseEntity.ok(gson.toJson(response));
        } else {
            return ResponseEntity.badRequest().body("Login failed. Invalid request.");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(gson.toJson("User logged out"));
    }

    @PostMapping("/signup")
    public ResponseEntity<String> createUser(@Valid @RequestBody User user) {
        try {
            User newUser = userService.createUser(user);
            if (newUser == null) {
                return ResponseEntity.badRequest().body("User signup failed");
            } else {
                return ResponseEntity.status(HttpStatus.CREATED).body("User signed up: " + newUser.getUsername());
            }
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error creating user");
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

    private void doAuthenticate(String username, String password) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password);
        try {
            authenticationManager.authenticate(authentication);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid Username or Password!");
        }
    }
}


package com.snelson.cadenceAPI.controller;

import com.google.gson.Gson;
import com.snelson.cadenceAPI.model.LoginRequest;
import com.snelson.cadenceAPI.model.LoginResponse;
import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.UserRepository;
import com.snelson.cadenceAPI.service.MongoAuthUserDetailService;
import com.snelson.cadenceAPI.service.TokenService;
import com.snelson.cadenceAPI.service.UserService;
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

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.getUsername(), loginRequest.getPassword());
        Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        String token = tokenService.generateToken(authenticationResponse);
        LoginResponse response = new LoginResponse(token, userDetails.getUsername());
        userService.login(userService.getUserByUsername(loginRequest.getUsername()));

        return ResponseEntity.ok(new Gson().toJson(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new Gson().toJson("User logged out"));
    }

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


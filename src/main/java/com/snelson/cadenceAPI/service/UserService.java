package com.snelson.cadenceAPI.service;

import com.snelson.cadenceAPI.model.ERole;
import com.snelson.cadenceAPI.model.Role;
import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.RoleRepository;
import com.snelson.cadenceAPI.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private MongoAuthUserDetailService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    public List<User> getUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            System.out.println("Error getting users: " + e.getMessage());
            return null;
        }
    }

    public User getUserByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } catch (Exception e) {
            System.out.println("Error getting user: " + e.getMessage());
            return null;
        }
    }

    public User getUserById(String id) {
        try {
            return userRepository.findById(id).orElse(null);
        } catch (Exception e) {
            System.out.println("Error getting user: " + e.getMessage());
            return null;
        }
    }

    public User createUser(User user) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("User already exists");
        }
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public User updateUser(String id, User user) {
        try {
            User existingUser = userRepository.findById(id).orElse(null);
            if (existingUser == null) {
                return null;
            }
            existingUser.setUsername(user.getUsername());
            existingUser.setPassword(user.getPassword());

            return userRepository.save(existingUser);
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteUser(String id) {
        try {
            userRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    public void login(User user) {
        try {
            user.setEnabled(true);
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            user.setRoles(roles);

            userRepository.save(user);
        } catch (Exception e) {
            System.out.println("Error logging in user: " + e.getMessage());
        }
    }

    public User logout(Authentication authentication) {
        try {
            if (authentication != null) {
                User user = userRepository.findByUsername(authentication.getName());
                user.setEnabled(false);
                userRepository.save(user);
                authentication.setAuthenticated(false);

                return user;
            }
        } catch (Exception e) {
            System.out.println("Error logging out user: " + e.getMessage());
        }
        return null;
    }
}

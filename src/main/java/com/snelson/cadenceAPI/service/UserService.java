package com.snelson.cadenceAPI.service;

import com.snelson.cadenceAPI.model.ERole;
import com.snelson.cadenceAPI.model.Role;
import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public List<User> getUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            System.out.println("Error getting users: " + e.getMessage());
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
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            Role role = Role.builder().name(ERole.ROLE_USER).id(new ObjectId()).build();
            user.setRoles(Set.of(role));
            return userRepository.save(user);
        } catch (Exception e) {
            System.out.println("Error creating user: " + e.getMessage());
            return null;
        }
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

    public User login(User user) {
        try {
            User existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser == null) {
                return null;
            }
            if (passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                existingUser.setEnabled(true);
                Role role = Role.builder().name(ERole.ROLE_USER).id(new ObjectId()).build();
                existingUser.setRoles(Set.of(role));
                userRepository.save(existingUser);
                return existingUser;
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("Error logging in user: " + e.getMessage());
        }
        return null;
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

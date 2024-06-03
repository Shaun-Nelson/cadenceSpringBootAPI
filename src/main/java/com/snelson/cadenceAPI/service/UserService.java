package com.snelson.cadenceAPI.service;

import com.snelson.cadenceAPI.model.User;
import com.snelson.cadenceAPI.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

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

    public User login(User user, HttpSession session) {
        try {
            User existingUser = userRepository.findByUsername(user.getUsername());
            if (existingUser == null) {
                throw new Exception("User not found");
            }
            if (!existingUser.getPassword().equals(user.getPassword())) {
                throw new Exception("Incorrect password");
            }
            session.setAttribute("user", existingUser);
            existingUser.setEnabled(true);
            userRepository.save(existingUser);

            return existingUser;
        } catch (Exception e) {
            System.out.println("Error logging in user: " + e.getMessage());
            return null;
        }
    }
}

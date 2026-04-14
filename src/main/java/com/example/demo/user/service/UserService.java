package com.example.demo.user.service;

import com.example.demo.user.model.User;
import com.example.demo.user.model.UserRequest;
import com.example.demo.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public User createUser(UserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        return userRepository.create(user);
    }

    public Optional<User> updateUser(String id, UserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        return userRepository.update(id, user);
    }

    public boolean deleteUser(String id) {
        return userRepository.deleteById(id);
    }
}

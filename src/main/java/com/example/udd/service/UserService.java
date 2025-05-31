package com.example.udd.service;

import com.example.udd.dto.RegistrationDto;
import com.example.udd.model.User;
import com.example.udd.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        Optional<User> user=userRepository.findById(id);
        return user.orElse(null);
    }

    public String registerUser(RegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.username)) {
            return "Username already exists.";
        }

        User newUser = new User(
                registrationDto.firstName,
                registrationDto.lastName,
                registrationDto.username,
                encoder.encode(registrationDto.password)
        );

        userRepository.save(newUser);
        return "Registration successful!";
    }
}

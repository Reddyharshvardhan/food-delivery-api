package com.harsha.food_delivery.Service;

import com.harsha.food_delivery.dto.UserResponse;
import com.harsha.food_delivery.model.Role;
import com.harsha.food_delivery.model.User;
import com.harsha.food_delivery.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse registerUser(User user){

        user.setRole(Role.CUSTOMER);

        user.setPassword((passwordEncoder.encode(user.getPassword())));

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }
    public UserResponse toUserResponse(User user){
        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }
}

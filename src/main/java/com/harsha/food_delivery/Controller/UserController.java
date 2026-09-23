package com.harsha.food_delivery.Controller;

import com.harsha.food_delivery.Service.JwtService;
import com.harsha.food_delivery.Service.UserService;
import com.harsha.food_delivery.dto.LoginRequest;
import com.harsha.food_delivery.dto.UserResponse;
import com.harsha.food_delivery.model.User;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public UserResponse registerUser(@Valid @RequestBody User user){
        return userService.registerUser(user);
    }
    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest loginRequest){
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(),
                                loginRequest.getPassword()
                        )
                );
        String token = jwtService.generateToken(loginRequest.getEmail(),
                authentication.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority()
                        .replace("ROLE_","")
        );
        return token;
    }
}

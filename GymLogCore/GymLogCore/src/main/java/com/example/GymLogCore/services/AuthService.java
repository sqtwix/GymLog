package com.example.GymLogCore.services;

import com.example.GymLogCore.dto.AuthResponse;
import com.example.GymLogCore.dto.LoginRequest;
import com.example.GymLogCore.dto.RegisterRequest;
import com.example.GymLogCore.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.GymLogCore.domain.*;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // for hashing passwords
    private final JwtService jwtService;

    /*
    * User registration
    */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("User with this email already existed!");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setBirthDate(request.birthDate());

        if (request.gender() != null && !request.gender().isBlank()) {
            user.setGender(Gender.valueOf(request.gender().toUpperCase()));
        }
        user.setHashedPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken);
    }

    /*
    * User authorization
    */
    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Incorrect login or email"));

        if (!passwordEncoder.matches(request.password(), user.getHashedPassword())) {
            throw new RuntimeException("Incorrect mail or passowrd");
        }

        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken);
    }
}

package com.example.GymLogCore.services;

import com.example.GymLogCore.dto.AuthResponse;
import com.example.GymLogCore.dto.LoginRequest;
import com.example.GymLogCore.dto.RegisterRequest;
import com.example.GymLogCore.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
        if (request.email() == null || request.email().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        String email = request.email().trim();
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
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
        if (request.email() == null || request.email().isBlank() ||
                request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        String email = request.email().trim();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getHashedPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String jwtToken = jwtService.generateToken(user);

        return new AuthResponse(jwtToken);
    }
}

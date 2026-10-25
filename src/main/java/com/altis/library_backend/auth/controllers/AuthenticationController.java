
package com.altis.library_backend.auth.controllers;

import com.altis.library_backend.auth.models.dtos.AuthenticationRequestDTO;
import com.altis.library_backend.auth.models.dtos.ForgotPasswordRequestDTO;
import com.altis.library_backend.auth.models.dtos.AuthenticationResponseDTO;
import com.altis.library_backend.auth.models.dtos.RegisterRequestDTO;
import com.altis.library_backend.auth.services.TokenService;
import com.altis.library_backend.users.models.entities.UserEntity;
import com.altis.library_backend.users.repositories.UserRepository;
import com.altis.library_backend.users.services.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationController(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            UserService userService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(
            @RequestBody @Valid AuthenticationRequestDTO data
    ) {

        var usernamePassword =
                new UsernamePasswordAuthenticationToken(
                        data.email(),
                        data.password()
                );

        var auth = authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken(
                (UserEntity) auth.getPrincipal()
        );

        return ResponseEntity.ok(
                new AuthenticationResponseDTO(token)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody @Valid RegisterRequestDTO data
    ) {

        String email = data.email().trim().toLowerCase();

        String cpf = data.cpf().replaceAll("\\D", "");

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already registered"
            );
        }

        if (userRepository.existsByCpf(cpf)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "CPF already registered"
            );
        }

        String encryptedPassword =
                passwordEncoder.encode(data.password());

        UserEntity newUser = new UserEntity(
                data,
                encryptedPassword
        );

        newUser.setEmail(email);
        newUser.setCpf(cpf);

        userRepository.save(newUser);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequestDTO request
    ) {

        userService.forgotPassword(request);

        return ResponseEntity.noContent().build();
    }
}

package com.altis.library_backend.users.controllers;

import com.altis.library_backend.books.models.dtos.BookResponseDTO;
import com.altis.library_backend.users.models.dtos.*;
import com.altis.library_backend.users.models.entities.UserEntity;
import com.altis.library_backend.users.services.UserService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAll(
            @RequestParam(required = false) String nameCompleted,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String cpf,
            @ParameterObject Pageable pageable
    ) {
        Page<UserResponseDTO> responses = userService.findAll(nameCompleted,email,cpf, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
        UserResponseDTO response = userService.findById(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe(
            @AuthenticationPrincipal UserEntity loggedUser
    ) {
        UserResponseDTO response = userService.findById(loggedUser.getId());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UpdateResponseDTO> updateMe(
            @AuthenticationPrincipal UserEntity loggedUser,
            @RequestBody @Valid UpdateRequestDTO request
    ) {
        UpdateResponseDTO response =
                userService.updateUser(loggedUser.getId(), request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> adminUpdate(
            @PathVariable Long id,
            @RequestBody @Valid AdminUpdateUserRequestDTO request
    ) {
        UserResponseDTO response =
                userService.adminUpdateUser(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
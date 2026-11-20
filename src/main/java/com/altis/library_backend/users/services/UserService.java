
package com.altis.library_backend.users.services;

import com.altis.library_backend.auth.models.dtos.ForgotPasswordRequestDTO;
import com.altis.library_backend.users.models.dtos.*;
import com.altis.library_backend.users.models.entities.UserEntity;
import com.altis.library_backend.users.repositories.UserRepository;
import com.altis.library_backend.users.specifications.UserSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String normalizeCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    private UserResponseDTO toUserResponse(UserEntity user) {
        return new UserResponseDTO(
                user.getId(),
                user.getNameCompleted(),
                user.getEmail(),
                user.getPhone(),
                user.getCpf(),
                user.getDateBirth(),
                user.getAddress()
        );
    }

    private UserEntity findExistingUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with ID: " + id
                ));
    }

    private void validateEmailAvailability(
            UserEntity existingUser,
            String email
    ) {
        if (email == null || email.isBlank()) {
            return;
        }

        if (!existingUser.getEmail().equals(email)
                && userRepository.existsByEmail(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already in use by another account"
            );
        }
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {

        UserEntity user = findExistingUser(id);

        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found with ID: " + id
            );
        }

        return toUserResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAll(
            String nameCompleted,
            String email,
            String cpf,
            Pageable pageable
    ) {

        Specification<UserEntity> spec =
                UserSpecification.isNotAdmin()
                        .and(UserSpecification.hasName(nameCompleted))
                        .and(UserSpecification.hasEmail(email))
                        .and(UserSpecification.hasCpf(cpf));

        Page<UserEntity> users =
                userRepository.findAll(spec, pageable);

        return users.map(this::toUserResponse);
    }

    @Transactional
    public UpdateResponseDTO updateUser(
            Long id,
            UpdateRequestDTO request
    ) {

        UserEntity existingUser = findExistingUser(id);

        if (Boolean.TRUE.equals(existingUser.getIsAdmin())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin user cannot be edited."
            );
        }

        if (!passwordEncoder.matches(
                request.currentPassword(),
                existingUser.getPassword()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid current password!"
            );
        }

        validateEmailAvailability(existingUser, request.email());

        if (request.nameCompleted() != null
                && !request.nameCompleted().isBlank()) {
            existingUser.setNameCompleted(
                    request.nameCompleted()
            );
        }

        if (request.email() != null
                && !request.email().isBlank()) {
            existingUser.setEmail(
                    request.email()
            );
        }

        if (request.address() != null
                && !request.address().isBlank()) {
            existingUser.setAddress(
                    request.address()
            );
        }

        if (request.phone() != null
                && !request.phone().isBlank()) {
            existingUser.setPhone(
                    request.phone()
            );
        }

        if (request.password() != null
                && !request.password().isBlank()) {
            existingUser.setPassword(
                    passwordEncoder.encode(request.password())
            );
        }

        UserEntity savedUser =
                userRepository.save(existingUser);

        return new UpdateResponseDTO(
                savedUser.getId(),
                savedUser.getNameCompleted(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getAddress()
        );
    }

    @Transactional
    public UserResponseDTO adminUpdateUser(
            Long id,
            AdminUpdateUserRequestDTO request
    ) {

        UserEntity existingUser = findExistingUser(id);

        if (Boolean.TRUE.equals(existingUser.getIsAdmin())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin user cannot be edited."
            );
        }

        validateEmailAvailability(existingUser, request.email());

        if (request.cpf() != null
                && !request.cpf().isBlank()) {

            String newCpf = normalizeCpf(request.cpf());
            String currentCpf = normalizeCpf(existingUser.getCpf());

            if (!newCpf.equals(currentCpf)
                    && userRepository.existsByCpf(newCpf)) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "CPF already in use by another account"
                );
            }

            existingUser.setCpf(newCpf);
        }

        if (request.nameCompleted() != null
                && !request.nameCompleted().isBlank()) {
            existingUser.setNameCompleted(
                    request.nameCompleted()
            );
        }

        if (request.email() != null
                && !request.email().isBlank()) {
            existingUser.setEmail(
                    request.email()
            );
        }

        if (request.phone() != null
                && !request.phone().isBlank()) {
            existingUser.setPhone(
                    request.phone()
            );
        }

        if (request.address() != null
                && !request.address().isBlank()) {
            existingUser.setAddress(
                    request.address()
            );
        }

        if (request.dateBirth() != null) {
            existingUser.setDateBirth(
                    request.dateBirth()
            );
        }

        UserEntity savedUser =
                userRepository.save(existingUser);

        return toUserResponse(savedUser);
    }

    @Transactional
    public void forgotPassword(
            ForgotPasswordRequestDTO request
    ) {

        UserEntity existingUser = userRepository
                .findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid email or CPF"
                ));

        if (Boolean.TRUE.equals(existingUser.getIsAdmin())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin cannot reset password."
            );
        }

        String storedCpf = normalizeCpf(existingUser.getCpf());
        String informedCpf = normalizeCpf(request.cpf());

        if (storedCpf == null
                || !storedCpf.equals(informedCpf)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email or CPF"
            );
        }

        if (!request.newPassword().equals(
                request.confirmPassword()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Passwords do not match"
            );
        }

        existingUser.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {

        UserEntity existingUser = findExistingUser(id);

        if (Boolean.TRUE.equals(existingUser.getIsAdmin())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin user cannot be deleted."
            );
        }

        userRepository.delete(existingUser);
    }
}

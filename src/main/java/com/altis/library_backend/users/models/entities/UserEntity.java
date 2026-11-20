package com.altis.library_backend.users.models.entities;

import com.altis.library_backend.auth.models.dtos.RegisterRequestDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserEntity implements UserDetails{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name", nullable = false)
    private String nameCompleted;

    @Column(name="email",nullable = false, unique = true)
    private String email;

    @Column(name="phone",nullable = false)
    private String phone;

    @Column(name="cpf",nullable = false, unique = true)
    private String cpf;

    @Column(name="date_birth",nullable = false)
    private LocalDate dateBirth;

    @Column(name="address",nullable = false)
    private String address;

    @Column(name="is_admin",nullable = false)
    private Boolean isAdmin=false;

    @Column(name="password",nullable = false)
    private String password;

    @CreatedDate
    @Column(name="created_at" , nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserEntity(RegisterRequestDTO data, String encryptedPassword) {
        this.nameCompleted = data.nameCompleted();
        this.email = data.email();
        this.phone = data.phone();
        this.cpf = data.cpf();
        this.dateBirth = data.dateBirth();
        this.address = data.address();
        this.password = encryptedPassword;
        this.isAdmin = false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (Boolean.TRUE.equals(isAdmin)) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return email;
    }
}

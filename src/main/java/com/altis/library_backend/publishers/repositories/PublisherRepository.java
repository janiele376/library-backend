package com.altis.library_backend.publishers.repositories;


import com.altis.library_backend.publishers.models.entities.PublisherEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<PublisherEntity, Long> {

    boolean existsByEmail(String email);

    boolean existsByCnpj(String cnpj);

    Page<PublisherEntity> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
}

package com.altis.library_backend.books.models.entities;

import com.altis.library_backend.publishers.models.entities.PublisherEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EqualsAndHashCode(of = "id")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="isbn",nullable = false, unique = true)
    private String isbn;

    @Column(name="title", nullable = false)
    private String title;

    @Column(name = "genre", nullable = false)
    private String genre;

    @Column(name="release_date",nullable = false)
    private String releaseDate;

    @Column(name = "quantity")
    private Integer quantity=1;

    @ManyToOne
    @JoinColumn(name = "publisher_id")
    private PublisherEntity publisherId;

    @CreatedDate
    @Column(name="created_at" , nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
package com.altis.library_backend.publishers.controllers;

import com.altis.library_backend.publishers.models.dtos.PublisherRequestDTO;
import com.altis.library_backend.publishers.models.dtos.PublisherResponseDTO;
import com.altis.library_backend.publishers.services.PublisherService;
import com.altis.library_backend.publishers.models.dtos.UpdateRequestDTO;
import com.altis.library_backend.publishers.models.dtos.UpdateResponseDTO;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.net.URI;

@RestController
@RequestMapping("/publishers")
public class PublisherController {
    private final PublisherService publisherService;

    public PublisherController(PublisherService publisherService){
        this.publisherService = publisherService;
    }

    @PostMapping
    public ResponseEntity<PublisherResponseDTO> create(@RequestBody @Valid PublisherRequestDTO request){
        PublisherResponseDTO response = publisherService.createPublisher(request);

        URI location = URI.create("/publishers/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<PublisherResponseDTO>> getAll(
            @RequestParam(required = false) String name,
            @ParameterObject Pageable pageable
    ) {
        Page<PublisherResponseDTO> responses =
                publisherService.findAll(name, pageable);

        return ResponseEntity.ok(responses);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PublisherResponseDTO> getById(@PathVariable Long id){
        PublisherResponseDTO response = publisherService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UpdateResponseDTO> update(@PathVariable Long id, @RequestBody @Valid UpdateRequestDTO request) {
        UpdateResponseDTO response = publisherService.updatePublisher(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PublisherResponseDTO> delete(@PathVariable Long id) {

        publisherService.deletePublisher(id);
        return ResponseEntity.noContent().build();

    }
}


package com.altis.library_backend.rentals.controllers;

import com.altis.library_backend.rentals.models.dtos.RentalRequestDTO;
import com.altis.library_backend.rentals.models.dtos.RentalResponseDTO;
import com.altis.library_backend.rentals.models.dtos.RentalUpdateDTO;
import com.altis.library_backend.rentals.services.RentalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/rentals")
@Validated
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping
    public ResponseEntity<RentalResponseDTO> create(
            @Valid @RequestBody RentalRequestDTO request
    ) {
        RentalResponseDTO response = rentalService.createRental(request);

        URI location = URI.create("/rentals/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<RentalResponseDTO> update(
            @PathVariable @Positive Long id,
            @Valid @RequestBody RentalUpdateDTO request
    ) {
        RentalResponseDTO response = rentalService.updateRental(id, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<RentalResponseDTO>> getAll(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String bookTitle,
            @ParameterObject Pageable pageable
    ) {
        Page<RentalResponseDTO> responses =
                rentalService.findAll(userName, bookTitle, pageable);

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalResponseDTO> getById(
            @PathVariable @Positive Long id
    ) {
        RentalResponseDTO response = rentalService.findById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<RentalResponseDTO> renew(
            @PathVariable @Positive Long id
    ) {
        RentalResponseDTO response = rentalService.renewRental(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<RentalResponseDTO> returnRental(
            @PathVariable @Positive Long id
    ) {
        RentalResponseDTO response = rentalService.returnRental(id);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable @Positive Long id
    ) {
        rentalService.deleteRental(id);

        return ResponseEntity.noContent().build();
    }
}

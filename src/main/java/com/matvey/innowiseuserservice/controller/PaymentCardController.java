package com.matvey.innowiseuserservice.controller;

import com.matvey.innowiseuserservice.dto.PaymentCardDto;
import com.matvey.innowiseuserservice.service.PaymentCardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment-cards")
public class PaymentCardController {

    @Autowired
    private PaymentCardService paymentCardService;

    @PostMapping
    public ResponseEntity<PaymentCardDto> createPaymentCard(@Valid @RequestBody PaymentCardDto paymentCardDto) {
        PaymentCardDto createdCard = paymentCardService.create(paymentCardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardDto> getPaymentCardById(@PathVariable UUID id) {
        PaymentCardDto paymentCardDto = paymentCardService.getById(id);
        return ResponseEntity.ok(paymentCardDto);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardDto>> getAllPaymentCards(
            @RequestParam(required = false) String holder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<PaymentCardDto> cards = paymentCardService.getAll(holder, pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentCardDto>> getCardsByUserId(@PathVariable UUID userId) {
        List<PaymentCardDto> cards = paymentCardService.getByUserId(userId);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardDto> updatePaymentCard(@PathVariable UUID id, @Valid @RequestBody PaymentCardDto paymentCardDto) {
        PaymentCardDto updatedCard = paymentCardService.update(id, paymentCardDto);
        return ResponseEntity.ok(updatedCard);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activatePaymentCard(@PathVariable UUID id) {
        paymentCardService.activate(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivatePaymentCard(@PathVariable UUID id) {
        paymentCardService.deactivate(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaymentCard(@PathVariable UUID id) {
        paymentCardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

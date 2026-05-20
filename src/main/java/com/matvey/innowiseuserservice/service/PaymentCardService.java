package com.matvey.innowiseuserservice.service;

import com.matvey.innowiseuserservice.dto.PaymentCardDto;
import com.matvey.innowiseuserservice.entity.PaymentCard;
import com.matvey.innowiseuserservice.entity.User;
import com.matvey.innowiseuserservice.mapper.PaymentCardMapper;
import com.matvey.innowiseuserservice.repository.PaymentCardRepository;
import com.matvey.innowiseuserservice.repository.UserRepository;
import com.matvey.innowiseuserservice.specification.PaymentCardSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentCardService {

    @Autowired
    private PaymentCardRepository paymentCardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardMapper paymentCardMapper;

    public PaymentCardDto create(PaymentCardDto paymentCardDto) {
        User user = userRepository.findById(paymentCardDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + paymentCardDto.getUserId()));

        List<PaymentCard> existingCards = paymentCardRepository.findByUserId(user.getId());
        if (existingCards.size() >= 5) {
            throw new RuntimeException("User cannot have more than 5 payment cards");
        }

        PaymentCard paymentCard = paymentCardMapper.toEntity(paymentCardDto);
        paymentCard.setUser(user);
        PaymentCard savedCard = paymentCardRepository.save(paymentCard);
        return paymentCardMapper.toDto(savedCard);
    }

    public PaymentCardDto getById(UUID id) {
        PaymentCard paymentCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment card not found with id: " + id));
        return paymentCardMapper.toDto(paymentCard);
    }

    public Page<PaymentCardDto> getAll(String holder, Pageable pageable) {
        Specification<PaymentCard> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (holder != null) {
            spec = spec.and(PaymentCardSpecification.byHolder(holder));
        }

        Page<PaymentCard> cardPage = paymentCardRepository.findAll(spec, pageable);
        return cardPage.map(paymentCardMapper::toDto);
    }

    public List<PaymentCardDto> getByUserId(UUID userId) {
        List<PaymentCard> cards = paymentCardRepository.findByUserId(userId);
        return cards.stream().map(paymentCardMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public PaymentCardDto update(UUID id, PaymentCardDto paymentCardDto) {
        PaymentCard existingCard = paymentCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment card not found with id: " + id));
        paymentCardMapper.updateEntityFromDto(paymentCardDto, existingCard);
        PaymentCard updatedCard = paymentCardRepository.save(existingCard);
        return paymentCardMapper.toDto(updatedCard);
    }

    @Transactional
    public void activate(UUID id) {
        paymentCardRepository.updateActiveStatus(id, true);
    }

    @Transactional
    public void deactivate(UUID id) {
        paymentCardRepository.updateActiveStatus(id, false);
    }

    @Transactional
    public void delete(UUID id) {
        paymentCardRepository.deleteById(id);
    }
}

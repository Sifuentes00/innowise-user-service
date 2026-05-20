package com.matvey.innowiseuserservice.mapper;

import com.matvey.innowiseuserservice.dto.PaymentCardDto;
import com.matvey.innowiseuserservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface PaymentCardMapper {

    PaymentCardDto toDto(PaymentCard paymentCard);

    @Mapping(target = "user", ignore = true)
    PaymentCard toEntity(PaymentCardDto paymentCardDto);

    void updateEntityFromDto(PaymentCardDto paymentCardDto, @MappingTarget PaymentCard paymentCard);
}

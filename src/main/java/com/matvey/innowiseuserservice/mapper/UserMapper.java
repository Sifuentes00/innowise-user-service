package com.matvey.innowiseuserservice.mapper;

import com.matvey.innowiseuserservice.dto.PaymentCardDto;
import com.matvey.innowiseuserservice.dto.UserDto;
import com.matvey.innowiseuserservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = PaymentCardMapper.class)
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "paymentCards", ignore = true)
    User toEntity(UserDto userDto);

    @Mapping(target = "paymentCards", ignore = true)
    void updateEntityFromDto(UserDto userDto, @MappingTarget User user);
}

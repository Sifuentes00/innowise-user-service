package com.matvey.innowiseuserservice.mapper;

import com.matvey.innowiseuserservice.dto.UserDto;
import com.matvey.innowiseuserservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    User toEntity(UserDto userDto);

    void updateEntityFromDto(UserDto userDto, @MappingTarget User user);
}

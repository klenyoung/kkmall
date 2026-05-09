package com.kkmall.account.interfaces.dto;

import com.kkmall.account.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * User 领域对象与 UserProfileDto 之间的转换器。
 */
@Mapper(componentModel = "spring")
public interface UserProfileDtoMapper {

    @Mapping(target = "phone", expression = "java(user.getPhone().value())")
    @Mapping(target = "gender", expression = "java(user.getGender() == null ? \"UNKNOWN\" : user.getGender().name())")
    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserProfileDto toDto(User user);
}

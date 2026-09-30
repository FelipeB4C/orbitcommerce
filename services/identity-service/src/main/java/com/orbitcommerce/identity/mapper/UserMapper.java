package com.orbitcommerce.identity.mapper;

import com.orbitcommerce.identity.dto.UserDetailDTO;
import com.orbitcommerce.identity.dto.UserRegisterDTO;
import com.orbitcommerce.identity.dto.UserRegisterResponseDTO;
import com.orbitcommerce.identity.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRegisterDTO userRegisterDTO);

    @Mapping(
            target = "status",
            expression = "java(user.getStatus() == null ? null : user.getStatus().name())"
    )
    UserRegisterResponseDTO toRegisterResponselDTO(User user);

    @Mapping(
            target = "status",
            expression = "java(user.getStatus() == null ? null : user.getStatus().name())"
    )
    UserDetailDTO toDetailDTO(User user);

}

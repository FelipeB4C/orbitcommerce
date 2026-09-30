package com.orbitcommerce.identity.mapper;

import com.orbitcommerce.identity.dto.UserDetailDTO;
import com.orbitcommerce.identity.dto.UserRegisterDTO;
import com.orbitcommerce.identity.dto.UserRegisterResponseDTO;
import com.orbitcommerce.identity.model.Role;
import com.orbitcommerce.identity.model.User;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-09-29T23:39:57-0300",
    comments = "version: 1.6.3, compiler: IncrementalProcessingEnvironment from gradle-java-compiler-worker-9.7.1.jar, environment: Java 25.0.4.1 (Azul Systems, Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(UserRegisterDTO userRegisterDTO) {
        if ( userRegisterDTO == null ) {
            return null;
        }

        String passwordHash = null;
        String fullName = null;
        String email = null;

        passwordHash = userRegisterDTO.password();
        fullName = userRegisterDTO.fullName();
        email = userRegisterDTO.email();

        User user = new User( fullName, email, passwordHash );

        return user;
    }

    @Override
    public UserRegisterResponseDTO toRegisterResponselDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String fullName = null;
        String email = null;
        String createdAt = null;

        id = user.getId();
        fullName = user.getFullName();
        email = user.getEmail();
        if ( user.getCreatedAt() != null ) {
            createdAt = user.getCreatedAt().toString();
        }

        String status = user.getStatus() == null ? null : user.getStatus().name();

        UserRegisterResponseDTO userRegisterResponseDTO = new UserRegisterResponseDTO( id, fullName, email, status, createdAt );

        return userRegisterResponseDTO;
    }

    @Override
    public UserDetailDTO toDetailDTO(User user) {
        if ( user == null ) {
            return null;
        }

        UUID id = null;
        String fullName = null;
        String email = null;
        Set<Role> roles = null;
        String createdAt = null;

        id = user.getId();
        fullName = user.getFullName();
        email = user.getEmail();
        Set<Role> set = user.getRoles();
        if ( set != null ) {
            roles = new LinkedHashSet<Role>( set );
        }
        if ( user.getCreatedAt() != null ) {
            createdAt = user.getCreatedAt().toString();
        }

        String status = user.getStatus() == null ? null : user.getStatus().name();

        UserDetailDTO userDetailDTO = new UserDetailDTO( id, fullName, email, roles, status, createdAt );

        return userDetailDTO;
    }
}

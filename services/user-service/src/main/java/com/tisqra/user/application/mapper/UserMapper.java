package com.tisqra.user.application.mapper;

import com.tisqra.user.application.dto.CreateUserRequest;
import com.tisqra.user.application.dto.UserDTO;
import com.tisqra.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for User entity
 */
@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    UserDTO toDTO(User user);

    User toEntity(CreateUserRequest request);

    void updateEntityFromDTO(com.tisqra.user.application.dto.UpdateUserRequest dto, @MappingTarget User entity);
}

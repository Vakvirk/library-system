package com.blewandowicz.library_system.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.blewandowicz.library_system.user.dto.UserCreateDTO;
import com.blewandowicz.library_system.user.dto.UserFetchDTO;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isStudent", ignore = true)
    @Mapping(target = "studentInstitiution", ignore = true)
    @Mapping(target = "studentProof", ignore = true)
    User userCreateToUser(UserCreateDTO userCreateDTO);

    UserFetchDTO userToUserFetch(User user);
}

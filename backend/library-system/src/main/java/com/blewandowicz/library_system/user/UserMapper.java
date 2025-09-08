package com.blewandowicz.library_system.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.blewandowicz.library_system.auth.dto.RegisterRequest;
import com.blewandowicz.library_system.user.dto.UserFetchDTO;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * Converts a RegisterRequest into a new User instance.
     *
     * <p>Produces a User populated from the request. The source `password` is mapped to the
     * target's `passwordHash`. The following target fields are intentionally left unset:
     * id, createdAt, updatedAt, isActive, role, isStudent, studentInstitiution, and studentProof.
     *
     * @param registerRequest request DTO containing user registration data
     * @return a User populated from the provided request (with the ignored fields unset)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "isStudent", ignore = true)
    @Mapping(target = "studentInstitiution", ignore = true)
    @Mapping(target = "studentProof", ignore = true)
    @Mapping(source = "password", target = "passwordHash")
    User registerToUser(RegisterRequest registerRequest);

    /**
 * Maps a User entity to a UserFetchDTO.
 *
 * Performs by-name field mapping from the source User to a new UserFetchDTO instance.
 *
 * @param user the source User to map
 * @return a UserFetchDTO with fields copied from the source User, or {@code null} if {@code user} is {@code null}
 */
UserFetchDTO userToUserFetch(User user);
}

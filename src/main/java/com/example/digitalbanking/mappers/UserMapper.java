package com.example.digitalbanking.mappers;

import com.example.digitalbanking.dtos.AppUserDTO;
import com.example.digitalbanking.dtos.RoleDTO;
import com.example.digitalbanking.entities.AppUser;
import com.example.digitalbanking.entities.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(mapRolesToStrings(appUser.getRoles()))")
    AppUserDTO toAppUserDTO(AppUser appUser);

    List<AppUserDTO> toAppUserDTOs(List<AppUser> appUsers);

    RoleDTO toRoleDTO(Role role);
    List<RoleDTO> toRoleDTOs(List<Role> roles);

    default List<String> mapRolesToStrings(List<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
    }
}

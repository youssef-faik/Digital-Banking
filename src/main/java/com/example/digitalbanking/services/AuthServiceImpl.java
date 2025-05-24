package com.example.digitalbanking.services;

import com.example.digitalbanking.dtos.AppUserDTO;
import com.example.digitalbanking.dtos.ChangePasswordRequestDTO;
import com.example.digitalbanking.dtos.RegisterRequestDTO;
import com.example.digitalbanking.entities.AppUser;
import com.example.digitalbanking.entities.Role;
import com.example.digitalbanking.mappers.UserMapper;
import com.example.digitalbanking.repositories.AppUserRepository;
import com.example.digitalbanking.repositories.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuthServiceImpl(AppUserRepository appUserRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public AppUserDTO registerUser(RegisterRequestDTO registerRequestDTO) {
        if (appUserRepository.findByUsername(registerRequestDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Error: Username is already taken!");
        }
        if (appUserRepository.findByEmail(registerRequestDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        AppUser appUser = new AppUser();
        appUser.setUsername(registerRequestDTO.getUsername());
        appUser.setEmail(registerRequestDTO.getEmail());
        appUser.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));

        // Assign a default role, e.g., "USER"
        // Ensure the role exists in the database, or create it
        Role userRole = roleRepository.findByRoleName("USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "USER")));
        List<Role> roles = new ArrayList<>();
        roles.add(userRole);
        appUser.setRoles(roles);

        AppUser savedUser = appUserRepository.save(appUser);
        return userMapper.toAppUserDTO(savedUser);
    }

    @Override
    public void changePassword(String username, ChangePasswordRequestDTO changePasswordRequestDTO) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(changePasswordRequestDTO.getOldPassword(), appUser.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }
        appUser.setPassword(passwordEncoder.encode(changePasswordRequestDTO.getNewPassword()));
        appUserRepository.save(appUser);
    }

    @Override
    public AppUserDTO getAuthenticatedUser(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toAppUserDTO(appUser);
    }
}

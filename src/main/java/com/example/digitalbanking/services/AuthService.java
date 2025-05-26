package com.example.digitalbanking.services;

import com.example.digitalbanking.dtos.AppUserDTO;
import com.example.digitalbanking.dtos.ChangePasswordRequestDTO;
import com.example.digitalbanking.dtos.RegisterRequestDTO;
import com.example.digitalbanking.dtos.UpdateProfileRequestDTO;

public interface AuthService {
    AppUserDTO registerUser(RegisterRequestDTO registerRequestDTO);
    void changePassword(String username, ChangePasswordRequestDTO changePasswordRequestDTO);
    AppUserDTO getAuthenticatedUser(String username);
    AppUserDTO updateProfile(String username, UpdateProfileRequestDTO updateProfileRequestDTO);
}

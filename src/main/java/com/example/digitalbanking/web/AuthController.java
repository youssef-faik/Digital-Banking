package com.example.digitalbanking.web;

import com.example.digitalbanking.dtos.*;
import com.example.digitalbanking.security.JwtUtil;
import com.example.digitalbanking.services.AuthService;
import com.example.digitalbanking.services.UserDetailsServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API for user authentication and account management")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager, UserDetailsServiceImpl userDetailsService, JwtUtil jwtUtil, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @Operation(
        summary = "Register a new user", 
        description = "Registers a new user with the provided details",
        security = {}  // No security for registration endpoint
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User successfully registered",
            content = @Content(schema = @Schema(implementation = AppUserDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Bad request - Username already exists or invalid data",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User registration details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = RegisterRequestDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"username\": \"john_doe\",\n" +
                            "  \"password\": \"SecurePass123\",\n" +
                            "  \"email\": \"john.doe@example.com\",\n" +
                            "  \"firstName\": \"John\",\n" +
                            "  \"lastName\": \"Doe\"\n" +
                            "}"
                )
            )
        )
        @RequestBody RegisterRequestDTO registerRequestDTO
    ) {
        try {
            AppUserDTO registeredUser = authService.registerUser(registerRequestDTO);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
        summary = "Authenticate user", 
        description = "Authenticates a user and returns a JWT token",
        security = {} // No security for login endpoint
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Authentication successful",
            content = @Content(schema = @Schema(implementation = JwtResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Authentication failed - Invalid credentials",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User login credentials",
            required = true,
            content = @Content(
                schema = @Schema(implementation = LoginRequestDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"username\": \"john_doe\",\n" +
                            "  \"password\": \"SecurePass123\"\n" +
                            "}"
                )
            )
        )
        @RequestBody LoginRequestDTO loginRequestDTO
    ) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword())
            );
        } catch (Exception e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestDTO.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new JwtResponseDTO(jwt));
    }

    @Operation(
        summary = "Change password", 
        description = "Allows an authenticated user to change their password",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Password changed successfully",
            content = @Content(schema = @Schema(type = "string"))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Bad request - Current password incorrect or new password invalid",
            content = @Content(schema = @Schema(type = "string"))
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - User not authenticated",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Password change details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = ChangePasswordRequestDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"currentPassword\": \"CurrentPass123\",\n" +
                            "  \"newPassword\": \"NewSecurePass456\",\n" +
                            "  \"confirmNewPassword\": \"NewSecurePass456\"\n" +
                            "}"
                )
            )
        )
        @RequestBody ChangePasswordRequestDTO changePasswordRequestDTO
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        try {
            authService.changePassword(currentPrincipalName, changePasswordRequestDTO);
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }    @Operation(
        summary = "Get current user profile", 
        description = "Retrieves the profile of the currently authenticated user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "User profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = AppUserDTO.class))
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - User not authenticated",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        try {
            AppUserDTO userDTO = authService.getAuthenticatedUser(currentPrincipalName);
            return ResponseEntity.ok(userDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(
        summary = "Update user profile", 
        description = "Updates the profile information of the currently authenticated user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = AppUserDTO.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Bad request - Invalid data provided",
            content = @Content(schema = @Schema(type = "string"))
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - User not authenticated",
            content = @Content(schema = @Schema(type = "string"))
        )
    })
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Profile update details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = UpdateProfileRequestDTO.class),
                examples = @ExampleObject(
                    value = "{\n" +
                            "  \"email\": \"john.updated@example.com\",\n" +
                            "  \"firstName\": \"John\",\n" +
                            "  \"lastName\": \"Doe Updated\",\n" +
                            "  \"phoneNumber\": \"+1234567890\"\n" +
                            "}"
                )
            )
        )
        @Valid @RequestBody UpdateProfileRequestDTO updateProfileRequestDTO
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        try {
            AppUserDTO updatedUser = authService.updateProfile(currentPrincipalName, updateProfileRequestDTO);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

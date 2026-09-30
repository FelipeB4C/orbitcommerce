package com.orbitcommerce.identity.controller;

import com.orbitcommerce.identity.dto.ChangePasswordDTO;
import com.orbitcommerce.identity.dto.UserDetailDTO;
import com.orbitcommerce.identity.dto.UserRegisterDTO;
import com.orbitcommerce.identity.dto.UserRegisterResponseDTO;
import com.orbitcommerce.identity.model.User;
import com.orbitcommerce.identity.service.TokenService;
import com.orbitcommerce.identity.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Operation(summary = "Register a new user account", description = "Creates a new customer account with PENDING_VERIFICATION status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload or validation error"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponseDTO> userRegister(
            @RequestBody @Valid UserRegisterDTO userRequest,
            UriComponentsBuilder uriBuilder) {
        UserRegisterResponseDTO userDetailDTO = userService.createUser(userRequest);
        URI uri = uriBuilder.path("/users/{id}").buildAndExpand(userDetailDTO.id()).toUri();
        return ResponseEntity.created(uri).body(userDetailDTO);
    }

    @Operation(
            summary = "Get current authenticated user profile",
            description = "Returns detailed information about the user currently authenticated via JWT[cite: 1].",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication token")
    })
    @GetMapping("/me")
    public ResponseEntity<UserDetailDTO> listDetailUserMe(@AuthenticationPrincipal User user) {
        UserDetailDTO userDetails = userService.userDetails(user);
        return ResponseEntity.ok(userDetails);
    }

    @Operation(
            summary = "Change user password",
            description = "Updates the password for the authenticated user after validating the current one.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or incorrect current password"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user,
                                               @RequestBody @Valid ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(user, changePasswordDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Block a user account",
            description = "Administrative action to block a user account. Restricted to administrators.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User blocked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden (requires admin role)"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{id}/block")
    public ResponseEntity<Void> userBlocked(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        userService.blockUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Unlock a user account",
            description = "Administrative action to unlock a blocked user account. Restricted to administrators.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User unlocked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/{id}/unlock")
    public ResponseEntity<Void> userUnlocked(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        userService.unlockUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Soft delete user account",
            description = "Anonymizes user data, marks status as DELETED, and revokes all active sessions globally.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User successfully deleted and sessions revoked"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id, @AuthenticationPrincipal User userLoggedIn) {
        userService.softDelete(id, userLoggedIn);
        tokenService.revokeAllSessions(id);
        return ResponseEntity.noContent().build();
    }

}

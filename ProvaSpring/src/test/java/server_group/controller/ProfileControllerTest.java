package server_group.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server_group.dto.AssignedTaskDTO;
import server_group.dto.ProfileDTO;
import server_group.model.CustomUser;
import server_group.repository.CustomUserRepository;
import server_group.security.Role;
import server_group.service.CustomUserService;
import server_group.service.ProcessStepExecutionService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    @Mock
    private CustomUserService customUserService;

    @Mock
    private CustomUserRepository customUserRepository;

    @Mock
    private ProcessStepExecutionService processStepExecutionService;

    @InjectMocks
    private ProfileController profileController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- GET PROFILE ---
    @Test
    void getProfile_successful() {
        // Arrange
        CustomUser user = new CustomUser();
        user.setEmail("test@email.com");
        user.setName("John");
        user.setSurname("Doe");
        when(customUserService.findByEmail("test@email.com")).thenReturn(user);

        // Act
        ResponseEntity<?> result = profileController.getProfile("test@email.com");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody() instanceof ProfileDTO);
        verify(customUserService, times(1)).findByEmail("test@email.com");
    }

    @Test
    void getProfile_notFound() {
        // Arrange
        when(customUserService.findByEmail("missing@email.com")).thenReturn(null);

        // Act
        ResponseEntity<?> result = profileController.getProfile("missing@email.com");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User not found!", result.getBody());
    }

    // --- UPDATE PROFILE ---
    @Test
    void updateProfile_successful() {
        // Arrange
        CustomUser existing = new CustomUser();
        existing.setEmail("email@test.com");

        CustomUser updated = new CustomUser();
        updated.setName("John");
        updated.setSurname("Doe");
        updated.setPhone("333 565 1235");
        updated.setRole(Role.EMPLOYEE);

        when(customUserRepository.findByEmail("email@test.com")).thenReturn(Optional.of(existing));
        when(customUserService.updateUser(any(CustomUser.class))).thenReturn(existing);

        // Act
        ResponseEntity<?> result = profileController.updateProfile("email@test.com", updated);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody() instanceof ProfileDTO);
        verify(customUserRepository, times(1)).findByEmail("email@test.com");
        verify(customUserService, times(1)).updateUser(existing);
    }

    @Test
    void updateProfile_notFound() {
        // Arrange
        when(customUserRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = profileController.updateProfile("missing@email.com", new CustomUser());

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User not found", result.getBody());
    }

    // --- DELETE PROFILE ---
    @Test
    void deleteProfile_successful() {
        // Arrange
        CustomUser existing = new CustomUser();
        existing.setEmail("email@test.com");
        when(customUserRepository.findByEmail("email@test.com")).thenReturn(Optional.of(existing));

        // Act
        ResponseEntity<?> result = profileController.deleteProfile("email@test.com");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User deleted successfully", result.getBody());
        verify(customUserRepository, times(1)).deleteById("email@test.com");
    }

    @Test
    void deleteProfile_notFound() {
        // Arrange
        when(customUserRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> result = profileController.deleteProfile("missing@email.com");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User not found", result.getBody());
        verify(customUserRepository, never()).deleteById(anyString());
    }
}


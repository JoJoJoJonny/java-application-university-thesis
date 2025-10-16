package server_group.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import server_group.dto.LoginRequestDTO;
import server_group.dto.LoginResponseDTO;
import server_group.dto.RegisterRequestDTO;
import server_group.security.Role;
import server_group.service.CustomUserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Mock
    private CustomUserService customUserService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // inizializza i mock
    }

    // --- TEST LOGIN ---

    @Test
    void login_successful() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("test@email.com", "password");
        LoginResponseDTO response = new LoginResponseDTO("test@email.com", Role.EMPLOYEE.name(),"fake-jwt-token");
        when(customUserService.loginUser(request)).thenReturn(response);

        // Act
        ResponseEntity<?> result = authController.login(request);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(response, result.getBody());
        verify(customUserService, times(1)).loginUser(request);
    }

    @Test
    void login_invalidCredentials() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("wrong@email.com", "wrongpass");
        when(customUserService.loginUser(request)).thenThrow(new BadCredentialsException("Invalid"));

        // Act
        ResponseEntity<?> result = authController.login(request);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertEquals("Invalid email or password!", result.getBody());
    }

    // --- TEST REGISTER ---

    @Test
    void register_successful() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("name", "surname", "email@test.com", "Password123!", "333 565 1568", Role.EMPLOYEE);
        when(customUserService.registerUser(request)).thenReturn(true);

        // Act
        ResponseEntity<String> result = authController.register(request);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("User registered successfully!", result.getBody());
    }

    @Test
    void register_emailAlreadyExists() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("name", "surname", "email@test.com", "Password123!", "333 565 1568", Role.EMPLOYEE);
        when(customUserService.registerUser(request)).thenReturn(false);

        // Act
        ResponseEntity<String> result = authController.register(request);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Email already in use!", result.getBody());
    }
}

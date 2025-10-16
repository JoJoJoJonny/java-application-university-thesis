package server_group.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import server_group.dto.LoginRequestDTO;
import server_group.dto.LoginResponseDTO;
import server_group.dto.RegisterRequestDTO;
import server_group.model.CustomUser;
import server_group.model.CustomUserDetails;
import server_group.repository.CustomUserRepository;
import server_group.security.JwtUtil;
import server_group.security.Role;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomUserServiceTest {
    @Mock
    private CustomUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private CustomUserService customUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- getUsersByRole ---
    @Test
    void getUsersByRole_returnsList() {
        // Arrange
        CustomUser user = new CustomUser();
        user.setRole(Role.EMPLOYEE);
        when(userRepository.findByRole(Role.EMPLOYEE)).thenReturn(List.of(user));

        // Act
        List<CustomUser> result = customUserService.getUsersByRole(Role.EMPLOYEE);

        // Assert
        assertEquals(1, result.size());
        assertEquals(Role.EMPLOYEE, result.get(0).getRole());
    }

    // --- findByEmail ---
    @Test
    void findByEmail_found() {
        // Arrange
        CustomUser user = new CustomUser();
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));

        // Act
        CustomUser result = customUserService.findByEmail("test@email.com");

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail("test@email.com");
    }

    @Test
    void findByEmail_notFound() {
        // Arrange
        when(userRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

        // Act
        CustomUser result = customUserService.findByEmail("missing@email.com");

        // Assert
        assertNull(result);
    }

    // --- loginUser ---
    @Test
    void loginUser_success() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("test@email.com", "password");
        Authentication auth = mock(Authentication.class);
        CustomUser user = new CustomUser();
        user.setEmail("test@email.com");
        user.setRole(Role.EMPLOYEE);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("fake-jwt-token");

        // Act
        LoginResponseDTO response = customUserService.loginUser(request);

        // Assert
        assertEquals("test@email.com", response.getEmail());
        assertEquals("EMPLOYEE", response.getRole());
        assertEquals("fake-jwt-token", response.getToken());
    }

    @Test
    void loginUser_badCredentials() {
        // Arrange
        LoginRequestDTO request = new LoginRequestDTO("wrong@email.com", "pass");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid"));

        // Assert
        assertThrows(BadCredentialsException.class, () -> customUserService.loginUser(request));
    }

    // --- registerUser ---
    @Test
    void registerUser_success() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("John", "Doe", "email@test.com", "Password123!", "333 565 1235",Role.EMPLOYEE);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPass");

        // Act
        boolean result = customUserService.registerUser(request);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).save(any(CustomUser.class));
    }

    @Test
    void registerUser_emailExists() {
        // Arrange
        RegisterRequestDTO request = new RegisterRequestDTO("John", "Doe", "email@test.com", "Password123!", "333 123 4567" ,Role.EMPLOYEE);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act
        boolean result = customUserService.registerUser(request);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    // --- updateUser ---
    @Test
    void updateUser_savesUser() {
        CustomUser user = new CustomUser();
        when(userRepository.save(user)).thenReturn(user);

        CustomUser result = customUserService.updateUser(user);

        assertEquals(user, result);
        verify(userRepository, times(1)).save(user);
    }

    // --- deleteUserByEmail ---
    @Test
    void deleteUserByEmail_callsRepository() {
        String email = "email@test.com";
        doNothing().when(userRepository).deleteById(email);

        customUserService.deleteUserByEmail(email);

        verify(userRepository, times(1)).deleteById(email);
    }
}

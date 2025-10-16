package server_group.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import server_group.model.CustomUser;
import server_group.model.CustomUserDetails;
import server_group.repository.CustomUserRepository;
import server_group.security.Role;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomUserDetailsServiceTest {

    @Mock
    private CustomUserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- loadUserByUsername ---
    @Test
    void loadUserByUsername_found() {
        // Arrange
        CustomUser user = new CustomUser();
        user.setEmail("user@test.com");
        user.setPassword("hashedPassword");
        user.setRole(Role.EMPLOYEE);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("user@test.com");

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof CustomUserDetails);
        assertEquals("user@test.com", result.getUsername());
        assertEquals("hashedPassword", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE")));

        verify(userRepository, times(1)).findByEmail("user@test.com");
    }

    @Test
    void loadUserByUsername_notFound() {
        // Arrange
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername("missing@test.com")
        );

        verify(userRepository, times(1)).findByEmail("missing@test.com");
    }
}

package server_group.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server_group.model.CustomUser;
import server_group.security.Role;
import server_group.service.CustomUserService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    @Mock
    private CustomUserService customUserService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // --- TEST GET ALL EMPLOYEES ---
    @Test
    void getAllEmployees_successful() {
        // Arrange
        CustomUser user1 = new CustomUser();
        CustomUser user2 = new CustomUser();
        List<CustomUser> employees = Arrays.asList(user1, user2);

        when(customUserService.getUsersByRole(Role.EMPLOYEE)).thenReturn(employees);

        // Act
        ResponseEntity<List<CustomUser>> result = employeeController.getAllEmployees();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(employees, result.getBody());
        verify(customUserService, times(1)).getUsersByRole(Role.EMPLOYEE);
    }
}

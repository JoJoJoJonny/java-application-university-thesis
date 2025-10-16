package server_group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import server_group.model.CustomUser;
import server_group.security.Role;
import server_group.service.CustomUserService;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final CustomUserService customUserService;

    public EmployeeController(CustomUserService customUserService) {
        this.customUserService = customUserService;
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    @GetMapping("/list")
    public ResponseEntity<List<CustomUser>> getAllEmployees() {
        List<CustomUser> employees = customUserService.getUsersByRole(Role.EMPLOYEE);
        return ResponseEntity.ok(employees);
    }
}
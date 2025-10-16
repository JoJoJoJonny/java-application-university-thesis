package server_group.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import server_group.dto.AssignedTaskDTO;
import server_group.dto.ProfileDTO;
import server_group.model.CustomUser;
import server_group.model.ProcessStepExecution;
import server_group.repository.CustomUserRepository;
import server_group.repository.ProcessStepExecutionRepository;
import server_group.service.CustomUserService;
import server_group.service.ProcessStepExecutionService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final CustomUserService customUserService;

    private final CustomUserRepository customUserRepository;

    private final ProcessStepExecutionService processStepExecutionService;

    public ProfileController(CustomUserService customUserService, CustomUserRepository customUserRepository, ProcessStepExecutionService processStepExecutionService) {
        this.customUserService = customUserService;
        this.customUserRepository = customUserRepository;
        this.processStepExecutionService = processStepExecutionService;
    }

    //profilo per email
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @GetMapping
    public ResponseEntity<?> getProfile(@RequestParam String email) {
        CustomUser user = customUserService.findByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(new ProfileDTO(user));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }
    }

    //modifica dati profilo
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @PutMapping("/{email}")
    public ResponseEntity<?> updateProfile(@PathVariable String email, @RequestBody CustomUser updatedUser) {
        Optional<CustomUser> existingOpt = customUserRepository.findByEmail(email);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        CustomUser existing = existingOpt.get();

        existing.setName(updatedUser.getName());
        existing.setSurname(updatedUser.getSurname());
        existing.setPhone(updatedUser.getPhone());
        existing.setRole(updatedUser.getRole());

        CustomUser saved = customUserService.updateUser(existing);
        return ResponseEntity.ok(new ProfileDTO(saved));
    }

    //elimina profilo
    @DeleteMapping("/{email}")
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    public ResponseEntity<?> deleteProfile(@PathVariable String email) {
        Optional<CustomUser> existingOpt = customUserRepository.findByEmail(email);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        customUserRepository.deleteById(email);
        return ResponseEntity.ok("User deleted successfully");
    }

    //per il lavoro assegnato oggi
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @GetMapping("/assigned-today")
    public ResponseEntity<?> getAssignedToday(@RequestParam String email) {
        List<AssignedTaskDTO> tasks = processStepExecutionService.getAssignedTasksToday(email);
        //System.out.println(tasks);
        return ResponseEntity.ok(tasks);
    }

    //per tutti i lavori assegnati
    @PreAuthorize("hasAnyRole('MANAGER', 'EMPLOYEE', 'ACCOUNTANT')")
    @GetMapping("/assigned")
    public ResponseEntity<?> getAssigned(@RequestParam String email) {
        List<AssignedTaskDTO> tasks = processStepExecutionService.getAssignedTasks(email);
        //System.out.println(tasks);
        return ResponseEntity.ok(tasks);
    }



}

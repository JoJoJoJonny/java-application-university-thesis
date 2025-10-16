package server_group.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import server_group.dto.LoginRequestDTO;
import server_group.dto.LoginResponseDTO;
import server_group.dto.RegisterRequestDTO;
import server_group.model.CustomUserDetails;
import server_group.security.JwtUtil;
import server_group.service.CustomUserService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    // tolto autowired per usare constructor injection
    private final CustomUserService customUserService;

    public AuthController(CustomUserService customUserService) {
        this.customUserService = customUserService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            LoginResponseDTO response = customUserService.loginUser(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password!");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO request) {
        boolean success = customUserService.registerUser(request);
        if (success) {
            return ResponseEntity.ok("User registered successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already in use!");
        }
    }
}


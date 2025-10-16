package server_group.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import server_group.dto.LoginRequestDTO;
import server_group.dto.LoginResponseDTO;
import server_group.dto.RegisterRequestDTO;
import server_group.model.CustomUser;
import server_group.model.CustomUserDetails;
import server_group.repository.CustomUserRepository;
import server_group.security.JwtUtil;
import server_group.security.Role;

import java.util.List;

@Service
public class CustomUserService {

    private final CustomUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public CustomUserService(AuthenticationManager authenticationManager, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, CustomUserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public List<CustomUser> getUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    public CustomUser findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public LoginResponseDTO loginUser(LoginRequestDTO request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        return new LoginResponseDTO(
                userDetails.getUsername(),
                userDetails.getUser().getRole().name(),
                token
        );
    }

    public boolean registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return false; // email già esistente
        }

        CustomUser user = new CustomUser();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // hashing
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        userRepository.save(user);
        return true;
    }

    // aggiornamento utente
    public CustomUser updateUser(CustomUser user) {
        return userRepository.save(user);
    }

    // eliminazione utente
    public void deleteUserByEmail(String email) {
        userRepository.deleteById(email);
    }
}

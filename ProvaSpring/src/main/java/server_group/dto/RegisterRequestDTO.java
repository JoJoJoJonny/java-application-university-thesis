package server_group.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import server_group.security.Role;

public class RegisterRequestDTO {

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotBlank
    @Email(message = "Insert a valid email!")
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    @Pattern(regexp = "^[0-9]{7,12}$", message = "Phone number must contain only digits (7-12 digits)")
    private String phone;

    @NotBlank
    private Role role;

    public RegisterRequestDTO(String name, String surname, String email, String password, String phone, Role role) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

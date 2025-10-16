package server_group.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import server_group.security.Role;


@Entity
public class CustomUser {
    @Id
    @Email(message = "Email must be a well-formed email address")
    private String email;

    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$&*.])(?=.*[0-9])(?=.*[a-z]).{8,}$", message = "Password must be at least 8 characters long and contain at least one digit, one lowercase, one uppercase, and one special character")

    private String password;

    private String name;
    private String surname;
    @Pattern(regexp = "^[0-9]{7,12}$", message = "Phone number must contain only digits (7-12 digits)")
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}

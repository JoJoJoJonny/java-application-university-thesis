package server_group.dto;

import server_group.model.CustomUser;
import server_group.security.Role;

public class ProfileDTO {
    private String email;
    private String name;
    private String surname;
    private String phone;
    private String role;

    public ProfileDTO(CustomUser user) {
        this.email = user.getEmail();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.phone = user.getPhone();
        //per evitare NullPointerException
        this.role = (user.getRole() != null) ? user.getRole().name() : Role.EMPLOYEE.name();  // Usa un valore di default se il ruolo è null
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

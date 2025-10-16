package client_group.model;

import client_group.dto.ProfileDTO;

public class UserModel extends ProfileDTO {
    private String email;
    private String name;
    private String surname;
    private String phone;
    private String role; // aggiunto campo esplicito

    public UserModel(String email, String name, String surname, String phone, String role) {
        this.email = email;
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.role = (role != null) ? role : "UNKNOWN"; // fallback sicuro
    }

    // getter
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getPhone() { return phone; }
    @Override
    public String getRole() { return role; }  // override esplicito

    // setter per il ruolo se serve
    public void setRole(String role) {
        this.role = (role != null) ? role : "UNKNOWN";
    }
}

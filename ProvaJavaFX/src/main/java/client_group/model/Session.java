package client_group.model;

import client_group.dto.ProfileDTO;

public class Session {
    private static Session instance;
    private ProfileDTO currentUser;
    private String token;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setCurrentUser(ProfileDTO user) {
        this.currentUser = user;
    }

    public ProfileDTO getCurrentUser() {
        return currentUser;
    }

    public void clear() {
        currentUser = null;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class User extends ProfileDTO {
        public User(String mail, String manager) {
        }
    }
}

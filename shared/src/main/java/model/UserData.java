package model;

public class UserData {
    public String getUsername() {
        return null;
    }

    public record UserData(String username, String password, String email) {
    }
}

package dataaccess;

public class GameDAO {
    private static Map<String, UserData> users = new HashMap<>();
    private static UserDAO instance = new UserDAO();

    public static UserDAO getInstance() {
        return instance;
    }

    public void clearUsers() {
        users.clear();
    }
}

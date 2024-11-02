package service;

import dataaccess.AuthTokenDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import dataaccess.DataAccessException;

import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final UserDAO userDAO;
    private final AuthTokenDAO authTokenDAO;

    public UserService(UserDAO userDAO, AuthTokenDAO authTokenDAO) {
        this.userDAO = userDAO;
        this.authTokenDAO = authTokenDAO;
    }

    public AuthData register(UserData user) throws DataAccessException {

        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        UserData userWithHashedPassword = new UserData(user.username(), hashedPassword, user.email());


        userDAO.insertUser(userWithHashedPassword);


        String authToken = UUID.randomUUID().toString();
        authTokenDAO.createAuth(new AuthData(authToken, user.username()));

        return new AuthData(authToken, user.username());
    }

    public UserData getUser(String username) throws DataAccessException {
        return userDAO.getUser(username);
    }

    public AuthData login(UserData user) throws DataAccessException {
        UserData foundUser = userDAO.getUser(user.username());


        if (foundUser != null && BCrypt.checkpw(user.password(), foundUser.password())) {
            String authToken = UUID.randomUUID().toString();
            authTokenDAO.createAuth(new AuthData(authToken, user.username()));
            return new AuthData(authToken, user.username());
        } else {
            throw new DataAccessException("Invalid credentials");
        }
    }
}

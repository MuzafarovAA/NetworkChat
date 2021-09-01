package ru.gb.java2.chat.server.chat.auth;

import java.sql.*;
import java.util.Set;

public class AuthService {

    public static final String JDBC_URL = "jdbc:sqlite:userlist.db";
    private Connection connection;


    public String getUsernameByLoginAndPassword(String login, String password) {

        String username = null;

        try {
            PreparedStatement statement = connection.prepareStatement("SELECT username FROM user WHERE login = ? AND password = ?");
            statement.setString(1, login);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet == null) {
                return null;
            }
            username = resultSet.getString("username");
            resultSet.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return username;
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection(JDBC_URL);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}

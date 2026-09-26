package com.ptms.app.util;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    private static final String dbUrl =  dotenv.get("DB_URL");
    private static final String username =  dotenv.get("DB_USERNAME");
    private static final String password =  dotenv.get("DB_PASSWORD");
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, username, password);
    }

    public static void main(String[] args) {

        try {
            Connection connection = DBConnection.getConnection();

            System.out.println("Database connected successfully!");

            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        }


}

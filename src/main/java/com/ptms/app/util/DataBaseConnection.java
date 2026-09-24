package com.ptms.app.util;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {
    private static final String dbUrl = "jdbc:mysql://localhost:3306/";
    private static final String username = "";
    private static final String password = "";
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, username, password);
    }
    /*
    public static void main(String[] args) {

        try {
            Connection connection = DataBaseConnection.getConnection();

            System.out.println("Database connected successfully!");

            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        }

     */

}

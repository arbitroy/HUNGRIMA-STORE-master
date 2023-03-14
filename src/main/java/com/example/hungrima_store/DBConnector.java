package com.example.hungrima_store;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DBConnector {
    static Connection connection = null;
    private static boolean hasData = false;

    public Connection getConnection() throws SQLException, IOException {
        // Load the SQLite JDBC driver
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load SQLite JDBC driver.");
            e.printStackTrace();
        }
        // Connect to the in-memory database
        try {
            connection = DriverManager.getConnection("jdbc:sqlite::resource:Hungrima.db");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the in-memory SQLite database.");
            e.printStackTrace();

        }
        initialise();

        return connection;
    }
    private void initialise() throws SQLException, IOException {
        if (!hasData){
            hasData = true;
            Statement state = connection.createStatement();
            ResultSet res = state.executeQuery("SELECT name FROM sqlite_master WHERE type ='table' and name = 'users'");
            if (!res.next()){
                System.out.println("initialising");
                //Initialize the script runner
                ScriptRunner sr = new ScriptRunner(connection);

                try (InputStream in = getClass().getResourceAsStream("/schema.sql");
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                    // Use resource
                    sr.runScript(reader);
                }
            }
        }
    }
}

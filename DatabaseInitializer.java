package bikeconsultantapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    public static void initialize() {
        System.out.println("Initializing Database...");
        try (InputStream input = DatabaseInitializer.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (input == null) {
                System.err.println("schema.sql not found!");
                return;
            }

            String sql = new BufferedReader(new InputStreamReader(input))
                    .lines().collect(Collectors.joining("\n"));

            String[] statements = sql.split(";");

            try (Connection con = DBConnection.getConnection();
                    Statement stmt = con.createStatement()) {

                for (String command : statements) {
                    if (command.trim().isEmpty())
                        continue;
                    try {
                        System.out.println("Executing: " + command.trim());
                        stmt.execute(command);
                    } catch (SQLException e) {
                        System.err.println("Error executing command: " + command);
                        e.printStackTrace();
                    }
                }
                System.out.println("Database Initialized.");
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        initialize();
    }
}

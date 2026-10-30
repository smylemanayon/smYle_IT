
package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public final class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:voting.db";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {

        Connection connection = DriverManager.getConnection(URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    public static void initialize() {

        try (Connection con = getConnection();
             Statement stmt = con.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    roles TEXT NOT NULL,
                    status TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS candidates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    party TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS votes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    voter_id INTEGER UNIQUE NOT NULL,
                    candidate_id INTEGER NOT NULL,
                    FOREIGN KEY(voter_id) REFERENCES users(id),
                    FOREIGN KEY(candidate_id) REFERENCES candidates(id)
                )
            """);

            // Default administrator account
            stmt.executeUpdate("""
    INSERT OR IGNORE INTO users
    (username, password, roles, status)
    VALUES ('admin', 'admin123', 'ADMIN', 'APPROVED')
""");

            // Default candidates
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM candidates")) {

                if (rs.next() && rs.getInt(1) == 0) {

                    stmt.executeUpdate("""
                        INSERT INTO candidates (name, party)
                        VALUES
                        ('Candidate A', 'Party Alpha'),
                        ('Candidate B', 'Party Beta'),
                        ('Candidate C', 'Party Gamma')
                    """);
                }
            }

            System.out.println("Database initialized successfully!");

        } catch (SQLException e) {

            System.out.println("Database error: " + e.getMessage());
        }
    }
}


package com.scc.dao;

import com.scc.model.User;
import com.scc.model.Candidate;
import com.scc.model.CandidateResult;

import java.sql.*;
import java.util.*;

public class UserDao {

    // REGISTER VOTER
    public boolean register(String username, String password) {

        String sql = """
            INSERT INTO users (username, password, roles, status)
            VALUES (?, ?, 'STUDENT', 'PENDING')
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ps.executeUpdate();

            return true;

        } catch (SQLException e) {

            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    // LOGIN
    public Optional<User> login(String username, String password) {

        String sql = """
            SELECT * FROM users
            WHERE username = ? AND password = ?
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            rs.getString("status")
                    ));
                }
            }

        } catch (SQLException e) {

            System.out.println("Login error: " + e.getMessage());
        }

        return Optional.empty();
    }

    // DISPLAY ALL USERS
    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM users ORDER BY id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("roles"),
                        rs.getString("status")
                ));
            }

        } catch (SQLException e) {

            System.out.println("Error: " + e.getMessage());
        }

        return users;
    }

    // FIND USER
    public Optional<User> findById(int id) {

        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("roles"),
                            rs.getString("status")
                    ));
                }
            }

        } catch (SQLException e) {

            System.out.println("Error: " + e.getMessage());
        }

        return Optional.empty();
    }

    // UPDATE USER ROLE AND STATUS
    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus) {

        String sql = """
            UPDATE users
            SET roles = ?, status = ?
            WHERE id = ?
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newRole.toUpperCase());
            ps.setString(2, newStatus.toUpperCase());
            ps.setInt(3, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Update failed: " + e.getMessage());
            return false;
        }
    }

    // GET CANDIDATES
    public List<Candidate> getCandidates() {

        List<Candidate> candidates = new ArrayList<>();

        String sql = "SELECT * FROM candidates ORDER BY id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                candidates.add(new Candidate(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("party")
                ));
            }

        } catch (SQLException e) {

            System.out.println("Error: " + e.getMessage());
        }

        return candidates;
    }

    // CHECK IF VOTER ALREADY VOTED
    public boolean hasVoted(int voterId) {

        String sql = "SELECT 1 FROM votes WHERE voter_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, voterId);

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next();
            }

        } catch (SQLException e) {

            System.out.println("Error: " + e.getMessage());
            return true;
        }
    }

    // CAST VOTE
    public boolean castVote(int voterId, int candidateId) {

        String sql = """
            INSERT INTO votes (voter_id, candidate_id)
            SELECT ?, ?
            WHERE EXISTS (
                SELECT 1 FROM users
                WHERE id = ?
                AND roles IN ('STUDENT', 'TEACHER')
                AND status = 'APPROVED'
            )
            AND EXISTS (
                SELECT 1 FROM candidates WHERE id = ?
            )
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, voterId);
            ps.setInt(2, candidateId);
            ps.setInt(3, voterId);
            ps.setInt(4, candidateId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            System.out.println("Vote failed: " + e.getMessage());
            return false;
        }
    }

    // AUTOMATED VOTE COUNTING
    public List<CandidateResult> getResults() {

        List<CandidateResult> results = new ArrayList<>();

        String sql = """
            SELECT c.id, c.name, c.party,
                   COUNT(v.id) AS total_votes
            FROM candidates c
            LEFT JOIN votes v
            ON c.id = v.candidate_id
            GROUP BY c.id, c.name, c.party
            ORDER BY total_votes DESC, c.id ASC
        """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                results.add(new CandidateResult(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("party"),
                        rs.getInt("total_votes")
                ));
            }

        } catch (SQLException e) {

            System.out.println("Counting error: " + e.getMessage());
        }

        return results;
    }
}

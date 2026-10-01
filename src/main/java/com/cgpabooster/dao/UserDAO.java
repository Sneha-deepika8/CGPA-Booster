package com.cgpabooster.dao;

import com.cgpabooster.model.User;
import com.cgpabooster.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data Access Object for the "users" table.
 * All database interaction for users goes through this class.
 */
public class UserDAO {

    /**
     * Checks if an email is already registered.
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Inserts a new user. The password passed in must already be hashed.
     */
    public boolean registerUser(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password, college, department) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getCollege());
            ps.setString(5, user.getDepartment());
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Finds a user by email. Returns null if not found.
     * Used during login to fetch the stored password hash for comparison.
     */
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, full_name, email, password, college, department, created_at " +
                     "FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, full_name, email, password, college, department, created_at " +
                     "FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /**
     * Updates the editable profile fields for a user.
     */
    public boolean updateProfile(int userId, String fullName, String college, String department) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, college = ?, department = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, college);
            ps.setString(3, department);
            ps.setInt(4, userId);
            return ps.executeUpdate() == 1;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("college"),
                rs.getString("department"),
                rs.getTimestamp("created_at")
        );
    }
}

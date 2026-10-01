package com.cgpabooster.dao;

import com.cgpabooster.model.Resource;
import com.cgpabooster.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the "resources" table.
 */
public class ResourceDAO {

    private static final String BASE_SELECT =
            "SELECT r.id, r.title, r.description, r.category, r.resource_url, " +
            "r.uploaded_by, u.full_name AS uploaded_by_name, r.created_at " +
            "FROM resources r JOIN users u ON r.uploaded_by = u.id ";

    public boolean addResource(Resource resource) throws SQLException {
        String sql = "INSERT INTO resources (title, description, category, resource_url, uploaded_by) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resource.getTitle());
            ps.setString(2, resource.getDescription());
            ps.setString(3, resource.getCategory());
            ps.setString(4, resource.getResourceUrl());
            ps.setInt(5, resource.getUploadedBy());
            return ps.executeUpdate() == 1;
        }
    }

    public List<Resource> getAllResources() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY r.created_at DESC";
        List<Resource> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Resource> getByCategory(String category) throws SQLException {
        String sql = BASE_SELECT + "WHERE r.category = ? ORDER BY r.created_at DESC";
        List<Resource> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Resource> search(String keyword) throws SQLException {
        String sql = BASE_SELECT + "WHERE r.title LIKE ? OR r.description LIKE ? ORDER BY r.created_at DESC";
        List<Resource> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Resource> getRecent(int limit) throws SQLException {
        String sql = BASE_SELECT + "ORDER BY r.created_at DESC LIMIT ?";
        List<Resource> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Resource getById(int id) throws SQLException {
        String sql = BASE_SELECT + "WHERE r.id = ?";
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

    public int countByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM resources WHERE uploaded_by = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Deletes a resource only if it belongs to the given user (authorization check).
     */
    public boolean deleteResource(int resourceId, int userId) throws SQLException {
        String sql = "DELETE FROM resources WHERE id = ? AND uploaded_by = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    private Resource mapRow(ResultSet rs) throws SQLException {
        Resource r = new Resource(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("category"),
                rs.getString("resource_url"),
                rs.getInt("uploaded_by"),
                rs.getTimestamp("created_at")
        );
        r.setUploadedByName(rs.getString("uploaded_by_name"));
        return r;
    }
}

package com.cgpabooster.dao;

import com.cgpabooster.model.Post;
import com.cgpabooster.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for the "posts" table.
 */
public class PostDAO {

    private static final String BASE_SELECT =
            "SELECT p.id, p.user_id, u.full_name AS user_name, p.title, p.content, p.created_at " +
            "FROM posts p JOIN users u ON p.user_id = u.id ";

    public boolean addPost(Post post) throws SQLException {
        String sql = "INSERT INTO posts (user_id, title, content) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, post.getUserId());
            ps.setString(2, post.getTitle());
            ps.setString(3, post.getContent());
            return ps.executeUpdate() == 1;
        }
    }

    public List<Post> getAllPosts() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY p.created_at DESC";
        List<Post> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int countByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM posts WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Deletes a post only if it belongs to the given user (authorization check).
     */
    public boolean deletePost(int postId, int userId) throws SQLException {
        String sql = "DELETE FROM posts WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    private Post mapRow(ResultSet rs) throws SQLException {
        Post p = new Post(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getTimestamp("created_at")
        );
        p.setUserName(rs.getString("user_name"));
        return p;
    }
}

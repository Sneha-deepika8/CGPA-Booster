package com.cgpabooster.controller;

import com.cgpabooster.dao.PostDAO;
import com.cgpabooster.model.Post;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles community post actions as a small JSON API consumed by
 * community.html + js/script.js.
 *
 * GET  /posts               -> list all posts
 * POST /posts (action=add)    -> create a post
 * POST /posts (action=delete) -> delete a post (owner only)
 */
@WebServlet("/posts")
public class PostServlet extends HttpServlet {

    private final PostDAO postDAO = new PostDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isLoggedIn(req)) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to continue.");
            return;
        }

        try {
            List<Post> posts = postDAO.getAllPosts();
            sendJsonPosts(resp, posts, currentUserId(req));
        } catch (SQLException e) {
            getServletContext().log("Database error in PostServlet (GET)", e);
            sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "A server error occurred.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isLoggedIn(req)) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to continue.");
            return;
        }

        String action = req.getParameter("action");
        int userId = currentUserId(req);

        try {
            if ("delete".equals(action)) {
                int postId = Integer.parseInt(req.getParameter("id"));
                boolean deleted = postDAO.deletePost(postId, userId);
                if (deleted) {
                    sendJsonSuccess(resp, "Post deleted successfully.");
                } else {
                    sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN, "You can only delete your own posts.");
                }
                return;
            }

            // default action = add
            String title = trim(req.getParameter("title"));
            String content = trim(req.getParameter("content"));

            if (isEmpty(title) || isEmpty(content)) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Title and content are required.");
                return;
            }

            Post post = new Post();
            post.setUserId(userId);
            post.setTitle(title);
            post.setContent(content);

            boolean added = postDAO.addPost(post);
            if (added) {
                sendJsonSuccess(resp, "Post created successfully.");
            } else {
                sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not create post.");
            }

        } catch (NumberFormatException e) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid post id.");
        } catch (SQLException e) {
            getServletContext().log("Database error in PostServlet (POST)", e);
            sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "A server error occurred.");
        }
    }

    // ---------- Helpers ----------

    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute("userId") != null;
    }

    private int currentUserId(HttpServletRequest req) {
        return (int) req.getSession(false).getAttribute("userId");
    }

    private void sendJsonPosts(HttpServletResponse resp, List<Post> posts, int currentUserId) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        StringBuilder json = new StringBuilder("{\"success\":true,\"posts\":[");
        for (int i = 0; i < posts.size(); i++) {
            Post p = posts.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"id\":").append(p.getId()).append(",")
                .append("\"userId\":").append(p.getUserId()).append(",")
                .append("\"userName\":\"").append(esc(p.getUserName())).append("\",")
                .append("\"title\":\"").append(esc(p.getTitle())).append("\",")
                .append("\"content\":\"").append(esc(p.getContent())).append("\",")
                .append("\"isOwner\":").append(p.getUserId() == currentUserId).append(",")
                .append("\"createdAt\":\"").append(p.getCreatedAt()).append("\"")
                .append("}");
        }
        json.append("]}");
        try (PrintWriter out = resp.getWriter()) {
            out.print(json);
        }
    }

    private void sendJsonSuccess(HttpServletResponse resp, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"success\":true,\"message\":\"" + esc(message) + "\"}");
        }
    }

    private void sendJsonError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print("{\"success\":false,\"message\":\"" + esc(message) + "\"}");
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}

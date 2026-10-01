package com.cgpabooster.controller;

import com.cgpabooster.dao.PostDAO;
import com.cgpabooster.dao.ResourceDAO;
import com.cgpabooster.dao.UserDAO;
import com.cgpabooster.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Handles profile view/update and supplies the stats used by the dashboard,
 * as a small JSON API consumed by profile.html, dashboard.html + js/script.js.
 *
 * GET  /profile               -> current user's profile + resource/post counts
 * POST /profile (action=update) -> update full name / college / department
 */
@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final PostDAO postDAO = new PostDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isLoggedIn(req)) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to continue.");
            return;
        }

        int userId = currentUserId(req);

        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                sendJsonError(resp, HttpServletResponse.SC_NOT_FOUND, "User not found.");
                return;
            }

            int resourceCount = resourceDAO.countByUser(userId);
            int postCount = postDAO.countByUser(userId);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            try (PrintWriter out = resp.getWriter()) {
                out.print("{\"success\":true,"
                        + "\"fullName\":\"" + esc(user.getFullName()) + "\","
                        + "\"email\":\"" + esc(user.getEmail()) + "\","
                        + "\"college\":\"" + esc(user.getCollege()) + "\","
                        + "\"department\":\"" + esc(user.getDepartment()) + "\","
                        + "\"createdAt\":\"" + user.getCreatedAt() + "\","
                        + "\"resourceCount\":" + resourceCount + ","
                        + "\"postCount\":" + postCount
                        + "}");
            }

        } catch (SQLException e) {
            getServletContext().log("Database error in ProfileServlet (GET)", e);
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

        int userId = currentUserId(req);
        String fullName = trim(req.getParameter("fullName"));
        String college = trim(req.getParameter("college"));
        String department = trim(req.getParameter("department"));

        if (isEmpty(fullName) || isEmpty(college) || isEmpty(department)) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "All fields are required.");
            return;
        }

        try {
            boolean updated = userDAO.updateProfile(userId, fullName, college, department);
            if (updated) {
                // Keep the session name in sync with the new full name
                HttpSession session = req.getSession(false);
                session.setAttribute("userName", fullName);
                sendJsonSuccess(resp, "Profile updated successfully.");
            } else {
                sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not update profile.");
            }
        } catch (SQLException e) {
            getServletContext().log("Database error in ProfileServlet (POST)", e);
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

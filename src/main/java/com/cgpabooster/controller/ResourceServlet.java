package com.cgpabooster.controller;

import com.cgpabooster.dao.ResourceDAO;
import com.cgpabooster.model.Resource;
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
 * Handles all resource-related actions as a small JSON API consumed by
 * resources.html + js/script.js (Vanilla JS fetch calls).
 *
 * GET  /resources?action=list
 * GET  /resources?action=search&keyword=...
 * GET  /resources?action=filter&category=...
 * POST /resources (action=add)      -> add a new resource
 * POST /resources (action=delete)   -> delete a resource (owner only)
 */
@WebServlet("/resources")
public class ResourceServlet extends HttpServlet {

    private final ResourceDAO resourceDAO = new ResourceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isLoggedIn(req)) {
            sendJsonError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to continue.");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "list";

        try {
            List<Resource> resources;

            switch (action) {
                case "search":
                    String keyword = req.getParameter("keyword");
                    resources = resourceDAO.search(keyword == null ? "" : keyword);
                    break;
                case "filter":
                    String category = req.getParameter("category");
                    resources = (category == null || category.isEmpty() || category.equals("All"))
                            ? resourceDAO.getAllResources()
                            : resourceDAO.getByCategory(category);
                    break;
                case "list":
                default:
                    resources = resourceDAO.getAllResources();
                    break;
            }

            sendJsonResources(resp, resources, currentUserId(req));

        } catch (SQLException e) {
            getServletContext().log("Database error in ResourceServlet (GET)", e);
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
                int resourceId = Integer.parseInt(req.getParameter("id"));
                boolean deleted = resourceDAO.deleteResource(resourceId, userId);
                if (deleted) {
                    sendJsonSuccess(resp, "Resource deleted successfully.");
                } else {
                    sendJsonError(resp, HttpServletResponse.SC_FORBIDDEN,
                            "You can only delete your own resources.");
                }
                return;
            }

            // default action = add
            String title = trim(req.getParameter("title"));
            String description = trim(req.getParameter("description"));
            String category = trim(req.getParameter("category"));
            String resourceUrl = trim(req.getParameter("resourceUrl"));

            if (isEmpty(title) || isEmpty(category) || isEmpty(resourceUrl)) {
                sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST,
                        "Title, category and resource URL are required.");
                return;
            }

            Resource resource = new Resource();
            resource.setTitle(title);
            resource.setDescription(description);
            resource.setCategory(category);
            resource.setResourceUrl(resourceUrl);
            resource.setUploadedBy(userId);

            boolean added = resourceDAO.addResource(resource);
            if (added) {
                sendJsonSuccess(resp, "Resource added successfully.");
            } else {
                sendJsonError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not add resource.");
            }

        } catch (NumberFormatException e) {
            sendJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid resource id.");
        } catch (SQLException e) {
            getServletContext().log("Database error in ResourceServlet (POST)", e);
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

    private void sendJsonResources(HttpServletResponse resp, List<Resource> resources, int currentUserId)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        StringBuilder json = new StringBuilder("{\"success\":true,\"resources\":[");
        for (int i = 0; i < resources.size(); i++) {
            Resource r = resources.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"id\":").append(r.getId()).append(",")
                .append("\"title\":\"").append(esc(r.getTitle())).append("\",")
                .append("\"description\":\"").append(esc(r.getDescription())).append("\",")
                .append("\"category\":\"").append(esc(r.getCategory())).append("\",")
                .append("\"resourceUrl\":\"").append(esc(r.getResourceUrl())).append("\",")
                .append("\"uploadedByName\":\"").append(esc(r.getUploadedByName())).append("\",")
                .append("\"uploadedBy\":").append(r.getUploadedBy()).append(",")
                .append("\"isOwner\":").append(r.getUploadedBy() == currentUserId).append(",")
                .append("\"createdAt\":\"").append(r.getCreatedAt()).append("\"")
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

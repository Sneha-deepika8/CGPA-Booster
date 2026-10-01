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
 * Small helper endpoint just for the dashboard's "Recently added resources" widget.
 * GET /dashboard-data
 */
@WebServlet("/dashboard-data")
public class DashboardServlet extends HttpServlet {

    private final ResourceDAO resourceDAO = new ResourceDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json");
            resp.getWriter().print("{\"success\":false,\"message\":\"Please log in to continue.\"}");
            return;
        }

        try {
            List<Resource> recent = resourceDAO.getRecent(5);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            StringBuilder json = new StringBuilder("{\"success\":true,\"recentResources\":[");
            for (int i = 0; i < recent.size(); i++) {
                Resource r = recent.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                    .append("\"title\":\"").append(esc(r.getTitle())).append("\",")
                    .append("\"category\":\"").append(esc(r.getCategory())).append("\",")
                    .append("\"uploadedByName\":\"").append(esc(r.getUploadedByName())).append("\"")
                    .append("}");
            }
            json.append("]}");

            try (PrintWriter out = resp.getWriter()) {
                out.print(json);
            }

        } catch (SQLException e) {
            getServletContext().log("Database error in DashboardServlet", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().print("{\"success\":false,\"message\":\"A server error occurred.\"}");
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}

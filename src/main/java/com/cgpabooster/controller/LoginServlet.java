package com.cgpabooster.controller;

import com.cgpabooster.dao.UserDAO;
import com.cgpabooster.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (isEmpty(email) || isEmpty(password)) {
            redirectWithError(resp, "Email and password are required.");
            return;
        }

        try {
            User user = userDAO.findByEmail(email.trim());

            if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
                redirectWithError(resp, "Invalid email or password.");
                return;
            }

            // Create session and store logged-in user's details
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getFullName());
            session.setAttribute("userEmail", user.getEmail());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            resp.sendRedirect("dashboard.html");

        } catch (SQLException e) {
            getServletContext().log("Database error during login", e);
            redirectWithError(resp, "A server error occurred. Please try again later.");
        }
    }

    private void redirectWithError(HttpServletResponse resp, String message) throws IOException {
        resp.sendRedirect("login.html?error=" + URLEncoder.encode(message, "UTF-8"));
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}

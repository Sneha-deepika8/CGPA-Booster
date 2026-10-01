package com.cgpabooster.controller;

import com.cgpabooster.dao.UserDAO;
import com.cgpabooster.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String fullName = trim(req.getParameter("fullName"));
        String email = trim(req.getParameter("email"));
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String college = trim(req.getParameter("college"));
        String department = trim(req.getParameter("department"));

        // ---- Validation ----
        if (isEmpty(fullName) || isEmpty(email) || isEmpty(password)
                || isEmpty(confirmPassword) || isEmpty(college) || isEmpty(department)) {
            redirectWithError(req, resp, "All fields are required.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            redirectWithError(req, resp, "Please enter a valid email address.");
            return;
        }

        if (password.length() < 6) {
            redirectWithError(req, resp, "Password must be at least 6 characters long.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            redirectWithError(req, resp, "Passwords do not match.");
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                redirectWithError(req, resp, "An account with this email already exists.");
                return;
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));

            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(hashedPassword);
            user.setCollege(college);
            user.setDepartment(department);

            boolean success = userDAO.registerUser(user);

            if (success) {
                resp.sendRedirect("login.html?registered=true");
            } else {
                redirectWithError(req, resp, "Registration failed. Please try again.");
            }

        } catch (SQLException e) {
            // Never expose raw SQL errors to the user.
            getServletContext().log("Database error during registration", e);
            redirectWithError(req, resp, "A server error occurred. Please try again later.");
        }
    }

    private void redirectWithError(HttpServletRequest req, HttpServletResponse resp, String message)
            throws IOException {
        resp.sendRedirect("register.html?error=" + java.net.URLEncoder.encode(message, "UTF-8"));
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}

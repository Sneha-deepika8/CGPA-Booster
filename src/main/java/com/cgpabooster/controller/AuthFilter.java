package com.cgpabooster.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Protects dashboard.html, resources.html, community.html and profile.html
 * (and their related servlet actions) from unauthenticated access.
 *
 * Since the frontend is plain static HTML, protection is enforced here at
 * the server level by checking for a valid session before the page is served.
 */
@WebFilter(urlPatterns = {"/dashboard.html", "/resources.html", "/community.html", "/profile.html"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("userId") != null);

        if (loggedIn) {
            chain.doFilter(request, response);
        } else {
            resp.sendRedirect(req.getContextPath() + "/login.html?error=Please+log+in+to+continue.");
        }
    }
}

/* =========================================================
   CGPA Booster - Vanilla JavaScript
   Handles: mobile nav, form validation, and AJAX calls to servlets
   ========================================================= */

// ---------------- Mobile Navbar ----------------
document.addEventListener('DOMContentLoaded', function () {
    const hamburger = document.querySelector('.hamburger');
    const navLinks = document.querySelector('.nav-links');
    if (hamburger && navLinks) {
        hamburger.addEventListener('click', function () {
            navLinks.classList.toggle('mobile-open');
        });
    }

    showUrlMessages();
    initRegisterForm();
    initLoginForm();
});

// Reads ?error= or ?registered= from the URL and shows a banner
function showUrlMessages() {
    const params = new URLSearchParams(window.location.search);
    const errorBox = document.getElementById('alertBox');
    if (!errorBox) return;

    if (params.get('error')) {
        errorBox.textContent = params.get('error');
        errorBox.className = 'alert alert-error';
        errorBox.style.display = 'block';
    } else if (params.get('registered') === 'true') {
        errorBox.textContent = 'Registration successful! Please log in.';
        errorBox.className = 'alert alert-success';
        errorBox.style.display = 'block';
    }
}

// ---------------- Registration Validation ----------------
function initRegisterForm() {
    const form = document.getElementById('registerForm');
    if (!form) return;

    form.addEventListener('submit', function (e) {
        let valid = true;

        valid = requireField('fullName', 'Full name is required.') && valid;
        valid = validateEmail('email') && valid;
        valid = validatePasswordLength('password') && valid;
        valid = validatePasswordMatch('password', 'confirmPassword') && valid;
        valid = requireField('college', 'College is required.') && valid;
        valid = requireField('department', 'Department is required.') && valid;

        if (!valid) {
            e.preventDefault();
        }
    });

    document.getElementById('confirmPassword')?.addEventListener('input', function () {
        validatePasswordMatch('password', 'confirmPassword');
    });
}

// ---------------- Login Validation ----------------
function initLoginForm() {
    const form = document.getElementById('loginForm');
    if (!form) return;

    form.addEventListener('submit', function (e) {
        let valid = true;
        valid = validateEmail('email') && valid;
        valid = requireField('password', 'Password is required.') && valid;
        if (!valid) e.preventDefault();
    });
}

// ---------------- Reusable Validation Helpers ----------------
function requireField(id, message) {
    const field = document.getElementById(id);
    const errorEl = document.getElementById(id + 'Error');
    if (!field) return true;
    if (!field.value.trim()) {
        showFieldError(errorEl, message);
        return false;
    }
    hideFieldError(errorEl);
    return true;
}

function validateEmail(id) {
    const field = document.getElementById(id);
    const errorEl = document.getElementById(id + 'Error');
    if (!field) return true;
    const pattern = /^[\w.+-]+@[\w-]+\.[a-zA-Z]{2,}$/;
    if (!field.value.trim()) {
        showFieldError(errorEl, 'Email is required.');
        return false;
    }
    if (!pattern.test(field.value.trim())) {
        showFieldError(errorEl, 'Please enter a valid email address.');
        return false;
    }
    hideFieldError(errorEl);
    return true;
}

function validatePasswordLength(id) {
    const field = document.getElementById(id);
    const errorEl = document.getElementById(id + 'Error');
    if (!field) return true;
    if (field.value.length < 6) {
        showFieldError(errorEl, 'Password must be at least 6 characters.');
        return false;
    }
    hideFieldError(errorEl);
    return true;
}

function validatePasswordMatch(pwId, confirmId) {
    const pw = document.getElementById(pwId);
    const confirm = document.getElementById(confirmId);
    const errorEl = document.getElementById(confirmId + 'Error');
    if (!pw || !confirm) return true;
    if (pw.value !== confirm.value) {
        showFieldError(errorEl, 'Passwords do not match.');
        return false;
    }
    hideFieldError(errorEl);
    return true;
}

function showFieldError(el, message) {
    if (!el) return;
    el.textContent = message;
    el.style.display = 'block';
}

function hideFieldError(el) {
    if (!el) return;
    el.style.display = 'none';
}

// ---------------- Generic confirm-before-delete ----------------
function confirmDelete(message) {
    return window.confirm(message || 'Are you sure you want to delete this? This cannot be undone.');
}

// ---------------- Small fetch helper ----------------
async function apiRequest(url, options) {
    try {
        const res = await fetch(url, options);
        const data = await res.json();
        return { ok: res.ok, data: data };
    } catch (err) {
        return { ok: false, data: { success: false, message: 'Network error. Please try again.' } };
    }
}

function toFormBody(obj) {
    return Object.keys(obj)
        .map(k => encodeURIComponent(k) + '=' + encodeURIComponent(obj[k]))
        .join('&');
}

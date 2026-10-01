# CGPA Booster

A full-stack Ed-Tech web application built with **HTML5 + CSS3 + Vanilla JavaScript**,
**Java (Jakarta Servlets) + JDBC**, **MySQL**, deployed on **Apache Tomcat**, built with **Maven**.

No React, Angular, Vue, Spring, Spring Boot, Hibernate, JPA, or Lombok is used anywhere.

---

## 1. Project Architecture

```
Browser (HTML/CSS/JS)
      |
      v
  Servlet (controller)
      |
      v
    DAO (data access object)
      |
      v
   JDBC (PreparedStatement)
      |
      v
    MySQL Database
```

- **Model** classes (`User`, `Resource`, `Post`) — plain Java objects that mirror database rows.
- **DAO** classes (`UserDAO`, `ResourceDAO`, `PostDAO`) — the only classes that talk to the database.
- **Controller** classes (Servlets) — handle HTTP requests, validate input, call DAOs, return
  either a redirect (login/register) or a small hand-built JSON response (resources/posts/profile).
- **util.DBConnection** — single place that opens a JDBC connection to MySQL.
- **webapp/** — static HTML pages styled with `css/style.css` and driven by `js/script.js`,
  which calls the servlets using `fetch()`.

### Folder structure

```
CGPA-Booster/
├── pom.xml
├── database/
│   └── cgpa_booster.sql
└── src/main/
    ├── java/com/cgpabooster/
    │   ├── controller/   LoginServlet, RegisterServlet, LogoutServlet,
    │   │                 ResourceServlet, PostServlet, ProfileServlet,
    │   │                 DashboardServlet, AuthFilter
    │   ├── dao/          UserDAO, ResourceDAO, PostDAO
    │   ├── model/        User, Resource, Post
    │   └── util/         DBConnection
    └── webapp/
        ├── index.html, login.html, register.html
        ├── dashboard.html, resources.html, community.html, profile.html
        ├── css/style.css
        ├── js/script.js
        └── WEB-INF/web.xml
```

---

## 2. Prerequisites (Windows)

- **JDK 11+** — `java -version`
- **Apache Maven** — `mvn -version`
- **MySQL Server 8.x** — installed and running as a service
- **Apache Tomcat 10.x** (Tomcat 10 uses the `jakarta.servlet.*` namespace, which matches
  this project's dependency — do not use Tomcat 9, which uses `javax.servlet.*`)

---

## 3. MySQL Setup (Windows CMD)

Open **Command Prompt**.

```cmd
:: 1. Log in to MySQL
mysql -u root -p

:: 2. (inside mysql prompt) just verify server is up, then exit
exit;

:: 3. Run the provided SQL file to create the database and tables
mysql -u root -p < "C:\path\to\CGPA-Booster\database\cgpa_booster.sql"

:: 4. Log back in and verify
mysql -u root -p

:: 5. (inside mysql prompt) check the database and tables exist
SHOW DATABASES;
USE cgpa_booster;
SHOW TABLES;

:: 6. Check each table
SELECT * FROM users;
SELECT * FROM resources;
SELECT * FROM posts;

exit;
```

---

## 4. Configure the Database Connection

Open:

```
src/main/java/com/cgpabooster/util/DBConnection.java
```

Update the placeholder password:

```java
private static final String URL = "jdbc:mysql://localhost:3306/cgpa_booster?useSSL=false&serverTimezone=UTC";
private static final String USERNAME = "root";
private static final String PASSWORD = "YOUR_MYSQL_PASSWORD"; // <-- change this
```

---

## 5. Build with Maven (Windows CMD)

From the project root (where `pom.xml` is):

```cmd
mvn clean package
```

This produces:

```
target\cgpa-booster.war
```

---

## 6. Deploy to Tomcat (Windows)

```cmd
:: 1. Copy the WAR file into Tomcat's webapps folder
copy target\cgpa-booster.war "C:\apache-tomcat-10.1.x\webapps\"

:: 2. Start Tomcat
"C:\apache-tomcat-10.1.x\bin\startup.bat"

:: 3. Open the application in your browser
:: http://localhost:8080/cgpa-booster/
```

Tomcat will automatically unpack the WAR into `webapps\cgpa-booster\`. Wait a few seconds
after `startup.bat` for it to finish deploying, then open the URL above.

To stop Tomcat:

```cmd
"C:\apache-tomcat-10.1.x\bin\shutdown.bat"
```

---

## 7. Testing Steps

1. Open `http://localhost:8080/cgpa-booster/` — homepage should load with the hero section.
2. Click **Get Started** → fill the registration form → submit.
   - Try an invalid email, a short password, mismatched passwords — confirm validation messages.
   - Register successfully → you're redirected to the login page with a success banner.
3. Log in with the same credentials → redirected to the dashboard.
4. On **Resources**: add a resource, search for it, filter by category, then delete it
   (delete button should only appear on resources you own).
5. On **Community**: create a post, confirm it appears at the top, delete it.
6. On **Profile**: update your name/college/department, confirm the change is saved and
   reflected in the navbar.
7. Click **Logout** → confirm you're returned to the login page.
8. Try opening `http://localhost:8080/cgpa-booster/dashboard.html` directly in a private/incognito
   window (no session) — you should be redirected to the login page (this proves `AuthFilter` works).

---

## 8. How Everything Connects

- **HTML → Servlet**: `login.html`'s `<form action="login" method="POST">` submits directly to
  `LoginServlet` (mapped with `@WebServlet("/login")`). Pages that need live data
  (`dashboard.html`, `resources.html`, `community.html`, `profile.html`) instead use
  `fetch()` in `js/script.js` to call JSON endpoints like `GET /resources` or `POST /posts`.
- **Servlet → DAO**: each servlet creates a DAO instance (e.g. `new UserDAO()`) and calls
  a method like `userDAO.findByEmail(email)`. The servlet never writes SQL itself.
- **DAO → JDBC**: each DAO method opens a `Connection` via `DBConnection.getConnection()`,
  builds a `PreparedStatement` with `?` placeholders, sets parameters, and executes it.
  `try-with-resources` closes the connection/statement/result set automatically.
- **JDBC → MySQL**: the MySQL Connector/J driver translates the JDBC calls into the MySQL
  wire protocol and talks to the `cgpa_booster` database.
- **Login/session**: `LoginServlet` checks the password with `BCrypt.checkpw()`, and on
  success calls `req.getSession(true)` to create an `HttpSession`, storing `userId`,
  `userName`, `userEmail` as session attributes. Every protected servlet checks
  `session.getAttribute("userId")` before doing anything. `AuthFilter` does the same check
  for the static HTML pages themselves, redirecting to `login.html` if there's no session.
- **CRUD**: Create = `INSERT` (`addResource`, `addPost`, `registerUser`), Read = `SELECT`
  (`getAllResources`, `getAllPosts`, `findByEmail`), Update = `UPDATE` (`updateProfile`),
  Delete = `DELETE ... WHERE id = ? AND owner_id = ?` (the extra `AND` is the authorization
  check that stops a user deleting someone else's data).

---

## 9. Security Notes

- All SQL uses `PreparedStatement` — no string concatenation of user input into SQL.
- Passwords are hashed with **BCrypt** (via the `jbcrypt` Maven dependency,
  `org.mindrot:jbcrypt:0.4`) before being stored; plain-text passwords are never saved.
- Session-based authentication via `HttpSession`; `AuthFilter` blocks unauthenticated access
  to `dashboard.html`, `resources.html`, `community.html`, `profile.html`.
- Delete operations always check `WHERE ... AND owner_id = ?` so users can only delete their
  own resources/posts.
- Database credentials live only in `DBConnection.java` on the server — never in HTML/JS.
- Errors are logged server-side (`getServletContext().log(...)`) and only friendly messages
  are sent to the browser — no raw SQL exceptions are exposed.

---

## 10. Interview Explanation Notes

### 30-second explanation
"CGPA Booster is a full-stack Ed-Tech web app I built with core Java — Servlets, JDBC and
MySQL, no frameworks like Spring or Hibernate. Students can register, log in, share and
search study resources, and post in a community area. I used an MVC-style structure with
separate Model, DAO, and Servlet layers, and the frontend is plain HTML, CSS and JavaScript
talking to the backend through small JSON APIs."

### 1-minute explanation
"CGPA Booster is a student-focused Ed-Tech platform I built end-to-end to demonstrate core
Java web development without relying on frameworks like Spring Boot or Hibernate. The
frontend is HTML, CSS and vanilla JavaScript, which calls backend Servlets using fetch().
Each Servlet handles one responsibility — login, registration, resources, posts, or
profile — and delegates all database work to a DAO class. The DAO classes use JDBC with
PreparedStatements to talk to a MySQL database, which stores users, resources and posts
with proper foreign keys. Authentication uses HttpSession: once a user logs in, their ID is
stored in the session, and a servlet Filter protects pages like the dashboard from
unauthenticated access. Passwords are hashed with BCrypt rather than stored in plain text.
The whole thing is built with Maven and packaged as a WAR file for deployment on Apache
Tomcat."

### Architecture explanation
"I followed a layered, MVC-style architecture. The 'View' is static HTML/CSS/JS. The
'Controller' layer is my Servlets, which read request parameters, validate them, and decide
what to do. The 'Model' is both my plain Java classes (User, Resource, Post) and, more
practically, the MySQL database itself, accessed only through DAO classes. This keeps
concerns separated: if I wanted to swap MySQL for another database, I'd only need to change
the DAO layer, not the Servlets or the frontend."

### Login flow explanation
"The login form posts to LoginServlet. LoginServlet calls UserDAO.findByEmail() to fetch the
user's stored BCrypt hash, then uses BCrypt.checkpw() to compare it against the submitted
password — the plain password is never stored or compared directly. If it matches, I create
an HttpSession and store the user's ID, name and email as session attributes, then redirect
to the dashboard. If it doesn't match, I redirect back to the login page with a friendly
error message in the URL, which JavaScript reads and displays."

### Registration flow explanation
"RegisterServlet validates that all fields are filled, the email format is valid using a
regex, the password is at least 6 characters, and the two password fields match. It then
checks UserDAO.emailExists() to prevent duplicate accounts. If everything passes, I hash the
password with BCrypt and insert the new user with UserDAO.registerUser(), which uses a
PreparedStatement so there's no SQL injection risk."

### Resource CRUD explanation
"ResourceServlet exposes a small JSON API. GET with no action, or action=list, returns all
resources; action=search filters by keyword using SQL LIKE; action=filter filters by
category. POST with no action adds a new resource tied to the logged-in user's ID; POST with
action=delete deletes a resource, but only if the WHERE clause also matches the current
user's ID as the uploader — that's the authorization check that stops someone deleting
another student's resource."

### JDBC explanation
"JDBC is the standard Java API for connecting to relational databases. I use
DriverManager.getConnection() to open a connection, PreparedStatement to safely bind
parameters instead of concatenating strings, and ResultSet to read query results row by
row. I wrap all of this in try-with-resources so connections, statements and result sets are
always closed, even if an exception is thrown."

### Why Servlets?
"Servlets are the standard Jakarta EE way to handle HTTP requests in Java without a
framework like Spring. They gave me full control over request/response handling and
sessions, which was the point of this project — to show I understand the fundamentals
underneath frameworks like Spring MVC."

### Why JDBC?
"JDBC is the lowest-level, most fundamental way to talk to a relational database from Java.
Using it directly, instead of an ORM like Hibernate/JPA, meant I had to think explicitly
about SQL, connections, and resource management, which demonstrates a solid understanding
of how database access actually works."

### Why MySQL?
"MySQL is a widely used, free, open-source relational database with strong community
support and straightforward JDBC connectivity via MySQL Connector/J, which made it a
practical choice for a project focused on core Java skills."

### Why MVC?
"MVC keeps responsibilities separated: the HTML/CSS/JS is the View, Servlets are the
Controller, and the Model/DAO layer manages data. This makes the codebase easier to
navigate, test, and extend — for example, I could add new pages without touching database
code, or change a table's structure without touching the frontend."

### Challenges faced
"One challenge was protecting static HTML pages, since plain HTML has no server-side logic
of its own — I solved this with a Servlet Filter (AuthFilter) that intercepts requests to
protected pages and checks the session before Tomcat serves the file. Another challenge was
avoiding SQL injection while keeping the DAO code readable — PreparedStatement with named
placeholders solved that cleanly."

### How I solved them
"I used a Filter mapped to the exact protected URLs to add server-side authentication checks
in front of static HTML. For data operations from the frontend, I built small hand-written
JSON responses from the Servlets so vanilla JavaScript could update the page dynamically
without needing a template engine or extra dependencies."

### Possible future improvements
- Add pagination for resources and posts as data grows.
- Add file upload support for resources instead of only external URLs.
- Add likes/comments on community posts.
- Add email verification during registration.
- Add role-based access (e.g., moderators who can remove inappropriate posts).
- Introduce connection pooling (e.g., HikariCP) instead of opening a new connection per request.

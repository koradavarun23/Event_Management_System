package com.eventmgmt.servlet;

import com.eventmgmt.DatabaseManager;
import com.eventmgmt.model.Student;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * StudentServlet – handles all /api/students/* requests.
 *
 * Routes:
 *   GET  /api/students   → list all students
 *   POST /api/students   → add a new student  { name, email }
 */
public class StudentServlet extends HttpServlet {

    // -----------------------------------------------------------------------
    // GET /api/students
    // -----------------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsAndContentType(resp);
        try {
            List<Student> students = DatabaseManager.getInstance().getAllStudents();
            JSONArray arr = new JSONArray();
            for (Student s : students) {
                arr.put(studentToJson(s));
            }
            writeJson(resp, 200, arr.toString());
        } catch (Exception e) {
            writeJson(resp, 500, error("Server error: " + e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/students   body: { "name": "...", "email": "..." }
    // -----------------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsAndContentType(resp);
        try {
            JSONObject body = readBody(req);

            if (!body.has("name") || body.getString("name").trim().isEmpty()) {
                writeJson(resp, 400, error("'name' is required"));
                return;
            }
            if (!body.has("email") || body.getString("email").trim().isEmpty()) {
                writeJson(resp, 400, error("'email' is required"));
                return;
            }

            String name  = body.getString("name").trim();
            String email = body.getString("email").trim().toLowerCase();

            // Check if email already exists
            Student existing = DatabaseManager.getInstance().getStudentByEmail(email);
            if (existing != null) {
                writeJson(resp, 409, error("A student with this email already exists"));
                return;
            }

            int newId = DatabaseManager.getInstance().createStudent(name, email);
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("id",      newId);
            result.put("message", "Student added successfully");
            writeJson(resp, 201, result.toString());

        } catch (Exception e) {
            writeJson(resp, 500, error("Server error: " + e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------
    // OPTIONS – CORS pre-flight
    // -----------------------------------------------------------------------
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsAndContentType(resp);
        resp.setStatus(204);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private JSONObject studentToJson(Student s) {
        JSONObject obj = new JSONObject();
        obj.put("id",        s.getId());
        obj.put("name",      s.getName());
        obj.put("email",     s.getEmail());
        obj.put("createdAt", s.getCreatedAt());
        return obj;
    }

    private void setCorsAndContentType(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private String error(String msg) {
        return new JSONObject().put("error", msg).toString();
    }

    private JSONObject readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return new JSONObject(sb.toString());
    }

    private void writeJson(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
    }
}

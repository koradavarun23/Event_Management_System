package com.eventmgmt.servlet;

import com.eventmgmt.DatabaseManager;
import com.eventmgmt.model.Registration;
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
 * RegistrationServlet – handles all /api/registrations/* requests.
 *
 * Routes:
 *   POST   /api/registrations              → register a student for an event
 *                                            body: { studentName, email, eventId }
 *   GET    /api/registrations?eventId=X    → list participants for event X
 *   DELETE /api/registrations/{id}         → cancel a registration (soft delete)
 */
public class RegistrationServlet extends HttpServlet {

    // -----------------------------------------------------------------------
    // POST /api/registrations
    // body: { "studentName": "Alice", "email": "alice@uni.edu", "eventId": 3 }
    // -----------------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsAndContentType(resp);
        try {
            JSONObject body = readBody(req);

            // --- Validate input ---
            if (!body.has("studentName") || body.getString("studentName").trim().isEmpty()) {
                writeJson(resp, 400, error("'studentName' is required"));
                return;
            }
            if (!body.has("email") || body.getString("email").trim().isEmpty()) {
                writeJson(resp, 400, error("'email' is required"));
                return;
            }
            if (!body.has("eventId")) {
                writeJson(resp, 400, error("'eventId' is required"));
                return;
            }

            String studentName = body.getString("studentName").trim();
            String email       = body.getString("email").trim().toLowerCase();
            int    eventId     = body.getInt("eventId");

            // --- Upsert student: find by email or create new ---
            DatabaseManager db = DatabaseManager.getInstance();
            Student student = db.getStudentByEmail(email);
            int studentId;
            if (student == null) {
                studentId = db.createStudent(studentName, email);
            } else {
                studentId = student.getId();
            }

            // --- Check duplicate registration ---
            if (db.isStudentRegistered(studentId, eventId)) {
                writeJson(resp, 409, error("Student (" + email + ") is already registered for this event."));
                return;
            }

            // --- Check capacity before registering ---
            var event = db.getEventById(eventId);
            if (event == null) {
                writeJson(resp, 404, error("Event not found"));
                return;
            }
            int registered = db.countActiveRegistrations(eventId);
            if (registered >= event.getCapacity()) {
                writeJson(resp, 409, error("Event is at full capacity (" + event.getCapacity() + " seats)"));
                return;
            }

            // --- Create the registration ---
            int regId = db.createRegistration(studentId, eventId);

            JSONObject result = new JSONObject();
            result.put("success",        true);
            result.put("registrationId", regId);
            result.put("studentId",      studentId);
            result.put("eventId",        eventId);
            result.put("message",        "Registration successful");
            writeJson(resp, 201, result.toString());

        } catch (Exception e) {
            writeJson(resp, 500, error("Server error: " + e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------
    // GET /api/registrations  or  GET /api/registrations?eventId=X
    // -----------------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsAndContentType(resp);
        try {
            String eventIdParam = req.getParameter("eventId");
            List<Registration> registrations;

            if (eventIdParam != null && !eventIdParam.trim().isEmpty()) {
                int eventId = Integer.parseInt(eventIdParam.trim());
                registrations = DatabaseManager.getInstance().getRegistrationsByEvent(eventId);
            } else {
                registrations = DatabaseManager.getInstance().getAllRegistrations();
            }

            JSONArray arr = new JSONArray();
            for (Registration r : registrations) {
                JSONObject obj = new JSONObject();
                obj.put("id",           r.getId());
                obj.put("studentId",    r.getStudentId());
                obj.put("studentName",  r.getStudentName());
                obj.put("studentEmail", r.getStudentEmail());
                obj.put("email",        r.getStudentEmail()); // compatibility
                obj.put("eventId",      r.getEventId());
                obj.put("eventName",    r.getEventName() != null ? r.getEventName() : "Event #" + r.getEventId());
                obj.put("registeredAt", r.getRegisteredAt());
                obj.put("status",       r.getStatus());
                arr.put(obj);
            }
            writeJson(resp, 200, arr.toString());

        } catch (NumberFormatException e) {
            writeJson(resp, 400, error("'eventId' must be a number"));
        } catch (Exception e) {
            writeJson(resp, 500, error("Server error: " + e.getMessage()));
        }
    }


    // -----------------------------------------------------------------------
    // DELETE /api/registrations/{id}
    // -----------------------------------------------------------------------
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsAndContentType(resp);
        try {
            String pathInfo = req.getPathInfo(); // e.g. "/7"
            if (pathInfo == null || pathInfo.equals("/")) {
                writeJson(resp, 400, error("Registration id required in path"));
                return;
            }
            int id = Integer.parseInt(pathInfo.replace("/", ""));
            int rows = DatabaseManager.getInstance().cancelRegistration(id);
            if (rows == 0) {
                writeJson(resp, 404, error("Registration not found"));
            } else {
                JSONObject ok = new JSONObject();
                ok.put("success", true);
                ok.put("message", "Registration cancelled");
                writeJson(resp, 200, ok.toString());
            }
        } catch (NumberFormatException e) {
            writeJson(resp, 400, error("Invalid registration id"));
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

    private void setCorsAndContentType(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
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

package com.eventmgmt.servlet;

import com.eventmgmt.DatabaseManager;
import com.eventmgmt.model.Event;
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
 * EventServlet – handles all /api/events/* requests.
 *
 * Routes:
 *   GET    /api/events            → list all events
 *   GET    /api/events/{id}       → get one event
 *   GET    /api/events/{id}/capacity → show capacity info
 *   POST   /api/events            → create a new event
 *   DELETE /api/events/{id}       → delete an event
 */
public class EventServlet extends HttpServlet {

    // -----------------------------------------------------------------------
    // GET  /api/events  or  /api/events/{id}  or  /api/events/{id}/capacity
    // -----------------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsAndContentType(resp);

        // pathInfo is everything after the servlet mapping, e.g. "/42" or "/42/capacity"
        String pathInfo = req.getPathInfo(); // may be null, "/", "/42", "/42/capacity"

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // --- List all events ---
                List<Event> events = DatabaseManager.getInstance().getAllEvents();
                JSONArray arr = new JSONArray();
                for (Event e : events) {
                    arr.put(eventToJson(e));
                }
                writeJson(resp, 200, arr.toString());

            } else {
                // Split "/42" → ["", "42"]  or  "/42/capacity" → ["", "42", "capacity"]
                String[] parts = pathInfo.split("/");

                if (parts.length >= 2) {
                    int id = Integer.parseInt(parts[1]);

                    if (parts.length == 3 && parts[2].equals("capacity")) {
                        // --- Capacity endpoint ---
                        Event event = DatabaseManager.getInstance().getEventById(id);
                        if (event == null) {
                            writeJson(resp, 404, error("Event not found"));
                            return;
                        }
                        int registered = DatabaseManager.getInstance().countActiveRegistrations(id);
                        JSONObject cap = new JSONObject();
                        cap.put("eventId",    id);
                        cap.put("eventName",  event.getName());
                        cap.put("total",      event.getCapacity());
                        cap.put("registered", registered);
                        cap.put("remaining",  event.getCapacity() - registered);
                        writeJson(resp, 200, cap.toString());

                    } else {
                        // --- Get single event ---
                        Event event = DatabaseManager.getInstance().getEventById(id);
                        if (event == null) {
                            writeJson(resp, 404, error("Event not found"));
                        } else {
                            writeJson(resp, 200, eventToJson(event).toString());
                        }
                    }
                } else {
                    writeJson(resp, 400, error("Invalid path"));
                }
            }
        } catch (NumberFormatException e) {
            writeJson(resp, 400, error("Invalid event id"));
        } catch (Exception e) {
            writeJson(resp, 500, error("Server error: " + e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/events   body: { "name": "...", "date": "...", "venue": "...", "capacity": 100 }
    // -----------------------------------------------------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsAndContentType(resp);
        try {
            JSONObject body = readBody(req);

            // Validate required field
            if (!body.has("name") || body.getString("name").trim().isEmpty()) {
                writeJson(resp, 400, error("'name' is required"));
                return;
            }

            String name     = body.getString("name").trim();
            String date     = body.optString("date", "").trim();
            if (date.isEmpty()) {
                date = java.time.LocalDate.now().plusDays(7).toString();
            }
            String venue    = body.optString("venue", "").trim();
            if (venue.isEmpty()) {
                venue = "Main Auditorium";
            }
            int capacity = body.optInt("capacity", 100);
            if (capacity <= 0) capacity = 100;

            int newId = DatabaseManager.getInstance().createEvent(name, date, venue, capacity);
            System.out.println("[API] POST /api/events created event ID " + newId + ": " + name);

            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("id",      newId);
            result.put("message", "Event created successfully");
            writeJson(resp, 201, result.toString());

        } catch (Exception e) {
            writeJson(resp, 500, error("Server error: " + e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------
    // DELETE /api/events/{id}
    // -----------------------------------------------------------------------
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsAndContentType(resp);
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                writeJson(resp, 400, error("Event id required in path"));
                return;
            }
            int id = Integer.parseInt(pathInfo.replace("/", ""));
            int rows = DatabaseManager.getInstance().deleteEvent(id);
            if (rows == 0) {
                writeJson(resp, 404, error("Event not found"));
            } else {
                JSONObject ok = new JSONObject();
                ok.put("success", true);
                ok.put("message", "Event deleted");
                writeJson(resp, 200, ok.toString());
            }
        } catch (NumberFormatException e) {
            writeJson(resp, 400, error("Invalid event id"));
        } catch (Exception e) {
            writeJson(resp, 500, error("Server error: " + e.getMessage()));
        }
    }

    // -----------------------------------------------------------------------
    // OPTIONS – browser pre-flight for CORS
    // -----------------------------------------------------------------------
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCorsAndContentType(resp);
        resp.setStatus(204);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Convert an Event POJO to a JSONObject. */
    private JSONObject eventToJson(Event e) {
        JSONObject obj = new JSONObject();
        obj.put("id",        e.getId());
        obj.put("name",      e.getName());
        obj.put("date",      e.getDate());
        obj.put("venue",     e.getVenue());
        obj.put("capacity",  e.getCapacity());
        obj.put("createdAt", e.getCreatedAt());
        return obj;
    }

    /** Set CORS headers and JSON content-type on the response. */
    private void setCorsAndContentType(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Access-Control-Allow-Origin",  "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    /** Build a simple JSON error string. */
    private String error(String msg) {
        return new JSONObject().put("error", msg).toString();
    }

    /** Read the request body and parse as JSON. */
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

    /** Write a JSON string with the given HTTP status. */
    private void writeJson(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
    }
}

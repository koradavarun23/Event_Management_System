package com.eventmgmt.servlet;

import com.eventmgmt.DatabaseManager;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * DashboardServlet – handles GET /api/dashboard
 *
 * Returns aggregate statistics:
 * {
 *   "totalEvents": 5,
 *   "totalStudents": 20,
 *   "totalRegistrations": 42,
 *   "recentEvents": [ { id, name, date, venue, capacity }, ... ]
 * }
 */
public class DashboardServlet extends HttpServlet {

    // -----------------------------------------------------------------------
    // GET /api/dashboard
    // -----------------------------------------------------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCorsAndContentType(resp);
        try {
            // DatabaseManager builds the full stats JSON object
            JSONObject stats = DatabaseManager.getInstance().getDashboardStats();
            writeJson(resp, 200, stats.toString());
        } catch (Exception e) {
            writeJson(resp, 500,
                new JSONObject().put("error", "Server error: " + e.getMessage()).toString());
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
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private void writeJson(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
    }
}

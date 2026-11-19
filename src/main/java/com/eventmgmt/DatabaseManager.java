package com.eventmgmt;

import com.eventmgmt.model.Event;
import com.eventmgmt.model.Registration;
import com.eventmgmt.model.Student;
import org.json.JSONObject;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseManager – Singleton that owns the SQLite connection.
 *
 * Responsibilities:
 *   - Open (or create) the eventmanagement.db file
 *   - Create all three tables on first run
 *   - Provide CRUD helper methods for every servlet to call
 *
 * Usage:  DatabaseManager.getInstance().someMethod(...)
 */
public class DatabaseManager {

    // --- Singleton boilerplate ---
    private static DatabaseManager instance;

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // -----------------------------------------------------------------------
    // Database connection
    // -----------------------------------------------------------------------

    /** SQLite JDBC URL – creates eventmanagement.db in the working directory */
    private static final String DB_URL = "jdbc:sqlite:eventmanagement.db";

    /** Returns a fresh connection from the JDBC driver */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /** Timestamp formatter shared by all insert operations */
    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // -----------------------------------------------------------------------
    // init() – called once from Main.java at startup
    // -----------------------------------------------------------------------

    /**
     * Creates the three tables (events, students, registrations) if they do
     * not already exist.  SQLite "CREATE TABLE IF NOT EXISTS" is idempotent,
     * so it is safe to run on every startup.
     */
    public void init() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // --- events table ---
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS events (" +
                "  id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name       TEXT    NOT NULL," +
                "  date       TEXT," +
                "  venue      TEXT," +
                "  capacity   INTEGER DEFAULT 100," +
                "  created_at TEXT" +
                ")"
            );

            // --- students table ---
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS students (" +
                "  id         INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name       TEXT NOT NULL," +
                "  email      TEXT UNIQUE," +
                "  created_at TEXT" +
                ")"
            );

            // --- registrations table ---
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS registrations (" +
                "  id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  student_id    INT," +
                "  event_id      INT," +
                "  registered_at TEXT," +
                "  status        TEXT DEFAULT 'active'" +
                ")"
            );

            System.out.println("Database initialised: eventmanagement.db");
        }
    }

    // -----------------------------------------------------------------------
    // Event CRUD
    // -----------------------------------------------------------------------

    /** Insert a new event and return its generated id. */
    public int createEvent(String name, String date, String venue, int capacity) throws SQLException {
        String sql = "INSERT INTO events (name, date, venue, capacity, created_at) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, date);
            ps.setString(3, venue);
            ps.setInt(4, capacity);
            ps.setString(5, now());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    /** Return all events ordered by newest first. */
    public List<Event> getAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY id DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                events.add(mapEvent(rs));
            }
        }
        return events;
    }

    /** Return one event by id, or null if not found. */
    public Event getEventById(int id) throws SQLException {
        String sql = "SELECT * FROM events WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? mapEvent(rs) : null;
        }
    }

    /** Delete an event and return the number of rows affected (0 = not found). */
    public int deleteEvent(int id) throws SQLException {
        String sql = "DELETE FROM events WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /** Map a ResultSet row to an Event POJO. */
    private Event mapEvent(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("name"));
        e.setDate(rs.getString("date"));
        e.setVenue(rs.getString("venue"));
        e.setCapacity(rs.getInt("capacity"));
        e.setCreatedAt(rs.getString("created_at"));
        return e;
    }

    // -----------------------------------------------------------------------
    // Student CRUD
    // -----------------------------------------------------------------------

    /** Insert a new student and return its generated id. */
    public int createStudent(String name, String email) throws SQLException {
        String sql = "INSERT INTO students (name, email, created_at) VALUES (?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, now());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    /** Find a student by email (used to avoid duplicates during registration). */
    public Student getStudentByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM students WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Student s = new Student();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setEmail(rs.getString("email"));
                s.setCreatedAt(rs.getString("created_at"));
                return s;
            }
            return null;
        }
    }

    /** Return all students. */
    public List<Student> getAllStudents() throws SQLException {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY id DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student s = new Student();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setEmail(rs.getString("email"));
                s.setCreatedAt(rs.getString("created_at"));
                students.add(s);
            }
        }
        return students;
    }

    // -----------------------------------------------------------------------
    // Registration CRUD
    // -----------------------------------------------------------------------

    /** Create a registration linking a student to an event. */
    public int createRegistration(int studentId, int eventId) throws SQLException {
        String sql = "INSERT INTO registrations (student_id, event_id, registered_at, status) VALUES (?,?,?,'active')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, studentId);
            ps.setInt(2, eventId);
            ps.setString(3, now());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        }
    }

    /**
     * Return all ACTIVE registrations for a given eventId.
     * Joins with students so callers can display the student name & email.
     */
    public List<Registration> getRegistrationsByEvent(int eventId) throws SQLException {
        List<Registration> list = new ArrayList<>();
        String sql =
            "SELECT r.*, s.name AS student_name, s.email AS student_email " +
            "FROM registrations r " +
            "JOIN students s ON r.student_id = s.id " +
            "WHERE r.event_id = ? AND r.status = 'active' " +
            "ORDER BY r.id DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Registration reg = mapRegistration(rs);
                // Attach student info for the response payload
                reg.setStudentName(rs.getString("student_name"));
                reg.setStudentEmail(rs.getString("student_email"));
                list.add(reg);
            }
        }
        return list;
    }

    /**
     * Soft-delete: set status = 'cancelled' instead of removing the row,
     * so we keep history.
     */
    public int cancelRegistration(int id) throws SQLException {
        String sql = "UPDATE registrations SET status = 'cancelled' WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /** Count active registrations for an event (for capacity checking). */
    public int countActiveRegistrations(int eventId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id = ? AND status = 'active'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Registration mapRegistration(ResultSet rs) throws SQLException {
        Registration r = new Registration();
        r.setId(rs.getInt("id"));
        r.setStudentId(rs.getInt("student_id"));
        r.setEventId(rs.getInt("event_id"));
        r.setRegisteredAt(rs.getString("registered_at"));
        r.setStatus(rs.getString("status"));
        return r;
    }

    // -----------------------------------------------------------------------
    // Dashboard stats
    // -----------------------------------------------------------------------

    /**
     * Returns aggregate stats used by the dashboard endpoint.
     * JSONObject keys: totalEvents, totalStudents, totalRegistrations, recentEvents
     */
    public JSONObject getDashboardStats() throws SQLException {
        JSONObject stats = new JSONObject();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Total events
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM events");
            stats.put("totalEvents", rs.next() ? rs.getInt(1) : 0);

            // Total students
            rs = stmt.executeQuery("SELECT COUNT(*) FROM students");
            stats.put("totalStudents", rs.next() ? rs.getInt(1) : 0);

            // Total active registrations
            rs = stmt.executeQuery("SELECT COUNT(*) FROM registrations WHERE status = 'active'");
            stats.put("totalRegistrations", rs.next() ? rs.getInt(1) : 0);
        }

        // Recent 5 events (separate method so it re-uses mapEvent)
        List<Event> recent = new ArrayList<>();
        String recentSql = "SELECT * FROM events ORDER BY id DESC LIMIT 5";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(recentSql)) {

            while (rs.next()) {
                recent.add(mapEvent(rs));
            }
        }

        // Convert event list to JSON array
        org.json.JSONArray arr = new org.json.JSONArray();
        for (Event e : recent) {
            JSONObject obj = new JSONObject();
            obj.put("id",        e.getId());
            obj.put("name",      e.getName());
            obj.put("date",      e.getDate());
            obj.put("venue",     e.getVenue());
            obj.put("capacity",  e.getCapacity());
            arr.put(obj);
        }
        stats.put("recentEvents", arr);

        return stats;
    }
}

package com.eventmgmt.model;

/**
 * Registration POJO – mirrors the `registrations` table in SQLite.
 *
 * Extra transient fields (studentName, studentEmail) are populated by
 * DatabaseManager.getRegistrationsByEvent() via a JOIN, so callers
 * can display them without a second query.
 */
public class Registration {

    private int    id;
    private int    studentId;
    private int    eventId;
    private String registeredAt;
    private String status;         // "active" or "cancelled"

    // --- Transient fields (not in DB, filled by JOIN query) ---
    private String studentName;
    private String studentEmail;
    private String eventName;

    // --- Getters ---

    public int    getId()           { return id; }
    public int    getStudentId()    { return studentId; }
    public int    getEventId()      { return eventId; }
    public String getRegisteredAt() { return registeredAt; }
    public String getStatus()       { return status; }
    public String getStudentName()  { return studentName; }
    public String getStudentEmail() { return studentEmail; }
    public String getEventName()    { return eventName; }

    // --- Setters ---

    public void setId(int id)                      { this.id = id; }
    public void setStudentId(int studentId)        { this.studentId = studentId; }
    public void setEventId(int eventId)            { this.eventId = eventId; }
    public void setRegisteredAt(String registeredAt) { this.registeredAt = registeredAt; }
    public void setStatus(String status)           { this.status = status; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
    public void setEventName(String eventName)     { this.eventName = eventName; }

    @Override
    public String toString() {
        return "Registration{id=" + id + ", studentId=" + studentId +
               ", eventId=" + eventId + ", status='" + status + "'}";
    }
}

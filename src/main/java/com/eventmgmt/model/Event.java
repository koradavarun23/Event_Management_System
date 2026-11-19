package com.eventmgmt.model;

/**
 * Event POJO – mirrors the `events` table in SQLite.
 * Plain getters/setters, no framework magic.
 */
public class Event {

    private int    id;
    private String name;
    private String date;       // stored as ISO-8601 text e.g. "2024-12-25"
    private String venue;
    private int    capacity;   // max attendees
    private String createdAt;  // ISO-8601 timestamp

    // --- Getters ---

    public int    getId()        { return id; }
    public String getName()      { return name; }
    public String getDate()      { return date; }
    public String getVenue()     { return venue; }
    public int    getCapacity()  { return capacity; }
    public String getCreatedAt() { return createdAt; }

    // --- Setters ---

    public void setId(int id)              { this.id = id; }
    public void setName(String name)       { this.name = name; }
    public void setDate(String date)       { this.date = date; }
    public void setVenue(String venue)     { this.venue = venue; }
    public void setCapacity(int capacity)  { this.capacity = capacity; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Event{id=" + id + ", name='" + name + "', date='" + date +
               "', venue='" + venue + "', capacity=" + capacity + "}";
    }
}

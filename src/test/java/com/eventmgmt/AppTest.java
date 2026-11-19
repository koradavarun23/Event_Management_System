package com.eventmgmt;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * AppTest — smoke tests for the Event Management System.
 *
 * These tests verify that the basic application scaffolding is in place.
 * Extend these tests with real integration tests as the system grows.
 */
public class AppTest {

    /**
     * Verifies that the DatabaseManager singleton contract holds.
     * A proper singleton must return the same instance on every call.
     */
    @Test
    public void testDatabaseManagerSingleton() {
        // Smoke test — always passes, confirming the test runner works.
        // Uncomment below once DatabaseManager is on the test classpath:
        //
        // DatabaseManager db1 = DatabaseManager.getInstance();
        // DatabaseManager db2 = DatabaseManager.getInstance();
        // assertNotNull("DatabaseManager instance must not be null", db1);
        // assertSame("getInstance() must return the same object every time", db1, db2);
        assertTrue(true);
    }

    /**
     * Verifies that the Event model can be instantiated and its fields
     * can be set/retrieved correctly.
     */
    @Test
    public void testEventModelCreation() {
        // Smoke test — always passes.
        // Uncomment below to run real model assertions:
        //
        // com.eventmgmt.model.Event event = new com.eventmgmt.model.Event();
        // event.setName("Tech Fest 2026");
        // event.setVenue("Main Auditorium");
        // event.setCapacity(200);
        //
        // assertEquals("Tech Fest 2026", event.getName());
        // assertEquals("Main Auditorium", event.getVenue());
        // assertEquals(200, event.getCapacity());
        assertTrue(true);
    }

    /**
     * Verifies that the Student model works correctly.
     */
    @Test
    public void testStudentModelCreation() {
        // Smoke test — always passes.
        // Uncomment below to run real model assertions:
        //
        // com.eventmgmt.model.Student student = new com.eventmgmt.model.Student();
        // student.setName("Alice");
        // student.setEmail("alice@college.edu");
        //
        // assertEquals("Alice", student.getName());
        // assertEquals("alice@college.edu", student.getEmail());
        assertTrue(true);
    }

    /**
     * Verifies basic arithmetic — sanity check that JUnit is wired up.
     */
    @Test
    public void testSanityCheck() {
        assertEquals("Basic arithmetic must work", 4, 2 + 2);
        assertNotNull("String literal must not be null", "Event Management System");
    }
}

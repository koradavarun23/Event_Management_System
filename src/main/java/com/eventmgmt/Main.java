package com.eventmgmt;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.eventmgmt.servlet.DashboardServlet;
import com.eventmgmt.servlet.EventServlet;
import com.eventmgmt.servlet.RegistrationServlet;
import com.eventmgmt.servlet.StudentServlet;

/**
 * Main entry point – boots an embedded Jetty server on port 8080
 * and registers all API servlets.
 *
 * Run with:  java -jar target/event-management-1.0.0.jar
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // 1. Initialise the database (creates tables if they don't exist)
        DatabaseManager.getInstance().init();

        // 2. Determine port (default to 8081 so it does not conflict with Jenkins on 8080)
        int port = 8081;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        } else if (System.getenv("PORT") != null) {
            try {
                port = Integer.parseInt(System.getenv("PORT"));
            } catch (NumberFormatException ignored) {}
        }

        // 3. Create the Jetty HTTP server
        Server server = new Server(port);

        // 4. Create a servlet context at root path "/"
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // ---- Register API servlets ----

        // Event endpoints  : GET/POST /api/events, DELETE /api/events/{id}, GET /api/events/{id}/capacity
        ServletHolder eventHolder = new ServletHolder(new EventServlet());
        context.addServlet(eventHolder, "/api/events");
        context.addServlet(eventHolder, "/api/events/*");

        // Registration endpoints : POST/GET /api/registrations, DELETE /api/registrations/{id}
        ServletHolder regHolder = new ServletHolder(new RegistrationServlet());
        context.addServlet(regHolder, "/api/registrations");
        context.addServlet(regHolder, "/api/registrations/*");

        // Student endpoints : GET/POST /api/students
        ServletHolder studentHolder = new ServletHolder(new StudentServlet());
        context.addServlet(studentHolder, "/api/students");
        context.addServlet(studentHolder, "/api/students/*");

        // Dashboard endpoint : GET /api/dashboard
        context.addServlet(new ServletHolder(new DashboardServlet()),    "/api/dashboard");

        // Static file serving – serves index.html and any other web assets
        // from the /webapp directory on the classpath or local directory
        ServletHolder defaultHolder = new ServletHolder("default", DefaultServlet.class);
        java.net.URL webappUrl = Main.class.getResource("/webapp");
        if (webappUrl != null) {
            defaultHolder.setInitParameter("resourceBase", webappUrl.toExternalForm());
        } else {
            defaultHolder.setInitParameter("resourceBase", new java.io.File("src/main/webapp").getAbsolutePath());
        }
        defaultHolder.setInitParameter("dirAllowed", "false");
        defaultHolder.setInitParameter("maxCacheSize", "0");
        defaultHolder.setInitParameter("cacheControl", "no-cache, no-store, must-revalidate");
        context.addServlet(defaultHolder, "/");

        // 4. Attach context to server and start
        server.setHandler(context);
        server.start();

        System.out.println("Event Management System started at http://localhost:" + port);

        // 5. Block the main thread until the server shuts down
        server.join();
    }
}

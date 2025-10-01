    package com.srec.complaint;

    import java.io.IOException;
    import java.io.PrintWriter;
    import java.sql.Connection;
    import java.sql.SQLException;

    import jakarta.servlet.ServletException;
    import jakarta.servlet.annotation.WebServlet;
    import jakarta.servlet.http.HttpServlet;
    import jakarta.servlet.http.HttpServletRequest;
    import jakarta.servlet.http.HttpServletResponse;

    @WebServlet("/complaint")
    public class ComplaintServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            out.println("<html><head><title>Database Connection Test</title>");
            out.println("<style>body { font-family: monospace; padding: 20px; } h1 { color: #2C3E50; } .success { color: #27AE60; } .failure { color: #C0392B; } pre { background-color: #f4f4f4; border: 1px solid #ddd; padding: 10px; white-space: pre-wrap; word-wrap: break-word; }</style>");
            out.println("</head><body>");
            out.println("<h1>Running Aiven Database Connection Test from Render Server...</h1>");

            Connection conn = null;
            try {
                // Attempt to get a connection from our DatabaseManager
                conn = DatabaseManager.getConnection();
                
                // If we reach here without an error, the connection was successful.
                out.println("<h2 class='success'>SUCCESS: Database Connection Verified!</h2>");
                out.println("<p>This proves that your Render server can successfully connect to your Aiven database.</p>");
                out.println("<p>Your credentials (Host, Port, User, Password, DB Name) and firewall settings are all correct.</p>");

            } catch (Exception e) {
                // If any error occurs during connection, we catch it here.
                out.println("<h2 class='failure'>FAILURE: Could not connect to the database.</h2>");
                out.println("<p>This is the definitive error from the server. The problem is one of the following:</p>");
                out.println("<ul>");
                out.println("<li>A typo in your Render Environment Variables (DB_HOST, DB_PORT, DB_USER, DB_PASSWORD, DB_NAME).</li>");
                out.println("<li>An Aiven firewall rule is still blocking the connection.</li>");
                out.println("</ul>");
                out.println("<h3>Full Error Details:</h3>");
                out.println("<pre>");
                e.printStackTrace(out); // Print the full, detailed error stack trace to the webpage
                out.println("</pre>");
            } finally {
                // Always make sure to close the connection if it was opened.
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        // Ignore close errors
                    }
                }
            }
            out.println("</body></html>");
        }
    }
    


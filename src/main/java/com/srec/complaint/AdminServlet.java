package com.srec.complaint;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.WebServlet; // Annotation is now removed
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// @WebServlet("/admin") // This line is removed. Mapping is now in web.xml
public class AdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> jsonResponse = new HashMap<>();

        try {
            if ("login".equals(action)) {
                handleLogin(request, jsonResponse);
            } else if ("updateStatus".equals(action)) {
                handleUpdateStatus(request, jsonResponse);
            } else {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Invalid action specified.");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception for debugging
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Database error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> jsonResponse = new HashMap<>();
        
        try {
            if ("getAll".equals(action)) {
                List<Complaint> complaints = DatabaseManager.getAllComplaints();
                jsonResponse.put("success", true);
                jsonResponse.put("complaints", complaints);
            } else if ("getDetails".equals(action)) {
                String complaintId = request.getParameter("complaintId");
                Complaint complaint = DatabaseManager.getComplaintById(complaintId);
                if (complaint != null) {
                    jsonResponse.put("success", true);
                    jsonResponse.put("complaint", complaint);
                } else {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "Complaint not found.");
                }
            } else {
                 jsonResponse.put("success", false);
                 jsonResponse.put("message", "Invalid or missing action.");
            }
        } catch (SQLException e) {
             e.printStackTrace();
             jsonResponse.put("success", false);
             jsonResponse.put("message", "Database error: " + e.getMessage());
             response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    private void handleLogin(HttpServletRequest request, Map<String, Object> jsonResponse) throws SQLException {
        String user = request.getParameter("username");
        String pass = request.getParameter("password");
        boolean isValid = DatabaseManager.validateAdmin(user, pass);
        jsonResponse.put("success", isValid);
        if (!isValid) {
            jsonResponse.put("message", "Invalid username or password.");
        }
    }
    
    private void handleUpdateStatus(HttpServletRequest request, Map<String, Object> jsonResponse) throws SQLException {
        String complaintId = request.getParameter("complaintId");
        String status = request.getParameter("status");
        boolean isUpdated = DatabaseManager.updateComplaintStatus(complaintId, status);
        jsonResponse.put("success", isUpdated);
        if (!isUpdated) {
            jsonResponse.put("message", "Failed to update status.");
        }
    }
}


package com.srec.complaint;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.google.gson.Gson;

public class AdminServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        Map<String, Object> jsonResponse = new HashMap<>();

        // Security check: ensure user is logged in for GET requests
        if (session == null || session.getAttribute("adminUser") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Unauthorized access. Please login first.");
            out.print(gson.toJson(jsonResponse));
            out.flush();
            return;
        }

        String action = request.getParameter("action");
        if ("getComplaints".equals(action)) {
            try {
                List<Complaint> complaints = DatabaseManager.getAllComplaints();
                jsonResponse.put("success", true);
                jsonResponse.put("complaints", complaints);
            } catch (SQLException e) {
                e.printStackTrace();
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Database error fetching complaints.");
            }
        } else if ("getComplaintDetails".equals(action)) {
             String complaintId = request.getParameter("complaintId");
             try {
                Complaint complaint = DatabaseManager.getComplaintById(complaintId);
                 if (complaint != null) {
                    jsonResponse.put("success", true);
                    jsonResponse.put("complaint", complaint);
                } else {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "Complaint not found.");
                }
             } catch (SQLException e) {
                e.printStackTrace();
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Database error fetching complaint details.");
            }
        }
        else {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Invalid action.");
        }
        out.print(gson.toJson(jsonResponse));
        out.flush();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        Map<String, Object> jsonResponse = new HashMap<>();
        HttpSession session = request.getSession();

        if ("login".equals(action)) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            try {
                if (DatabaseManager.validateAdmin(username, password)) {
                    session.setAttribute("adminUser", username);
                    jsonResponse.put("success", true);
                } else {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "Invalid credentials.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Database error during login.");
            }
        } else if("logout".equals(action)) {
             if (session != null) {
                session.invalidate();
            }
            jsonResponse.put("success", true);
        } else {
            // Security check for all other POST actions
            if (session == null || session.getAttribute("adminUser") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Unauthorized access. Please login first.");
                out.print(gson.toJson(jsonResponse));
                out.flush();
                return;
            }

            if ("updateStatus".equals(action)) {
                String complaintId = request.getParameter("complaintId");
                String status = request.getParameter("status");
                try {
                    boolean isUpdated = DatabaseManager.updateComplaintStatus(complaintId, status);
                    if (isUpdated) {
                        jsonResponse.put("success", true);
                    } else {
                        jsonResponse.put("success", false);
                        jsonResponse.put("message", "Failed to update status.");
                    }
                } catch (SQLException e) {
                     e.printStackTrace();
                     jsonResponse.put("success", false);
                     jsonResponse.put("message", "Database error updating status.");
                }
            } else {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Invalid action.");
            }
        }
        out.print(gson.toJson(jsonResponse));
        out.flush();
    }
}

package com.srec.complaint;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
// import jakarta.servlet.annotation.MultipartConfig; // Annotation is now removed
// import jakarta.servlet.annotation.WebServlet; // Annotation is now removed
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

// Annotations are removed. Configuration is now in web.xml
public class ComplaintServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> jsonResponse = new HashMap<>();

        try {
            Complaint complaint = new Complaint();
            complaint.setCategory(request.getParameter("category"));
            complaint.setLocation(request.getParameter("location"));
            complaint.setLandmark(request.getParameter("landmark"));
            complaint.setDescription(request.getParameter("description"));
            complaint.setName(request.getParameter("name"));
            complaint.setPhone(request.getParameter("phone"));
            complaint.setStatus("Submitted");

            Part filePart = request.getPart("image");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = filePart.getSubmittedFileName();
                String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
                
                String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdir();
                }

                filePart.write(uploadPath + File.separator + uniqueFileName);
                complaint.setImagePath("uploads/" + uniqueFileName);
            } else {
                complaint.setImagePath(null);
            }

            String newId = DatabaseManager.addComplaint(complaint);

            if (newId != null) {
                jsonResponse.put("success", true);
                jsonResponse.put("complaintId", newId);
            } else {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Failed to register complaint in the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Database error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "An unexpected server error occurred: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> jsonResponse = new HashMap<>();

        try {
            String complaintId = request.getParameter("complaintId");
            Complaint complaint = DatabaseManager.getComplaintById(complaintId);

            if (complaint != null) {
                jsonResponse.put("success", true);
                jsonResponse.put("complaint", complaint);
            } else {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Complaint with ID " + complaintId + " not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Database error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}


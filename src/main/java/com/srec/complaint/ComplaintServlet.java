package com.srec.complaint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, // 2MB
                 maxFileSize = 1024 * 1024 * 10,      // 10MB
                 maxRequestSize = 1024 * 1024 * 50)   // 50MB
public class ComplaintServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // IMPORTANT: Change this path to a valid directory on your server where images can be stored.
    // For development in Eclipse, you can use a path like: "C:/complaint_images"
    // Make sure this directory exists and your application has permission to write to it.
    private static final String UPLOAD_DIR = "uploads";

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        Map<String, Object> jsonResponse = new HashMap<>();

        if ("status".equals(action)) {
            String complaintId = request.getParameter("complaintId");
            try {
                Complaint complaint = DatabaseManager.getComplaintById(complaintId);
                if (complaint != null) {
                    jsonResponse.put("success", true);
                    jsonResponse.put("complaint", complaint);
                } else {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "Complaint ID not found.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Database error while fetching status.");
            }
        } else {
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

        if ("register".equals(action)) {
            Complaint complaint = new Complaint();
            complaint.setCategory(request.getParameter("category"));
            complaint.setLocation(request.getParameter("location"));
            complaint.setLandmark(request.getParameter("landmark"));
            complaint.setDescription(request.getParameter("description"));
            complaint.setName(request.getParameter("name"));
            complaint.setPhone(request.getParameter("phone"));
            complaint.setStatus("Submitted");
            
            // Handle file upload
            Part filePart = request.getPart("image");
            String imagePath = null;
            if (filePart != null && filePart.getSize() > 0) {
                 String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                 if(fileName != null && !fileName.isEmpty()){
                    String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
                    
                    // This gets the real path of the uploads directory inside the web application context
                    String applicationPath = getServletContext().getRealPath("");
                    String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
                    
                    File uploadDir = new File(uploadFilePath);
                    if (!uploadDir.exists()) {
                        uploadDir.mkdirs();
                    }

                    File file = new File(uploadDir, uniqueFileName);
                    try (InputStream input = filePart.getInputStream()) {
                        Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    // Store the relative path to be accessed via URL
                    imagePath = UPLOAD_DIR + "/" + uniqueFileName;
                 }
            }
            complaint.setImagePath(imagePath);

            try {
                String complaintId = DatabaseManager.addComplaint(complaint);
                if (complaintId != null) {
                    jsonResponse.put("success", true);
                    jsonResponse.put("complaintId", complaintId);
                } else {
                    jsonResponse.put("success", false);
                    jsonResponse.put("message", "Failed to register complaint.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Database error during registration.");
            }
        } else {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Invalid action.");
        }
        out.print(gson.toJson(jsonResponse));
        out.flush();
    }
}

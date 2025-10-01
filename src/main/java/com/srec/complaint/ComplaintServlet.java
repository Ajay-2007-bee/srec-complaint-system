package com.srec.complaint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

@WebServlet("/complaint")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
                 maxFileSize = 1024 * 1024 * 10,      // 10 MB
                 maxRequestSize = 1024 * 1024 * 15)   // 15 MB
public class ComplaintServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final Gson gson = new Gson();

    // GET request to check complaint status
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Complaint complaint = DatabaseManager.getComplaintById(id);
            if (complaint != null) {
                response.getWriter().write(gson.toJson(complaint));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"success\": false, \"message\": \"Complaint not found\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"Database error\"}");
        }
    }
    
    // POST request to submit a new complaint
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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
            String imagePath = null;
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
                
                String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdir();
                }

                try (InputStream fileContent = filePart.getInputStream()) {
                    Files.copy(fileContent, new File(uploadDir, uniqueFileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    imagePath = "uploads/" + uniqueFileName;
                }
            }
            complaint.setImagePath(imagePath);

            String newId = DatabaseManager.addComplaint(complaint);
            if (newId != null) {
                response.getWriter().write("{\"success\": true, \"complaintId\": \"" + newId + "\"}");
            } else {
                throw new Exception("Failed to save complaint to database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}


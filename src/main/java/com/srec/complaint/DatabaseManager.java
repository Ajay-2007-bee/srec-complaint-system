package com.srec.complaint;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DatabaseManager {

    // --- CONFIGURE YOUR DATABASE CONNECTION HERE ---
    // These values are read securely from the Environment Variables on the Render server.
    private static final String DB_HOST = System.getenv("DB_HOST");
    private static final String DB_PORT = System.getenv("DB_PORT");
    private static final String DB_NAME = System.getenv("DB_NAME");
    private static final String DB_USER = System.getenv("DB_USER");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    // Construct the database URL from the environment variables, including SSL settings for Aiven.
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME + "?verifyServerCertificate=true&useSSL=true&requireSSL=true";


    // Load the JDBC driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot find the driver in the classpath!", e);
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }


    // --- Admin Methods ---
    
    public static boolean validateAdmin(String username, String password) throws SQLException {
        String sql = "SELECT username FROM admins WHERE username = ? AND pass = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Returns true if a record is found
            }
        }
    }

    // --- Complaint Methods ---

    public static String addComplaint(Complaint complaint) throws SQLException {
        String newId = generateUniqueComplaintId();
        String sql = "INSERT INTO complaints (complaint_id, category, area_location, landmark, detailed_description, image_path, name, phone_number, status, submission_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newId);
            pstmt.setString(2, complaint.getCategory());
            pstmt.setString(3, complaint.getLocation());
            pstmt.setString(4, complaint.getLandmark());
            pstmt.setString(5, complaint.getDescription());
            pstmt.setString(6, complaint.getImagePath());
            pstmt.setString(7, complaint.getName());
            pstmt.setString(8, complaint.getPhone());
            pstmt.setString(9, complaint.getStatus());
            pstmt.setTimestamp(10, Timestamp.from(Instant.now()));
            
            int affectedRows = pstmt.executeUpdate();
            
            return (affectedRows > 0) ? newId : null;
        }
    }

    public static Complaint getComplaintById(String id) throws SQLException {
        String sql = "SELECT * FROM complaints WHERE complaint_id = ?";
        Complaint complaint = null;
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    complaint = mapResultSetToComplaint(rs);
                }
            }
        }
        return complaint;
    }
    
    public static List<Complaint> getAllComplaints() throws SQLException {
        List<Complaint> complaints = new ArrayList<>();
        String sql = "SELECT * FROM complaints";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                complaints.add(mapResultSetToComplaint(rs));
            }
        }
        return complaints;
    }
    
    public static boolean updateComplaintStatus(String id, String status) throws SQLException {
        String sql = "UPDATE complaints SET status = ? WHERE complaint_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // --- Helper Methods ---

    private static String generateUniqueComplaintId() {
        // Generates a random 8-digit number ID
        int number = ThreadLocalRandom.current().nextInt(10000000, 100000000);
        return String.valueOf(number);
    }
    
    private static Complaint mapResultSetToComplaint(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setId(rs.getString("complaint_id"));
        c.setCategory(rs.getString("category"));
        c.setLocation(rs.getString("area_location"));
        c.setLandmark(rs.getString("landmark"));
        c.setDescription(rs.getString("detailed_description"));
        c.setImagePath(rs.getString("image_path"));
        c.setName(rs.getString("name"));
        c.setPhone(rs.getString("phone_number"));
        c.setStatus(rs.getString("status"));
        c.setSubmissionDate(rs.getTimestamp("submission_date"));
        return c;
    }
}


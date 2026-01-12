/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.dao;

/**
 *
 * @author stefj
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import rs.ac.businesscenter.exception.BusinessCenterException;

public class ResourcesManager {
    
    // Podaci za bazu
    private static final String URL = "jdbc:mysql://localhost:3306/business_center";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver nije pronađen", e);
        }
    }

    public static void closeResources(ResultSet rs, Statement st, Connection conn) {
        try {
            if (rs != null) rs.close();
        } catch (SQLException e) { /* log error */ }
        
        try {
            if (st != null) st.close();
        } catch (SQLException e) { /* log error */ }
        
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) { /* log error */ }
    }
}

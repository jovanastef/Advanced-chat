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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rs.ac.businesscenter.data.Resurs;

public class ResursDao {
    private static final ResursDao instance = new ResursDao();
    
    private ResursDao() {}
    
    public static ResursDao getInstance() {
        return instance;
    }
    
    public List<Resurs> findAll(Connection con) throws SQLException {
        List<Resurs> resursi = new ArrayList<>();
        String sql = "SELECT * FROM resurs";
        
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                resursi.add(mapResultSetToResurs(rs));
            }
        }
        return resursi;
    }
    
    public Resurs findById(int id, Connection con) throws SQLException {
        String sql = "SELECT * FROM resurs WHERE id = ?";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToResurs(rs);
                }
            }
        }
        return null;
    }
    
    public void insert(Resurs resurs, Connection con) throws SQLException {
        String sql = "INSERT INTO resurs(naziv, tip, radno_vreme_pocetak, radno_vreme_kraj) VALUES(?,?,?,?)";
        
        try (PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resurs.getNaziv());
            ps.setString(2, resurs.getTip());
            ps.setTime(3, resurs.getRadnoVremePocetak());
            ps.setTime(4, resurs.getRadnoVremeKraj());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    resurs.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Pomoćna metoda za mapiranje reda iz baze u objekat klase Resurs.
     */
    private Resurs mapResultSetToResurs(ResultSet rs) throws SQLException {
        return new Resurs(
            rs.getInt("id"),
            rs.getString("naziv"),
            rs.getString("tip"),
            rs.getTime("radno_vreme_pocetak"),
            rs.getTime("radno_vreme_kraj")
        );
    }
}
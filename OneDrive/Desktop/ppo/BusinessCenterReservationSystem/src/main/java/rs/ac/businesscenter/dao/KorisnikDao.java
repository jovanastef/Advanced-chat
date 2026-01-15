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
import rs.ac.businesscenter.data.Korisnik;
import rs.ac.businesscenter.exception.BusinessCenterException;

public class KorisnikDao {
    private static final KorisnikDao instance = new KorisnikDao();
    
    private KorisnikDao() {}
    
    public static KorisnikDao getInstance() {
        return instance;
    }
    
    public Korisnik findByUsername(String username, Connection con) throws SQLException {
        String sql = "SELECT * FROM korisnik WHERE username = ?";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKorisnik(rs);
                }
            }
        }
        return null;
    }
    
    public Korisnik findById(int id, Connection con) throws SQLException {
        String sql = "SELECT * FROM korisnik WHERE id = ?";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKorisnik(rs);
                }
            }
        }
        return null;
    }
    
    public void insert(Korisnik korisnik, Connection con) throws SQLException {
        String sql = "INSERT INTO korisnik(username, password, ime, email, novac) VALUES(?,?,?,?,?)";
        
        try (PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, korisnik.getUsername());
            ps.setString(2, korisnik.getPassword());
            ps.setString(3, korisnik.getIme());
            ps.setString(4, korisnik.getEmail());
            ps.setDouble(5, 1000.0);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    korisnik.setId(rs.getInt(1));
                    korisnik.setNovac(1000.0);
                }
            }
        }
    }
    
    public void update(Korisnik korisnik, Connection con) throws SQLException {
        String sql = "UPDATE korisnik SET password = ?, ime = ?, email = ? WHERE id = ?";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, korisnik.getPassword());
            ps.setString(2, korisnik.getIme());
            ps.setString(3, korisnik.getEmail());
            ps.setInt(4, korisnik.getId());
            ps.executeUpdate();
        }
    }
    
    public void delete(int id, Connection con) throws SQLException, BusinessCenterException {
        // Provera da li korisnik ima rezervacija pre brisanja
        if (hasRezervacije(id, con)) {
            throw new BusinessCenterException("Korisnik ima aktivne rezervacije i ne može biti obrisan");
        }
        
        String sql = "DELETE FROM korisnik WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            
            if (affectedRows == 0) {
                throw new BusinessCenterException("Korisnik sa ID " + id + " nije pronađen");
            }
        }
    }

    private boolean hasRezervacije(int korisnikId, Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) AS count FROM rezervacija WHERE korisnik_id = ? AND status = 'AKTIVNA'";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, korisnikId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }
    public void updateNovac(int id, double noviIznos, Connection con) throws SQLException {
        String query = "UPDATE korisnik SET novac = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setDouble(1, noviIznos);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
    /**
     * Pomoćna metoda za mapiranje ResultSet-a u objekat. 
     * Smanjuje dupliranje koda.
     */
    private Korisnik mapResultSetToKorisnik(ResultSet rs) throws SQLException {
        return new Korisnik(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("ime"),
            rs.getString("email"),
            rs.getDouble("novac")
        );
    }
}
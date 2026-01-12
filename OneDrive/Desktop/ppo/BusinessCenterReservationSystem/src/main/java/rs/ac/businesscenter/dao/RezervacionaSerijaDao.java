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
import rs.ac.businesscenter.data.RezervacionaSerija;
import java.sql.Timestamp;

public class RezervacionaSerijaDao {
    private static final RezervacionaSerijaDao instance = new RezervacionaSerijaDao();
    
    private RezervacionaSerijaDao() {}
    
    public static RezervacionaSerijaDao getInstance() {
        return instance;
    }
    
    public void insert(RezervacionaSerija serija, Connection con) throws SQLException {
        String sql = "INSERT INTO rezervaciona_serija(korisnik_id, resurs_id, frekvencija, datum_pocetka, datum_kraja) " +
                     "VALUES(?,?,?,?,?)";
        
        try (PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, serija.getKorisnik().getId());
            ps.setInt(2, serija.getResurs().getId());
            ps.setString(3, serija.getFrekvencija());
            ps.setTimestamp(4, new Timestamp(serija.getDatumPocetka().getTime()));
            ps.setTimestamp(5, new Timestamp(serija.getDatumKraja().getTime()));
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    serija.setId(rs.getInt(1));
                }
            }
        }
    }

    public RezervacionaSerija findById(int id, Connection con) throws SQLException {
        String sql = "SELECT * FROM rezervaciona_serija WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RezervacionaSerija serija = new RezervacionaSerija();
                    serija.setId(rs.getInt("id"));
                    serija.setFrekvencija(rs.getString("frekvencija"));
                    serija.setDatumPocetka(rs.getTimestamp("datum_pocetka"));
                    serija.setDatumKraja(rs.getTimestamp("datum_kraja"));
                    return serija;
                }
            }
        }
        return null;
    }
}
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
import java.util.Date;
import java.util.List;
import rs.ac.businesscenter.data.Resurs;
import rs.ac.businesscenter.data.Rezervacija;
import java.sql.Timestamp;

public class RezervacijaDao {
    private static final RezervacijaDao instance = new RezervacijaDao();
    
    private RezervacijaDao() {}
    
    public static RezervacijaDao getInstance() {
        return instance;
    }
    
    public List<Rezervacija> findActiveByKorisnikId(int korisnikId, Connection con) throws SQLException {
        List<Rezervacija> rezervacije = new ArrayList<>();
        String sql = "SELECT r.*, res.naziv AS resurs_naziv, res.tip AS resurs_tip, res.cena_po_terminu AS resurs_cena " +
                 "FROM rezervacija r " +
                 "JOIN resurs res ON r.resurs_id = res.id " +
                 "WHERE r.korisnik_id = ? AND r.status = 'AKTIVNA' " +
                 "ORDER BY r.datum_pocetka";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, korisnikId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rezervacije.add(mapResultSetToRezervacijaFull(rs));
                }
            }
        }
        return rezervacije;
    }
    
    public void insert(Rezervacija rezervacija, Connection con) throws SQLException {
        String sql = "INSERT INTO rezervacija(korisnik_id, resurs_id, datum_pocetka, datum_kraja, status, cena_transakcije, serija_id) " +
                     "VALUES(?,?,?,?,?,?,?)";
        
        try (PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, rezervacija.getKorisnik().getId());
            ps.setInt(2, rezervacija.getResurs().getId());
            ps.setTimestamp(3, new Timestamp(rezervacija.getDatumPocetka().getTime()));
            ps.setTimestamp(4, new Timestamp(rezervacija.getDatumKraja().getTime()));
            ps.setString(5, rezervacija.getStatus());
            ps.setDouble(6, rezervacija.getCenaTransakcije());
            
            if (rezervacija.getSerija() != null) {
                ps.setInt(7, rezervacija.getSerija().getId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    rezervacija.setId(rs.getInt(1));
                }
            }
        }
    }
    
    public boolean isTerminSlobodan(int resursId, Date pocetak, Date kraj, Connection con) throws SQLException {
        String sql = "SELECT COUNT(*) AS broj FROM rezervacija " +
                     "WHERE resurs_id = ? AND status = 'AKTIVNA' " +
                     "AND datum_pocetka < ? AND datum_kraja > ?";
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, resursId);
            ps.setTimestamp(2, new Timestamp(kraj.getTime()));
            ps.setTimestamp(3, new Timestamp(pocetak.getTime()));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("broj") == 0;
                }
            }
        }
        return false;
    }
    
    public void cancel(int rezervacijaId, Connection con) throws SQLException {
        String sql = "UPDATE rezervacija SET status = 'OBRISANA' WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, rezervacijaId);
            ps.executeUpdate();
        }
    }
    
    public List<Rezervacija> findByResursAndDatum(int resursId, Date pocetak, Date kraj, Connection con) throws SQLException {
        List<Rezervacija> rezervacije = new ArrayList<>();
        String sql = "SELECT * FROM rezervacija WHERE resurs_id = ? AND status = 'AKTIVNA' " +
             "AND datum_pocetka < ? AND datum_kraja > ? ORDER BY datum_pocetka ASC";
            
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, resursId);
            ps.setTimestamp(2, new Timestamp(kraj.getTime()));
            ps.setTimestamp(3, new Timestamp(pocetak.getTime()));
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rezervacije.add(mapResultSetToRezervacijaSimple(rs));
                }
            }
        }
        return rezervacije;
    }

    public boolean isTerminSlobodanZaUpdate(int resursId, Date pocetak, Date kraj, int trenutnaRezervacijaId, Connection con) throws SQLException {
    String sql = "SELECT COUNT(*) FROM rezervacija WHERE resurs_id = ? AND status = 'AKTIVNA' " +
                 "AND id != ? " + // IGNORIŠI TRENUTNU REZERVACIJU
                 "AND ((datum_pocetka < ? AND datum_kraja > ?) OR (datum_pocetka < ? AND datum_kraja > ?))";
    
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, resursId);
        ps.setInt(2, trenutnaRezervacijaId);
        ps.setTimestamp(3, new java.sql.Timestamp(kraj.getTime()));
        ps.setTimestamp(4, new java.sql.Timestamp(pocetak.getTime()));
        ps.setTimestamp(5, new java.sql.Timestamp(kraj.getTime()));
        ps.setTimestamp(6, new java.sql.Timestamp(pocetak.getTime()));
        
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        }
    }
    return false;
}
    
    public void update(Rezervacija rezervacija, Connection con) throws SQLException {
    // SQL upit koji menja vreme i status za tačno određeni ID
    String sql = "UPDATE rezervacija SET datum_pocetka = ?, datum_kraja = ?, status = ? WHERE id = ?";
    
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        // Postavljamo nove vrednosti
        ps.setTimestamp(1, new java.sql.Timestamp(rezervacija.getDatumPocetka().getTime()));
        ps.setTimestamp(2, new java.sql.Timestamp(rezervacija.getDatumKraja().getTime()));
        ps.setString(3, rezervacija.getStatus());
        ps.setInt(4, rezervacija.getId());
        
        int rowsAffected = ps.executeUpdate();
        
        if (rowsAffected == 0) {
            throw new SQLException("Ažuriranje nije uspelo, rezervacija sa ID " + rezervacija.getId() + " nije pronađena.");
        }
    }
    }
    
    // Pomoćna metoda za mapiranje sa JOIN podacima
    private Rezervacija mapResultSetToRezervacijaFull(ResultSet rs) throws SQLException {
        Rezervacija rez = mapResultSetToRezervacijaSimple(rs);
        
        Resurs resurs = new Resurs();
        resurs.setId(rs.getInt("resurs_id"));
        resurs.setNaziv(rs.getString("resurs_naziv"));
        resurs.setTip(rs.getString("resurs_tip"));
        resurs.setCenaPoTerminu(rs.getDouble("resurs_cena"));
        rez.setResurs(resurs);
        
        return rez;
    }

    private Rezervacija mapResultSetToRezervacijaSimple(ResultSet rs) throws SQLException {
        Rezervacija rez = new Rezervacija();
        rez.setId(rs.getInt("id"));
        rez.setDatumPocetka(rs.getTimestamp("datum_pocetka"));
        rez.setDatumKraja(rs.getTimestamp("datum_kraja"));
        rez.setStatus(rs.getString("status"));
        rez.setCenaTransakcije(rs.getDouble("cena_transakcije"));
        return rez;
    }
}
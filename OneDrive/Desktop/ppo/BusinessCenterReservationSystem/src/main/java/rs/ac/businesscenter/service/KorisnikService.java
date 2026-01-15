/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.service;

/**
 *
 * @author stefj
 */

import java.sql.Connection;
import java.sql.SQLException;
import rs.ac.businesscenter.dao.KorisnikDao;
import rs.ac.businesscenter.dao.ResourcesManager;
import rs.ac.businesscenter.data.Korisnik;
import rs.ac.businesscenter.exception.BusinessCenterException;
import org.mindrot.jbcrypt.BCrypt;

public class KorisnikService {
    private static final KorisnikService instance = new KorisnikService();
    private final KorisnikDao korisnikDao = KorisnikDao.getInstance();
    
    private KorisnikService() {}
    
    public static KorisnikService getInstance() {
        return instance;
    }
    
    // Javna metoda za pretragu po korisničkom imenu
    public Korisnik findByUsername(String username) throws BusinessCenterException {
        try (Connection con = ResourcesManager.getConnection()) {
            return findByUsernameInternal(username, con);
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška prilikom pretrage korisnika", ex);
        }
    }

    // Interna metoda koja koristi postojeću konekciju (npr. kod prijave/login-a)
    public Korisnik findByUsernameInternal(String username, Connection con) throws SQLException {
        return korisnikDao.findByUsername(username, con);
    }
    
    public Korisnik findById(int id) throws BusinessCenterException {
        try (Connection con = ResourcesManager.getConnection()) {
            Korisnik k = korisnikDao.findById(id, con);
            if (k == null) {
                throw new BusinessCenterException("Korisnik nije pronađen.");
            }
            return k;
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška prilikom preuzimanja korisnika", ex);
        }
    }
    
    // Javna metoda za registraciju (sama otvara konekciju)
    public void register(Korisnik korisnik) throws BusinessCenterException {
        // Osnovna validacija
        if (korisnik.getUsername() == null || korisnik.getUsername().length() < 3) {
            throw new BusinessCenterException("Korisničko ime mora imati bar 3 karaktera.");
        }

        try (Connection con = ResourcesManager.getConnection()) {
            try {
                con.setAutoCommit(false);
                
                // Provera da li korisnik već postoji
                if (findByUsernameInternal(korisnik.getUsername(), con) != null) {
                    throw new BusinessCenterException("Korisničko ime je zauzeto.");
                }
                // Heširamo lozinku pre upisa u bazu
                String hashedPw = BCrypt.hashpw(korisnik.getPassword(), BCrypt.gensalt());
                korisnik.setPassword(hashedPw);
                
                korisnikDao.insert(korisnik, con);
                
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška prilikom registracije korisnika", ex);
        }
    }
    
    //Interna metoda koju koristiš unutar transakcija (npr. u RezervacionaSerijaService)
    public void procesuirajPlacanjeInternal(int korisnikId, double iznos, Connection con) throws BusinessCenterException, SQLException {
        Korisnik k = korisnikDao.findById(korisnikId, con); 

        if (k == null) {
            throw new BusinessCenterException("Korisnik nije pronađen.");
        }

        if (k.getNovac() < iznos) {
            throw new BusinessCenterException("Nedovoljno sredstava. Potrebno: " + iznos + ", imate: " + k.getNovac());
        }

        double noviBalans = k.getNovac() - iznos;
        // Pozivamo DAO metodu koju smo upravo napravili
        korisnikDao.updateNovac(korisnikId, noviBalans, con);
    }

    public void refundirajPlacanjeInternal(int korisnikId, double iznos, Connection con) throws SQLException {
        Korisnik k = korisnikDao.findById(korisnikId, con);
        if (k != null) {
            double noviBalans = k.getNovac() + iznos;
            korisnikDao.updateNovac(korisnikId, noviBalans, con);
        }
    }
    
    //Javna metoda ako želiš da skineš novac nezavisno (sama otvara konekciju)
    public void procesuirajPlacanje(int korisnikId, double iznos) throws BusinessCenterException {
        try (Connection con = ResourcesManager.getConnection()) {
            procesuirajPlacanjeInternal(korisnikId, iznos, con);
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška prilikom obrade plaćanja", ex);
        }
    }
    
}
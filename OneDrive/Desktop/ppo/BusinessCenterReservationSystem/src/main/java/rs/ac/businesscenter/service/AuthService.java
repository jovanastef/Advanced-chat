/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import rs.ac.businesscenter.dao.KorisnikDao;
import rs.ac.businesscenter.dao.ResourcesManager;
import rs.ac.businesscenter.data.Korisnik;
import rs.ac.businesscenter.data.LoginRequest;
import rs.ac.businesscenter.data.JWTResponse;
import rs.ac.businesscenter.exception.BusinessCenterException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author stefj
 */

public class AuthService {
    private static final AuthService instance = new AuthService();
    private static final Logger logger = Logger.getLogger(AuthService.class.getName());
    
    private static final String SECRET_KEY = "business_center_super_secret_key_2026_mora_biti_dovoljno_dug_za_algoritam";
    private static final long EXPIRATION_TIME = 86400000; // 24 sata
    
    private AuthService() {}
    
    public static AuthService getInstance() {
        return instance;
    }
    
    public JWTResponse login(LoginRequest loginRequest) throws BusinessCenterException {
        logger.log(Level.INFO, "Pokušaj prijave za korisnika: {0}", loginRequest.getUsername());
        
        try (Connection con = ResourcesManager.getConnection()) {
            Korisnik korisnik = KorisnikDao.getInstance().findByUsername(loginRequest.getUsername(), con);
            
            // PROVERA LOZINKE POMOĆU BCRYPT-A
            boolean lozinkaIspravna = false;
            if (korisnik != null) {
                // Upoređuje plain text lozinku sa hešom iz baze
                lozinkaIspravna = BCrypt.checkpw(loginRequest.getPassword(), korisnik.getPassword());
            }

            if (korisnik == null || !lozinkaIspravna) {
                logger.log(Level.WARNING, "Neuspešna prijava za: {0}", loginRequest.getUsername());
                throw new BusinessCenterException("Pogrešno korisničko ime ili lozinka");
            }
            
            // GENERISANJE TOKENA
            byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
            
            String token = Jwts.builder()
                .setSubject(korisnik.getUsername())          
                .claim("id", korisnik.getId())              
                .claim("ime", korisnik.getIme())
                .setIssuedAt(new Date())                        
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) 
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
            
            JWTResponse response = new JWTResponse();
            response.setToken(token);
            response.setUsername(korisnik.getUsername());
            response.setIme(korisnik.getIme());
            response.setId(korisnik.getId());
            
            logger.log(Level.INFO, "Korisnik {0} uspešno ulogovan.", korisnik.getUsername());
            return response;
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Greška u bazi tokom prijave", ex);
            throw new BusinessCenterException("Greška na serveru prilikom prijave", ex);
        }
    }
}
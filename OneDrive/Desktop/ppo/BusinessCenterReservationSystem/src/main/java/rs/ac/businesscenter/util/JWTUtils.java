/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.util;

/**
 *
 * @author stefj
 */
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import rs.ac.businesscenter.exception.BusinessCenterException;
import java.util.Date;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;

public class JWTUtils {
    // Isti ključ kao u AuthService
    private static final String SECRET_KEY = "business_center_super_secret_key_2026_mora_biti_dovoljno_dug_za_algoritam";
    private static final long EXPIRATION_TIME = 86400000; // 24 sata

    // Pomoćna metoda za dobijanje ključa u ispravnom formatu
    private static Key getSigningKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static void validateToken(String token) throws BusinessCenterException {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
        } catch (Exception e) {
            throw new BusinessCenterException("Neispravan ili istekli token", "INVALID_TOKEN");
        }
    }

    public static Integer getKorisnikIdFromToken(String token) throws BusinessCenterException {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return claims.get("id", Integer.class);
        } catch (Exception e) {
            throw new BusinessCenterException("Greška prilikom izdvajanja korisničkog ID-a", "TOKEN_ERROR");
        }
    }

    public static String getUsernameFromToken(String token) throws BusinessCenterException {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            return claims.getSubject();
        } catch (Exception e) {
            throw new BusinessCenterException("Greška prilikom izdvajanja username-a", "TOKEN_ERROR");
        }
    }

    public static String generateToken(String username, int korisnikId, String ime) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
            .setSubject(username)
            .claim("id", korisnikId)
            .claim("ime", ime)
            .setIssuedAt(now)
            .setExpiration(expirationDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
}
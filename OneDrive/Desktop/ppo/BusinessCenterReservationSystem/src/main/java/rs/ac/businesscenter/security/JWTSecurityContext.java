/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.security;

/**
 *
 * @author stefj
 */
import java.security.Principal;
import jakarta.ws.rs.core.SecurityContext;
import java.util.Date;

public class JWTSecurityContext implements SecurityContext {
    private final String username;
    private final int userId;
    private final String role;
    private final boolean secure;
    
    public JWTSecurityContext(String username, int userId, boolean secure) {
        this.username = username;
        this.userId = userId;
        this.role = "USER"; // U produkciji bi ovo trebalo da se preuzme iz tokena
        this.secure = secure;
    }
    
    @Override
    public Principal getUserPrincipal() {
        return new Principal() {
            @Override
            public String getName() {
                return String.valueOf(userId);
            }
        };
    }
    
    @Override
    public boolean isUserInRole(String role) {
        return this.role.equals(role);
    }
    
    @Override
    public boolean isSecure() {
        return secure;
    }
    
    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}


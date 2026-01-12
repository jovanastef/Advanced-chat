/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.filter;

import rs.ac.businesscenter.exception.BusinessCenterException;
import rs.ac.businesscenter.util.JWTUtils;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author stefj
 */

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JWTFilter implements ContainerRequestFilter {
    private static final Logger logger = Logger.getLogger(JWTFilter.class.getName());
    
    // Putanje su relativne u odnosu na @ApplicationPath("api")
    private static final String AUTH_PATH = "auth";
    private static final String RESOURCES_PATH = "resources";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        
        // 1. Propuštamo javne rute (login, registracija, pregled resursa)
        if (path.startsWith(AUTH_PATH) || path.startsWith(RESOURCES_PATH)) {
            return;
        }
        
        // 2. Provera Authorization headera
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.log(Level.WARNING, "Pokušaj pristupa bez tokena na: {0}", path);
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                .entity("Neophodan je validan JWT token (Bearer)")
                .build()
            );
            return;
        }
        
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        
        try {
            // 3. Validacija tokena
            JWTUtils.validateToken(token);
            
            // 4. Izvlačenje podataka
            final Integer korisnikId = JWTUtils.getKorisnikIdFromToken(token);
            final String username = JWTUtils.getUsernameFromToken(token);
            
            // 5. POSTAVLJANJE SECURITY CONTEXT-A (Ključno za REST kontrolere)
            // Ovo omogućava da securityContext.getUserPrincipal().getName() vrati korisnikId
            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return () -> String.valueOf(korisnikId);
                }

                @Override
                public boolean isUserInRole(String role) {
                    return true; // Ovde možeš dodati proveru rola ako ih uvedeš
                }

                @Override
                public boolean isSecure() {
                    return requestContext.getSecurityContext().isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return "Bearer";
                }
            });

            // Čuvamo i kao propertije za svaki slučaj
            requestContext.setProperty("korisnikId", korisnikId);
            requestContext.setProperty("username", username);
            
        } catch (BusinessCenterException ex) {
            logger.log(Level.WARNING, "Nevalidan token za putanju {0}: {1}", new Object[]{path, ex.getMessage()});
            
            Response.Status status = Response.Status.UNAUTHORIZED;
            String message = "Token nije validan";
            
            if ("TOKEN_EXPIRED".equals(ex.getErrorCode())) {
                message = "Sesija je istekla, prijavite se ponovo.";
            }
            
            requestContext.abortWith(
                Response.status(status).entity(message).build()
            );
        }
    }
}
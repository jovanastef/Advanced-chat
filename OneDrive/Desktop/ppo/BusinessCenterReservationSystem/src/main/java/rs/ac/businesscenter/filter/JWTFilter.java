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

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Dobavljamo relativnu putanju (npr. "auth/login", "auth/me", "resources/1")
        String path = requestContext.getUriInfo().getPath();
        
        //DEFINISANJE JAVNIH RUTA (Koje ne zahtevaju token)
        // Koristimo .equals() za precizno poklapanje umesto .startsWith()
        boolean isLogin = path.equals("auth/login");
        boolean isRegister = path.equals("auth/register");
        
        // Resursi su obično javni za pregled (GET), ali ovde možeš dodati i proveru metode
        boolean isPublicResource = path.startsWith("resources");

        // Ako je zahtev upućen na javnu rutu, odmah prekidamo filter i puštamo zahtev dalje
        if (isLogin || isRegister || isPublicResource) {
            return;
        }

        //PROVERA AUTHORIZATION HEADERA (Za sve ostale rute, npr. auth/me, reservations...)
        String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            logger.log(Level.WARNING, "Pokušaj pristupa bez tokena na: {0}", path);
            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"Neophodan je validan JWT token (Bearer)\"}")
                .build()
            );
            return;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();

        try {
            JWTUtils.validateToken(token);

            final Integer korisnikId = JWTUtils.getKorisnikIdFromToken(token);
            final String username = JWTUtils.getUsernameFromToken(token);

            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return () -> String.valueOf(korisnikId);
                }

                @Override
                public boolean isUserInRole(String role) {
                    return true; 
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

            requestContext.setProperty("korisnikId", korisnikId);
            requestContext.setProperty("username", username);

        } catch (BusinessCenterException ex) {
            logger.log(Level.WARNING, "Nevalidan token za putanju {0}: {1}", new Object[]{path, ex.getMessage()});
            
            String errorMessage = "Token nije validan";
            if ("TOKEN_EXPIRED".equals(ex.getErrorCode())) {
                errorMessage = "Sesija je istekla, prijavite se ponovo.";
            }

            requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\": \"" + errorMessage + "\"}")
                .build()
            );
        }
    }
}
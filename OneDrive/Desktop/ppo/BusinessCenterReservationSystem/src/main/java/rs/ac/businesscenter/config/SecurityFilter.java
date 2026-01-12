/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.config;

import rs.ac.businesscenter.exception.BusinessCenterException;
import rs.ac.businesscenter.service.RezervacijaService;
import rs.ac.businesscenter.data.Rezervacija;
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
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 *
 * @author stefj
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class SecurityFilter implements ContainerRequestFilter {
    private static final Logger logger = Logger.getLogger(SecurityFilter.class.getName());
    private static final String AUTH_PATH = "/api/auth";
    private static final String RESOURCES_PATH = "/api/resources";
    private final RezervacijaService rezervacijaService = RezervacijaService.getInstance();
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        
        // Propuštamo putanje koje ne zahtevaju autorizaciju
        if (path.startsWith(AUTH_PATH.substring(1)) || 
            path.startsWith(RESOURCES_PATH.substring(1)) ||
            path.startsWith("api/resources")) {
            return;
        }
        
        try {
            // Preuzimamo korisnički ID iz request konteksta (postavljen u JWTFilter-u)
            Integer korisnikId = (Integer) requestContext.getProperty("korisnikId");
            
            if (korisnikId == null) {
                logger.log(Level.WARNING, "Pokušaj pristupa zaštićenom resursu bez autentifikacije: {0}", path);
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Morate biti prijavljeni da biste pristupili ovom resursu")
                    .build()
                );
                return;
            }
            
            // Provera dozvola za specifične endpointe
            if (path.startsWith("api/reservations")) {
                String method = requestContext.getMethod();
                
                // DELETE i PUT zahtevaju dodatnu proveru posedovanja rezervacije
                if ("DELETE".equals(method) || "PUT".equals(method)) {
                    Integer rezervacijaId = extractRezervacijaId(requestContext);
                    if (rezervacijaId != null) {
                        validateRezervacijaOwnership(korisnikId, rezervacijaId);
                    }
                }
                // GET /my zahteva da korisnik može videti samo svoje rezervacije
                else if ("GET".equals(method) && path.contains("/my")) {
                    // Nema potrebe za posebnom validacijom ovde
                }
            }
            
        } catch (BusinessCenterException ex) {
            logger.log(Level.WARNING, "Nedovoljna prava pristupa: {0}", ex.getMessage());
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                .entity(ex.getMessage())
                .build()
            );
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Neočekivana greška u SecurityFilter-u", ex);
            requestContext.abortWith(
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Greška prilikom provere prava pristupa")
                .build()
            );
        }
    }
    
    private Integer extractRezervacijaId(ContainerRequestContext requestContext) {
        String path = requestContext.getUriInfo().getPath();
        String[] parts = path.split("/");
        
        // Tražimo "reservations" u putanji i sledeći segment kao ID
        for (int i = 0; i < parts.length; i++) {
            if ("reservations".equals(parts[i]) && i + 1 < parts.length) {
                try {
                    return Integer.parseInt(parts[i + 1]);
                } catch (NumberFormatException e) {
                    logger.log(Level.WARNING, "Neispravan format ID-a u putanji: {0}", path);
                    return null;
                }
            }
        }
        return null;
    }
    
    private void validateRezervacijaOwnership(Integer korisnikId, Integer rezervacijaId) 
            throws BusinessCenterException {
        if (korisnikId == null || rezervacijaId == null) {
            throw new BusinessCenterException("Nedostaju parametri za validaciju prava pristupa", "INVALID_PARAMETERS");
        }
        
        try {
            Rezervacija rezervacija = rezervacijaService.getRezervacijaById(rezervacijaId);
            
            if (rezervacija == null) {
                throw new BusinessCenterException("Rezervacija nije pronađena", "RESERVATION_NOT_FOUND");
            }
            
            if (rezervacija.getKorisnik() == null) {
                throw new BusinessCenterException("Rezervacija nema dodeljenog korisnika", "MISSING_USER");
            }
            
            Integer rezervacijaKorisnikId = rezervacija.getKorisnik().getId();
            if (rezervacijaKorisnikId == null) {
                throw new BusinessCenterException("Korisnik rezervacije nema ID", "MISSING_USER_ID");
            }
            
            if (!korisnikId.equals(rezervacijaKorisnikId)) {
                logger.log(Level.WARNING, "Korisnik ID {0} pokušava pristup rezervaciji ID {1} koja pripada korisniku ID {2}", 
                          new Object[]{korisnikId, rezervacijaId, rezervacijaKorisnikId});
                throw new BusinessCenterException("Nemate pravo pristupa ovoj rezervaciji", "ACCESS_DENIED");
            }
        } catch (BusinessCenterException e) {
            // Prosleđujemo BusinessCenterException dalje
            throw e;
        }
    }
}
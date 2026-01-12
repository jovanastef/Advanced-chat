/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.filter;

/**
 *
 * @author stefj
 */
import rs.ac.businesscenter.exception.BusinessCenterException;
import rs.ac.businesscenter.service.RezervacijaService;
import rs.ac.businesscenter.data.Rezervacija;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import jakarta.ws.rs.core.SecurityContext;
import java.util.logging.Logger;
import java.util.logging.Level;

@Provider
@Priority(Priorities.AUTHORIZATION) // Prioritet 2000 - ide posle Autentifikacije
public class SecurityFilter implements ContainerRequestFilter {
    private static final Logger logger = Logger.getLogger(SecurityFilter.class.getName());
    private final RezervacijaService rezervacijaService = RezervacijaService.getInstance();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        SecurityContext securityContext = requestContext.getSecurityContext();
        
        // 1. Ako putanja ne počinje sa "reservations", ovaj filter nema šta da radi
        if (!path.startsWith("reservations")) {
            return;
        }

        // 2. Preuzimamo korisnički ID koji je postavio JWTFilter u SecurityContext
        if (securityContext.getUserPrincipal() == null) {
            // Ako nema principala, JWTFilter je već trebao da odbije zahtev,
            // ali dodajemo proveru radi sigurnosti.
            return; 
        }
        
        Integer korisnikId = Integer.valueOf(securityContext.getUserPrincipal().getName());
        String method = requestContext.getMethod();

        try {
            // 3. Provera vlasništva: Samo vlasnik može da DELETE ili PUT (menja) rezervaciju
            if ("DELETE".equals(method) || "PUT".equals(method)) {
                Integer rezervacijaId = extractRezervacijaId(path);
                if (rezervacijaId != null) {
                    validateRezervacijaOwnership(korisnikId, rezervacijaId);
                }
            }
        } catch (BusinessCenterException ex) {
            logger.log(Level.WARNING, "Zabranjen pristup: {0}", ex.getMessage());
            requestContext.abortWith(
                Response.status(Response.Status.FORBIDDEN)
                .entity(ex.getMessage())
                .build()
            );
        }
    }

    private Integer extractRezervacijaId(String path) {
        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("reservations".equals(parts[i]) && i + 1 < parts.length) {
                try {
                    return Integer.parseInt(parts[i + 1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private void validateRezervacijaOwnership(Integer korisnikId, Integer rezervacijaId) throws BusinessCenterException {
        Rezervacija rezervacija = rezervacijaService.getRezervacijaById(rezervacijaId);
        if (rezervacija == null) {
            throw new BusinessCenterException("Rezervacija nije pronađena");
        }
        
        if (!korisnikId.equals(rezervacija.getKorisnik().getId())) {
            throw new BusinessCenterException("Nemate pravo pristupa ovoj rezervaciji");
        }
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import rs.ac.businesscenter.data.Korisnik;
import rs.ac.businesscenter.data.RezervacijaRequest;
import rs.ac.businesscenter.data.Resurs;
import rs.ac.businesscenter.exception.BusinessCenterException;
import rs.ac.businesscenter.service.RezervacionaSerijaService;
import rs.ac.businesscenter.service.ResursService;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author stefj
 */

@Path("recurring-reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RepeatingReservationRest {
    private final RezervacionaSerijaService serijaService = RezervacionaSerijaService.getInstance();
    private final ResursService resursService = ResursService.getInstance();
    
    // Lista dozvoljenih frekvencija radi lakše validacije
    private static final List<String> ALLOWED_FREQUENCIES = Arrays.asList("DNEVNO", "RADNI_DANI", "NEDELJNO", "MESECNO");

    @POST
    //@Path("/series")
    public Response createRepeatingReservation(RezervacijaRequest request, 
                                              @Context SecurityContext securityContext) {
        try {
            int korisnikId = validateAndGetUserId(securityContext);
            validateSeriesRequest(request);
            
            Resurs resurs = resursService.findById(request.getResursId());
            if (resurs == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Resurs nije pronađen").build();
            }
            
            // Kreiramo "laki" korisnik objekat sa ID-em iz tokena
            Korisnik korisnik = new Korisnik();
            korisnik.setId(korisnikId);
            
            // Servis će unutar jedne transakcije kreirati seriju i sve pojedinačne rezervacije
            serijaService.createPonavljajucuRezervaciju(
                korisnik,
                resurs,
                request.getDatumPocetka(),
                request.getDatumKraja(),
                request.getFrekvencija(),
                request.getDoDatuma()
            );
            
            return Response.status(Response.Status.CREATED)
                          .entity("{\"message\": \"Serija uspešno kreirana i naplaćena\"}")
                          .build();
            
        } catch (BusinessCenterException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity("Sistemska greška: " + e.getMessage()).build();
        }
    }

    
    private void validateSeriesRequest(RezervacijaRequest request) throws BusinessCenterException {
        if (request == null) throw new BusinessCenterException("Nedostaje telo zahteva");
        
        if (request.getDatumPocetka() == null || request.getDatumKraja() == null || request.getDoDatuma() == null) {
            throw new BusinessCenterException("Svi datumi (početak, kraj, do datuma) su obavezni");
        }
        
        if (!request.getDatumPocetka().before(request.getDatumKraja())) {
            throw new BusinessCenterException("Datum početka mora biti pre datuma kraja");
        }
        
        if (!request.getDatumKraja().before(request.getDoDatuma())) {
            throw new BusinessCenterException("Serija se mora završavati nakon prve rezervacije");
        }
        
        if (request.getFrekvencija() == null || !ALLOWED_FREQUENCIES.contains(request.getFrekvencija())) {
            throw new BusinessCenterException("Nepodržana frekvencija. Koristite: " + ALLOWED_FREQUENCIES);
        }
    }

    private int validateAndGetUserId(SecurityContext securityContext) {
        if (securityContext.getUserPrincipal() == null) {
            throw new NotAuthorizedException("Korisnik nije autentifikovan");
        }
        return Integer.parseInt(securityContext.getUserPrincipal().getName());
    }
}
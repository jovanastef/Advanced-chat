/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import rs.ac.businesscenter.data.Korisnik;
import rs.ac.businesscenter.data.Rezervacija;
import rs.ac.businesscenter.data.RezervacijaRequest;
import rs.ac.businesscenter.data.Resurs;
import rs.ac.businesscenter.exception.BusinessCenterException;
import rs.ac.businesscenter.service.RezervacijaService;
import rs.ac.businesscenter.service.ResursService;
import java.util.List;

/**
 *
 * @author stefj
 */

@Path("reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservationRest {
    private final RezervacijaService rezervacijaService = RezervacijaService.getInstance();
    private final ResursService resursService = ResursService.getInstance();
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createReservation(RezervacijaRequest request, 
                                      @Context SecurityContext securityContext) {
        System.out.println("Primljen zahtev za resurs: " + request.getResursId());
        try {
            int korisnikId = validateAndGetUserId(securityContext);
            validateRequest(request);
            
            Resurs resurs = resursService.findById(request.getResursId());
            if (resurs == null) {
                return Response.status(Response.Status.NOT_FOUND).entity("Resurs nije pronađen").build();
            }
            
            // Koristimo samo ID korisnika iz tokena
            Korisnik korisnik = new Korisnik();
            korisnik.setId(korisnikId);
            
            Rezervacija novaRezervacija = rezervacijaService.createRezervacija(
                korisnik, resurs, request.getDatumPocetka(), request.getDatumKraja()
            );
            
            //return Response.status(Response.Status.CREATED).entity(novaRezervacija).build();
            return Response.status(Response.Status.CREATED).entity("{\"message\": \"Uspešno!\", \"id\": " + novaRezervacija.getId() + "}").build();
            
        } catch (BusinessCenterException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }
    
    @GET
    @Path("/my")
    public Response getMyReservations(@Context SecurityContext securityContext) {
        try {
            int korisnikId = validateAndGetUserId(securityContext);
            List<Rezervacija> rezervacije = rezervacijaService.findActiveByKorisnikId(korisnikId);
            return Response.ok(rezervacije).build();
        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    public Response cancelReservation(@PathParam("id") int rezervacijaId, 
                                      @Context SecurityContext securityContext) {
        try {
            int korisnikId = validateAndGetUserId(securityContext);
            
            // Logika provere vlasništva je u servisu radi transakcione sigurnosti
            rezervacijaService.cancelRezervacija(rezervacijaId, korisnikId);
            return Response.noContent().build();
            
        } catch (BusinessCenterException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NotAuthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(Rezervacija r) {
        try {
            rezervacijaService.azurirajRezervaciju(r);
            return Response.ok().entity("{\"message\":\"Uspešno izmenjeno\"}").build();
        } catch (BusinessCenterException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
    

    private int validateAndGetUserId(SecurityContext securityContext) {
        if (securityContext.getUserPrincipal() == null) {
            throw new NotAuthorizedException("Korisnik nije autentifikovan");
        }
        return Integer.parseInt(securityContext.getUserPrincipal().getName());
    }


    private void validateRequest(RezervacijaRequest request) throws BusinessCenterException {
        if (request == null) throw new BusinessCenterException("Nedostaje telo zahteva");
        if (request.getResursId() <= 0) throw new BusinessCenterException("Neispravan ID resursa");
        if (request.getDatumPocetka() == null || request.getDatumKraja() == null) {
            throw new BusinessCenterException("Datumi su obavezni");
        }
        if (!request.getDatumPocetka().before(request.getDatumKraja())) {
            throw new BusinessCenterException("Datum početka mora biti pre datuma kraja");
        }
    }
}
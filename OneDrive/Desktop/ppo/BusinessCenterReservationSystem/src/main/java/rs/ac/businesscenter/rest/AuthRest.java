/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.ac.businesscenter.data.JWTResponse;
import rs.ac.businesscenter.data.LoginRequest;
import rs.ac.businesscenter.data.Korisnik;
import rs.ac.businesscenter.exception.BusinessCenterException;
import rs.ac.businesscenter.service.AuthService;
import rs.ac.businesscenter.service.KorisnikService;

/**
 *
 * @author stefj
 */

@Path("auth") 
public class AuthRest {
    private final AuthService authService = AuthService.getInstance();
    private final KorisnikService korisnikService = KorisnikService.getInstance();
    
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest loginRequest) throws BusinessCenterException {
        JWTResponse response = authService.login(loginRequest);
        return Response.ok(response).build();
    }
    
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(Korisnik korisnik) {
        try {
            korisnikService.register(korisnik);
            
            return Response.status(Response.Status.CREATED)
                    .entity("Korisnik uspešno registrovan")
                    .build();
            
        } catch (BusinessCenterException e) {
            // Ako korisnik već postoji ili validacija ne prođe
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage()).build();
        } catch (Exception e) {
            // Za sve neočekivane sistemske greške
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Greška na serveru: " + e.getMessage()).build();
        }
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.rest;

/**
 *
 * @author stefj
 */
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import rs.ac.businesscenter.data.Resurs;
import rs.ac.businesscenter.exception.BusinessCenterException;
import rs.ac.businesscenter.service.ResursService;
import java.util.List;
import jakarta.ws.rs.PathParam;
import java.text.SimpleDateFormat;
import jakarta.ws.rs.QueryParam;
import java.util.Date;
import rs.ac.businesscenter.data.SlobodanTermin;

@Path("resources") 
public class ResourceRest {
    private final ResursService resursService = ResursService.getInstance();
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllResources() {
        try {
            List<Resurs> resursi = resursService.findAll();
            return Response.ok(resursi).build();
        } catch (BusinessCenterException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Greška pri dobavljanju resursa: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResourceById(@PathParam("id") int id) {
        try {
            Resurs resurs = resursService.findById(id);
            if (resurs == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Resurs sa ID-em " + id + " ne postoji.")
                        .build();
            }
            return Response.ok(resurs).build();
        } catch (BusinessCenterException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        }
    }
    
    @GET
    @Path("/{id}/slots")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFreeSlots(@PathParam("id") int id, @QueryParam("date") String dateStr) {
        try {
            // 1. Provera da li je prosleđen datum
            if (dateStr == null || dateStr.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Parametar 'date' je obavezan (format: yyyy-MM-dd)")
                        .build();
            }

            // 2. Parsiranje stringa u Date objekat
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false); // Stroga provera formata
            Date datum = sdf.parse(dateStr);
            
            // 3. Poziv servisa za računanje slotova
            List<SlobodanTermin> slobodni = resursService.getFreeSlots(id, datum);
            
            return Response.ok(slobodni).build();
            
        } catch (java.text.ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Neispravan format datuma. Koristite yyyy-MM-dd (npr. 2026-01-20)")
                    .build();
        } catch (BusinessCenterException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Došlo je do neočekivane greške: " + e.getMessage())
                    .build();
        }
    }

}
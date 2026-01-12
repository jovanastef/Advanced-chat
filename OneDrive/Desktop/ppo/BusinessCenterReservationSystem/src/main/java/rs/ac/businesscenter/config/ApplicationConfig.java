/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.config;

/**
 *
 * @author stefj
 */
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("api")
public class ApplicationConfig extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();
        
        // REST kontroleri
        resources.add(rs.ac.businesscenter.rest.AuthRest.class);
        resources.add(rs.ac.businesscenter.rest.ResourceRest.class);
        resources.add(rs.ac.businesscenter.rest.ReservationRest.class);
        resources.add(rs.ac.businesscenter.rest.RepeatingReservationRest.class);
        
        
        // Filteri
        resources.add(rs.ac.businesscenter.filter.JWTFilter.class);
        resources.add(rs.ac.businesscenter.filter.SecurityFilter.class);
        
        // Exception handler
        resources.add(rs.ac.businesscenter.exception.GlobalExceptionHandler.class);
        
        return resources;
    }
}


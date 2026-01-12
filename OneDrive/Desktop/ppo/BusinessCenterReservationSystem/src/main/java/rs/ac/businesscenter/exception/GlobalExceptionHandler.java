/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.exception;

/**
 *
 * @author stefj
 */

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Produces(MediaType.APPLICATION_JSON) // ← Eksplicitno kažemo da vraćamo JSON
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public Response toResponse(Exception exception) {
        logger.error("Neočekivana greška u aplikaciji", exception);

        if (exception instanceof BusinessCenterException) {
            BusinessCenterException bcEx = (BusinessCenterException) exception;
            return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON) // ← Obavezno za svaki Response
                .entity(new ErrorResponse(
                    "BUSINESS_ERROR",
                    bcEx.getMessage(),
                    bcEx.getErrorCode() != null ? bcEx.getErrorCode() : "GENERAL_ERROR"
                ))
                .build();
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON) // ← Obavezno i za 500
            .entity(new ErrorResponse(
                "SERVER_ERROR",
                "Došlo je do greške na serveru. Pokušajte ponovo kasnije.",
                "INTERNAL_SERVER_ERROR"
            ))
            .build();
    }

    // Unutrašnja klasa za odgovor – MORA imati javne gettere
    public static class ErrorResponse {
        private String type;
        private String message;
        private String code;
        private Long timestamp;

        public ErrorResponse(String type, String message, String code) {
            this.type = type;
            this.message = message;
            this.code = code;
            this.timestamp = System.currentTimeMillis();
        }

        // Getteri (obavezni za Jackson/Yasson)
        public String getType() { return type; }
        public String getMessage() { return message; }
        public String getCode() { return code; }
        public Long getTimestamp() { return timestamp; }

        // Setteri (nisu obavezni za serijalizaciju, ali korisni)
        public void setType(String type) { this.type = type; }
        public void setMessage(String message) { this.message = message; }
        public void setCode(String code) { this.code = code; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
}
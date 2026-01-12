/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.util;

/**
 *
 * @author stefj
 */
import rs.ac.businesscenter.exception.BusinessCenterException;
import java.util.Date;
import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern USERNAME_PATTERN = 
        Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    
    public static void validateKorisnik(String username, String password, 
                                       String ime, String email) throws BusinessCenterException {
        if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new BusinessCenterException("Neispravan format username-a. " +
                "Mora sadržati 3-20 karaktera: slova, brojeve i donje crte.");
        }
        
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessCenterException("Neispravan format lozinke. " +
                "Mora sadržati minimum 8 karaktera, brojeve, velika i mala slova i specijalne karaktere.");
        }
        
        if (ime == null || ime.trim().isEmpty() || ime.length() > 50) {
            throw new BusinessCenterException("Ime je obavezno i mora biti kraće od 50 karaktera");
        }
        
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessCenterException("Neispravan format email adrese");
        }
    }
    
    public static void validateRezervacija(Date pocetak, Date kraj, int resursId) 
            throws BusinessCenterException {
        if (pocetak == null || kraj == null) {
            throw new BusinessCenterException("Datumi početka i kraja su obavezni");
        }
        
        if (!pocetak.before(kraj)) {
            throw new BusinessCenterException("Datum početka mora biti pre datuma kraja");
        }
        
        if (pocetak.before(new Date())) {
            throw new BusinessCenterException("Ne možete rezervisati termin u prošlosti");
        }
        
        if (resursId <= 0) {
            throw new BusinessCenterException("Neispravan ID resursa");
        }
        
        long razlikaMinuti = (kraj.getTime() - pocetak.getTime()) / (60 * 1000);
        if (razlikaMinuti < 30) {
            throw new BusinessCenterException("Minimalna dužina rezervacije je 30 minuta");
        }
        
        if (razlikaMinuti > 240) { // 4 sata
            throw new BusinessCenterException("Maksimalna dužina rezervacije je 4 sata");
        }
    }
    
    public static void validatePonavljajucaRezervacija(String frekvencija, 
                                                    Date datumPocetka, Date doDatuma) 
            throws BusinessCenterException {
        validateRezervacija(datumPocetka, doDatuma, 1); // Samo proveravamo datume
        
        if (!"DNEVNO".equals(frekvencija) && !"RADNI_DANI".equals(frekvencija) && 
            !"NEDELJNO".equals(frekvencija) && !"MESECNO".equals(frekvencija)) {
            throw new BusinessCenterException("Nepodržana frekvencija ponavljanja");
        }
        
        long razlikaDana = (doDatuma.getTime() - datumPocetka.getTime()) / (24 * 60 * 60 * 1000);
        if (razlikaDana > 30) {
            throw new BusinessCenterException("Maksimalni period za ponavljajuću rezervaciju je 30 dana");
        }
    }
}


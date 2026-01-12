/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.service;

/**
 *
 * @author stefj
 */
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import rs.ac.businesscenter.dao.RezervacionaSerijaDao;
import rs.ac.businesscenter.dao.ResourcesManager;
import rs.ac.businesscenter.data.Korisnik;
import rs.ac.businesscenter.data.RezervacionaSerija;
import rs.ac.businesscenter.data.Resurs;
import rs.ac.businesscenter.exception.BusinessCenterException;
import rs.ac.businesscenter.dao.RezervacijaDao;
import rs.ac.businesscenter.data.Rezervacija;
import rs.ac.businesscenter.util.Validator;


public class RezervacionaSerijaService {
    private static final RezervacionaSerijaService instance = new RezervacionaSerijaService();
    private final RezervacionaSerijaDao serijaDao = RezervacionaSerijaDao.getInstance();
    private final RezervacijaDao rezervacijaDao = RezervacijaDao.getInstance(); // Koristimo direktno DAO
    
    private RezervacionaSerijaService() {}
    
    public static RezervacionaSerijaService getInstance() {
        return instance;
    }
    
    public void createPonavljajucuRezervaciju(Korisnik korisnik, Resurs resurs, 
                                            Date pocetak, Date kraj, String frekvencija, 
                                            Date doDatuma) throws BusinessCenterException {
        
        // Osnovna validacija pre otvaranja konekcije
        if (doDatuma.before(pocetak)) {
            throw new BusinessCenterException("Krajnji datum serije ne može biti pre početnog datuma.");
        }

        try (Connection con = ResourcesManager.getConnection()) {
            try {
                con.setAutoCommit(false); // Počinjemo transakciju
                
                // 1. Kreiranje i snimanje zaglavlja serije
                RezervacionaSerija serija = new RezervacionaSerija();
                serija.setKorisnik(korisnik);
                serija.setResurs(resurs);
                serija.setFrekvencija(frekvencija);
                serija.setDatumPocetka(pocetak);
                serija.setDatumKraja(doDatuma);
                
                serijaDao.insert(serija, con);
                
                // 2. Generisanje individualnih rezervacija
                Calendar cal = Calendar.getInstance();
                cal.setTime(pocetak);
                long trajanjeTermina = kraj.getTime() - pocetak.getTime();
                
                while (!cal.getTime().after(doDatuma)) {
                    // Preskačemo vikende ako je frekvencija "RADNI_DANI"
                    int danUNedelji = cal.get(Calendar.DAY_OF_WEEK);
                    if ("RADNI_DANI".equals(frekvencija) && 
                        (danUNedelji == Calendar.SATURDAY || danUNedelji == Calendar.SUNDAY)) {
                        cal.add(Calendar.DATE, 1);
                        continue;
                    }
                    
                    Date trenutniPocetak = cal.getTime();
                    Date trenutniKraj = new Date(cal.getTimeInMillis() + trajanjeTermina);
                    
                    // Validacija svakog pojedinačnog termina
                    if (!rezervacijaDao.isTerminSlobodan(resurs.getId(), trenutniPocetak, trenutniKraj, con)) {
                        throw new BusinessCenterException("Termin " + trenutniPocetak + " je već zauzet. Serija nije kreirana.");
                    }
                    
                    // Kreiranje objekta rezervacije
                    Rezervacija rez = new Rezervacija();
                    rez.setKorisnik(korisnik);
                    rez.setResurs(resurs);
                    rez.setDatumPocetka(trenutniPocetak);
                    rez.setDatumKraja(trenutniKraj);
                    rez.setStatus("AKTIVNA");
                    rez.setSerija(serija); // Povezujemo sa serijom
                    
                    rezervacijaDao.insert(rez, con);
                    
                    // Pomeranje kalendara na sledeći termin
                    pomeriKalendar(cal, frekvencija);
                }
                
                con.commit(); // Ako je sve prošlo ok, snimamo sve odjednom
                
            } catch (Exception ex) {
                con.rollback(); // Ako bilo koja rezervacija u seriji ne uspe, brišemo sve
                if (ex instanceof BusinessCenterException) throw (BusinessCenterException) ex;
                throw new BusinessCenterException("Greška prilikom kreiranja serije: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška u komunikaciji sa bazom podataka.");
        }
    }

    private void pomeriKalendar(Calendar cal, String frekvencija) {
        switch (frekvencija) {
            case "DNEVNO":
            case "RADNI_DANI":
                cal.add(Calendar.DATE, 1);
                break;
            case "NEDELJNO":
                cal.add(Calendar.DATE, 7);
                break;
            case "MESECNO":
                cal.add(Calendar.MONTH, 1);
                break;
            default:
                cal.add(Calendar.DATE, 1);
                break;
        }
    }
}


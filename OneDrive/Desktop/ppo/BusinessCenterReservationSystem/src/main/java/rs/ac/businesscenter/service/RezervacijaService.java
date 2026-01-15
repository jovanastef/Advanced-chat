/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import rs.ac.businesscenter.dao.RezervacijaDao;
import rs.ac.businesscenter.dao.ResourcesManager;
import rs.ac.businesscenter.data.Korisnik;
import rs.ac.businesscenter.data.Rezervacija;
import rs.ac.businesscenter.data.RezervacionaSerija;
import rs.ac.businesscenter.data.Resurs;
import rs.ac.businesscenter.data.SlobodanTermin;
import rs.ac.businesscenter.exception.BusinessCenterException;
import rs.ac.businesscenter.util.Validator;

/**
 *
 * @author stefj
 */

public class RezervacijaService {
    private static final RezervacijaService instance = new RezervacijaService();
    private final RezervacijaDao rezervacijaDao = RezervacijaDao.getInstance();
    private final ResursService resursService = ResursService.getInstance();
    private final KorisnikService korisnikService = KorisnikService.getInstance();
    
    private RezervacijaService() {}
    
    public static RezervacijaService getInstance() {
        return instance;
    }
    
    public List<Rezervacija> findActiveByKorisnikId(int korisnikId) throws BusinessCenterException {
        try (Connection con = ResourcesManager.getConnection()) {
            return rezervacijaDao.findActiveByKorisnikId(korisnikId, con);
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška prilikom preuzimanja rezervacija", ex);
        }
    }
    
    public Rezervacija createRezervacija(Korisnik korisnik, Resurs resurs, Date pocetak, Date kraj) throws BusinessCenterException {
        Validator.validateRezervacija(pocetak, kraj, resurs.getId());
        
        try (Connection con = ResourcesManager.getConnection()) {
            try {
                con.setAutoCommit(false);
                
                //NAPLATA: Skini novac pre nego što rezervišeš
                korisnikService.procesuirajPlacanjeInternal(korisnik.getId(), resurs.getCenaPoTerminu(), con);
                
                // Provera da li se termin preklapa sa postojećim
                if (!rezervacijaDao.isTerminSlobodan(resurs.getId(), pocetak, kraj, con)) {
                    throw new BusinessCenterException("Termin je već zauzet");
                }
                
                Rezervacija rezervacija = new Rezervacija();
                rezervacija.setKorisnik(korisnik);
                rezervacija.setResurs(resurs);
                rezervacija.setDatumPocetka(pocetak);
                rezervacija.setDatumKraja(kraj);
                rezervacija.setStatus("AKTIVNA");
                
                rezervacija.setCenaTransakcije(resurs.getCenaPoTerminu());
                
                rezervacijaDao.insert(rezervacija, con);
                con.commit();
                
                return rezervacija;
            } catch (Exception ex) {
                con.rollback(); // PONIŠTAVA SVE: Vraća pare ako insert ne uspe
                if (ex instanceof BusinessCenterException) {
                    throw (BusinessCenterException) ex;
                }
                throw new BusinessCenterException("Greška prilikom kreiranja rezervacije: " + ex.getMessage());
            }
            } catch (SQLException ex) {
                throw new BusinessCenterException("Greška sa bazom podataka.");
            }
        }
    
    public void cancelRezervacija(int rezervacijaId, int korisnikId) throws BusinessCenterException {
        try (Connection con = ResourcesManager.getConnection()) {
            try {
                con.setAutoCommit(false);
                
                Rezervacija rezervacija = getRezervacijaByIdInternal(rezervacijaId, con);
                if (rezervacija.getKorisnik().getId() != korisnikId) {
                    throw new BusinessCenterException("Nemate pravo da otkazujete tuđe rezervacije");
                }
                if (!"AKTIVNA".equals(rezervacija.getStatus())) {
                    throw new BusinessCenterException("Rezervacija je već otkazana");
                }
                
                double iznosPovrata = rezervacija.getCenaTransakcije();
                korisnikService.refundirajPlacanjeInternal(korisnikId, iznosPovrata, con);
                
                rezervacijaDao.cancel(rezervacijaId, con);
                con.commit();
            } catch (Exception ex) {
                con.rollback();
                if (ex instanceof BusinessCenterException) {
                    throw (BusinessCenterException) ex;
                }
                throw new BusinessCenterException("Greška prilikom otkazivanja: " + ex.getMessage());
            }
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška u komunikaciji sa bazom.");
        }
    }

    //Izračunava slobodne termine u okviru radnog vremena resursa za zadati datum.
    
    public List<SlobodanTermin> getSlobodniTermini(int resursId, Date datum) throws BusinessCenterException {
        try (Connection con = ResourcesManager.getConnection()) {
            Resurs resurs = resursService.findById(resursId);
            if (resurs == null) throw new BusinessCenterException("Resurs nije pronađen");
            
            //Postavljanje vremenskog okvira za bazu (ceo dan)
            Calendar cal = Calendar.getInstance();
            cal.setTime(datum);
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0);
            Date pocetakDana = cal.getTime();
            cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59);
            Date krajDana = cal.getTime();
            
            //Dobavljanje zauzetih termina sortiranih po vremenu početka
            List<Rezervacija> zauzeti = rezervacijaDao.findByResursAndDatum(resursId, pocetakDana, krajDana, con);
            zauzeti.sort(Comparator.comparing(Rezervacija::getDatumPocetka));
            
            //Postavljanje radnog vremena za taj specifičan dan
            Date radnoPocetak = mergeDateAndTime(datum, resurs.getRadnoVremePocetak());
            Date radnoKraj = mergeDateAndTime(datum, resurs.getRadnoVremeKraj());
            
            List<SlobodanTermin> slobodni = new ArrayList<>();
            Date trenutniMarker = radnoPocetak;
            Date sada = new Date();

            //Algoritam za pronalaženje praznina (gaps)
            for (Rezervacija rez : zauzeti) {
                if (rez.getDatumPocetka().after(trenutniMarker)) {
                    dodajSlobodanTermin(slobodni, trenutniMarker, rez.getDatumPocetka(), sada);
                }
                // Pomeramo marker na kraj trenutne rezervacije ako je on kasniji od markera
                if (rez.getDatumKraja().after(trenutniMarker)) {
                    trenutniMarker = rez.getDatumKraja();
                }
            }

            //provera preostalog vremena do kraja radnog vremena
            if (trenutniMarker.before(radnoKraj)) {
                dodajSlobodanTermin(slobodni, trenutniMarker, radnoKraj, sada);
            }

            return slobodni;
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška prilikom dobavljanja slobodnih termina", ex);
        }
    }

    private void dodajSlobodanTermin(List<SlobodanTermin> lista, Date pocetak, Date kraj, Date sada) {
        // Dodajemo samo ako je termin u budućnosti i traje barem 1 minut
        if (kraj.after(pocetak) && kraj.after(sada)) {
            Date stvaranPocetak = pocetak.before(sada) ? sada : pocetak;
            if (kraj.getTime() - stvaranPocetak.getTime() > 60000) {
                lista.add(new SlobodanTermin(stvaranPocetak, kraj));
            }
        }
    }

    private Date mergeDateAndTime(Date datePart, Date timePart) {
        Calendar base = Calendar.getInstance();
        base.setTime(datePart);
        Calendar time = Calendar.getInstance();
        time.setTime(timePart);
        
        base.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
        base.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
        base.set(Calendar.SECOND, 0);
        base.set(Calendar.MILLISECOND, 0);
        return base.getTime();
    }

    public Rezervacija getRezervacijaById(int id) throws BusinessCenterException {
        try (Connection con = ResourcesManager.getConnection()) {
            return getRezervacijaByIdInternal(id, con);
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška prilikom preuzimanja rezervacije", ex);
        }
    }

    public void azurirajRezervaciju(Rezervacija r) throws BusinessCenterException {
    try (Connection con = ResourcesManager.getConnection()) {
        con.setAutoCommit(false);
        try {
            // 1. Proveri da li je novi termin slobodan (ignorišući samu sebe)
            if (!rezervacijaDao.isTerminSlobodanZaUpdate(r.getResurs().getId(), r.getDatumPocetka(), r.getDatumKraja(), r.getId(), con)) {
                throw new BusinessCenterException("Izabrani termin je u međuvremenu zauzet.");
            }

            // 2. Izvrši update
            rezervacijaDao.update(r, con);
            
            con.commit();
        } catch (SQLException ex) {
            con.rollback();
            throw new BusinessCenterException("Greška pri čuvanju izmena: " + ex.getMessage());
        }
    } catch (SQLException ex) {
        throw new BusinessCenterException("Greška sa konekcijom: " + ex.getMessage());
    }
}
    
    private Rezervacija getRezervacijaByIdInternal(int id, Connection con) throws SQLException, BusinessCenterException {
        String sql = "SELECT r.*, k.username AS korisnik_username, k.ime AS korisnik_ime, "
            + "res.naziv AS resurs_naziv, res.tip AS resurs_tip, res.cena_po_terminu AS resurs_cena, "
            + "s.id AS serija_id, s.frekvencija AS serija_frekvencija "
            + "FROM rezervacija r "
            + "JOIN korisnik k ON r.korisnik_id = k.id "
            + "JOIN resurs res ON r.resurs_id = res.id "
            + "LEFT JOIN rezervaciona_serija s ON r.serija_id = s.id "
            + "WHERE r.id = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new BusinessCenterException("Rezervacija sa ID " + id + " nije pronađena");
                }

                Rezervacija rezervacija = new Rezervacija();
                rezervacija.setId(rs.getInt("id"));
                rezervacija.setDatumPocetka(rs.getTimestamp("datum_pocetka"));
                rezervacija.setDatumKraja(rs.getTimestamp("datum_kraja"));
                rezervacija.setStatus(rs.getString("status"));
                rezervacija.setCenaTransakcije(rs.getDouble("cena_transakcije"));

                Korisnik korisnik = new Korisnik();
                korisnik.setId(rs.getInt("korisnik_id"));
                korisnik.setUsername(rs.getString("korisnik_username"));
                korisnik.setIme(rs.getString("korisnik_ime"));
                rezervacija.setKorisnik(korisnik);

                Resurs resurs = new Resurs();
                resurs.setId(rs.getInt("resurs_id"));
                resurs.setNaziv(rs.getString("resurs_naziv"));
                resurs.setTip(rs.getString("resurs_tip"));
                resurs.setCenaPoTerminu(rs.getDouble("resurs_cena")); 
                rezervacija.setResurs(resurs);

                if (rs.getObject("serija_id") != null) {
                    RezervacionaSerija serija = new RezervacionaSerija();
                    serija.setId(rs.getInt("serija_id"));
                    serija.setFrekvencija(rs.getString("serija_frekvencija"));
                    rezervacija.setSerija(serija);
                }
                return rezervacija;
            }
        }
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import rs.ac.businesscenter.dao.ResursDao;
import rs.ac.businesscenter.dao.ResourcesManager;
import rs.ac.businesscenter.data.Resurs;
import rs.ac.businesscenter.exception.BusinessCenterException;
import java.util.Calendar;
import java.util.Date;
import rs.ac.businesscenter.dao.RezervacijaDao;
import rs.ac.businesscenter.data.Rezervacija;
import rs.ac.businesscenter.data.SlobodanTermin;

/**
 *
 * @author stefj
 */

public class ResursService {
    private static final ResursService instance = new ResursService();
    private final ResursDao resursDao = ResursDao.getInstance();
    private final RezervacijaDao rezervacijaDao = RezervacijaDao.getInstance();
    
    private ResursService() {}
    
    public static ResursService getInstance() {
        return instance;
    }
    
    public List<Resurs> findAll() throws BusinessCenterException {
        // Try-with-resources garantuje zatvaranje konekcije
        try (Connection con = ResourcesManager.getConnection()) {
            return resursDao.findAll(con);
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška prilikom preuzimanja liste resursa", ex);
        }
    }
    
    public Resurs findById(int id) throws BusinessCenterException {
        try (Connection con = ResourcesManager.getConnection()) {
            return findByIdInternal(id, con);
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška prilikom preuzimanja resursa", ex);
        }
    }

    /**
     * Interna metoda koja omogućava ponovnu upotrebu postojeće konekcije 
     * unutar jedne transakcije.
     */
    public Resurs findByIdInternal(int id, Connection con) throws SQLException, BusinessCenterException {
        Resurs resurs = resursDao.findById(id, con);
        if (resurs == null) {
            throw new BusinessCenterException("Resurs sa ID " + id + " nije pronađen");
        }
        return resurs;
    }
    
    public void addResurs(Resurs resurs) throws BusinessCenterException {
        // Validacija pre otvaranja konekcije (štedi resurse)
        validateResursData(resurs);

        try (Connection con = ResourcesManager.getConnection()) {
            try {
                con.setAutoCommit(false);
                
                resursDao.insert(resurs, con);
                
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new BusinessCenterException("Greška prilikom dodavanja resursa u bazu", ex);
        }
    }

    private void validateResursData(Resurs resurs) throws BusinessCenterException {
        if (resurs.getNaziv() == null || resurs.getNaziv().trim().isEmpty()) {
            throw new BusinessCenterException("Naziv resursa je obavezan");
        }
        if (resurs.getTip() == null || resurs.getTip().trim().isEmpty()) {
            throw new BusinessCenterException("Tip resursa je obavezan");
        }
        if (resurs.getRadnoVremePocetak() == null || resurs.getRadnoVremeKraj() == null) {
            throw new BusinessCenterException("Radno vreme je obavezno");
        }
        // Dodatna provera logike radnog vremena
        if (resurs.getRadnoVremePocetak().after(resurs.getRadnoVremeKraj())) {
            throw new BusinessCenterException("Vreme početka ne može biti nakon vremena završetka");
        }
    }
    

public List<SlobodanTermin> getFreeSlots(int resursId, Date datum) throws BusinessCenterException {
    try (Connection con = ResourcesManager.getConnection()) {
        Resurs resurs = findByIdInternal(resursId, con);
        
        // Definišemo opseg pretrage (ceo taj dan)
        Calendar cal = Calendar.getInstance();
        cal.setTime(datum);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        Date pocetakDana = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        Date krajDana = cal.getTime();

        // Uzimamo zauzete termine za taj dan
        List<Rezervacija> zauzeti = rezervacijaDao.findByResursAndDatum(resursId, pocetakDana, krajDana, con);

        List<SlobodanTermin> slobodni = new ArrayList<>();
        
        // Radno vreme resursa za taj dan
        Calendar radnoPocetak = Calendar.getInstance();
        radnoPocetak.setTime(datum);
        Calendar radnoVremePocetakResursa = Calendar.getInstance();
        radnoVremePocetakResursa.setTime(resurs.getRadnoVremePocetak());
        radnoPocetak.set(Calendar.HOUR_OF_DAY, radnoVremePocetakResursa.get(Calendar.HOUR_OF_DAY));
        radnoPocetak.set(Calendar.MINUTE, radnoVremePocetakResursa.get(Calendar.MINUTE));

        Calendar radnoKraj = Calendar.getInstance();
        radnoKraj.setTime(datum);
        Calendar radnoVremeKrajResursa = Calendar.getInstance();
        radnoVremeKrajResursa.setTime(resurs.getRadnoVremeKraj());
        radnoKraj.set(Calendar.HOUR_OF_DAY, radnoVremeKrajResursa.get(Calendar.HOUR_OF_DAY));
        radnoKraj.set(Calendar.MINUTE, radnoVremeKrajResursa.get(Calendar.MINUTE));

        Date trenutno = radnoPocetak.getTime();

        for (Rezervacija rez : zauzeti) {
            if (rez.getDatumPocetka().after(trenutno)) {
                // Imamo rupu između 'trenutno' i početka ove rezervacije
                slobodni.add(new SlobodanTermin(trenutno, rez.getDatumPocetka()));
            }
            if (rez.getDatumKraja().after(trenutno)) {
                trenutno = rez.getDatumKraja();
            }
        }

        // Provera nakon poslednje rezervacije do kraja radnog vremena
        if (trenutno.before(radnoKraj.getTime())) {
            slobodni.add(new SlobodanTermin(trenutno, radnoKraj.getTime()));
        }

        return slobodni;
    } catch (SQLException ex) {
        throw new BusinessCenterException("Greška pri dobavljanju slobodnih termina", ex);
    }
}
}
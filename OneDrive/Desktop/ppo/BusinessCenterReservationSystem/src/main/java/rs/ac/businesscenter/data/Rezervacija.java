/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.data;

/**
 *
 * @author stefj
 */
import jakarta.json.bind.annotation.JsonbTransient;
import java.io.Serializable;
import java.util.Date;

public class Rezervacija implements Serializable {
    private int id;
    @JsonbTransient
    private Korisnik korisnik;
    private Resurs resurs;
    private Date datumPocetka;
    private Date datumKraja;
    private String status; // AKTIVNA, OBRISANA, ZAVRSENA
    @JsonbTransient
    private RezervacionaSerija serija;
    private double cenaTransakcije;
    
    public Rezervacija() {
    }

    public Rezervacija(int id, Korisnik korisnik, Resurs resurs, Date datumPocetka, Date datumKraja, String status, RezervacionaSerija serija, double cenaTransakcije) {
        this.id = id;
        this.korisnik = korisnik;
        this.resurs = resurs;
        this.datumPocetka = datumPocetka;
        this.datumKraja = datumKraja;
        this.status = status;
        this.serija = serija;
        this.cenaTransakcije = cenaTransakcije;
    }

    

    public Rezervacija(Korisnik korisnik, Resurs resurs, Date datumPocetka, Date datumKraja) {
        this.korisnik = korisnik;
        this.resurs = resurs;
        this.datumPocetka = datumPocetka;
        this.datumKraja = datumKraja;
        this.status = "AKTIVNA";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Korisnik getKorisnik() {
        return korisnik;
    }

    public void setKorisnik(Korisnik korisnik) {
        this.korisnik = korisnik;
    }

    public Resurs getResurs() {
        return resurs;
    }

    public void setResurs(Resurs resurs) {
        this.resurs = resurs;
    }

    public Date getDatumPocetka() {
        return datumPocetka;
    }

    public void setDatumPocetka(Date datumPocetka) {
        this.datumPocetka = datumPocetka;
    }

    public Date getDatumKraja() {
        return datumKraja;
    }

    public void setDatumKraja(Date datumKraja) {
        this.datumKraja = datumKraja;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RezervacionaSerija getSerija() {
        return serija;
    }

    public void setSerija(RezervacionaSerija serija) {
        this.serija = serija;
    }

    public double getCenaTransakcije() {
        return cenaTransakcije;
    }

    public void setCenaTransakcije(double cenaTransakcije) {
        this.cenaTransakcije = cenaTransakcije;
    }
    
    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Rezervacija{");
        sb.append("id=").append(id);
        sb.append(", korisnik=").append(korisnik != null ? korisnik.getUsername() : "null");
        sb.append(", resurs=").append(resurs != null ? resurs.getNaziv() : "null");
        sb.append(", datumPocetka=").append(datumPocetka);
        sb.append(", datumKraja=").append(datumKraja);
        sb.append(", status=").append(status);
        sb.append(", cenaTransakcije=").append(cenaTransakcije);
        sb.append(", serija=").append(serija != null ? serija.getId() : "null");
        sb.append('}');
        return sb.toString();
    }
    
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.data;

/**
 *
 * @author stefj
 */
import java.io.Serializable;
import java.util.Date;
import jakarta.json.bind.annotation.JsonbDateFormat;

public class RezervacionaSerija implements Serializable {
    private int id;
    private Korisnik korisnik;
    private Resurs resurs;
    private String frekvencija; // DNEVNO, RADNI_DANI, NEDELJNO, MESEČNO
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private Date datumPocetka;
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private Date datumKraja;
    
    public RezervacionaSerija() {
    }

    public RezervacionaSerija(int id, Korisnik korisnik, Resurs resurs, String frekvencija, Date datumPocetka, Date datumKraja) {
        this.id = id;
        this.korisnik = korisnik;
        this.resurs = resurs;
        this.frekvencija = frekvencija;
        this.datumPocetka = datumPocetka;
        this.datumKraja = datumKraja;
    }

    public RezervacionaSerija(Korisnik korisnik, Resurs resurs, String frekvencija, Date datumPocetka, Date datumKraja) {
        this.korisnik = korisnik;
        this.resurs = resurs;
        this.frekvencija = frekvencija;
        this.datumPocetka = datumPocetka;
        this.datumKraja = datumKraja;
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

    public String getFrekvencija() {
        return frekvencija;
    }

    public void setFrekvencija(String frekvencija) {
        this.frekvencija = frekvencija;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RezervacionaSerija{");
        sb.append("id=").append(id);
        sb.append(", korisnik=").append(korisnik != null ? korisnik.getUsername() : "null");
        sb.append(", resurs=").append(resurs != null ? resurs.getNaziv() : "null");
        sb.append(", frekvencija=").append(frekvencija);
        sb.append(", datumPocetka=").append(datumPocetka);
        sb.append(", datumKraja=").append(datumKraja);
        sb.append('}');
        return sb.toString();
    }
    
}
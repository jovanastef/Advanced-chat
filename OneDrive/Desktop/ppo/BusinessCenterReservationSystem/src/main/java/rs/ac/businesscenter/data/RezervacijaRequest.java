/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.data;

/**
 *
 * @author stefj
 */
import java.util.Date;
import jakarta.json.bind.annotation.JsonbDateFormat;

public class RezervacijaRequest {
    private int resursId;
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private Date datumPocetka;
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private Date datumKraja;
    private String frekvencija; // Za ponavljajuće rezervacije
    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    private Date doDatuma;     // Za ponavljajuće rezervacije
    
    public RezervacijaRequest() {
    }
    
    public int getResursId() {
        return resursId;
    }
    
    public void setResursId(int resursId) {
        this.resursId = resursId;
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
    
    public String getFrekvencija() {
        return frekvencija;
    }
    
    public void setFrekvencija(String frekvencija) {
        this.frekvencija = frekvencija;
    }
    
    public Date getDoDatuma() {
        return doDatuma;
    }
    
    public void setDoDatuma(Date doDatuma) {
        this.doDatuma = doDatuma;
    }
}

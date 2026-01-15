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
import java.sql.Time;
import jakarta.json.bind.annotation.JsonbDateFormat;

public class Resurs implements Serializable {
    private int id;
    private String naziv;
    private String tip;
    @JsonbDateFormat("HH:mm:ss")
    private Time radnoVremePocetak;
    @JsonbDateFormat("HH:mm:ss")
    private Time radnoVremeKraj;
    private double cenaPoTerminu;
    
    public Resurs() {
    }

    public Resurs(int id, String naziv, String tip, Time radnoVremePocetak, Time radnoVremeKraj, double cenaPoTerminu) {
        this.id = id;
        this.naziv = naziv;
        this.tip = tip;
        this.radnoVremePocetak = radnoVremePocetak;
        this.radnoVremeKraj = radnoVremeKraj;
        this.cenaPoTerminu = cenaPoTerminu;
    }

    public Resurs(String naziv, String tip, Time radnoVremePocetak, Time radnoVremeKraj) {
        this.naziv = naziv;
        this.tip = tip;
        this.radnoVremePocetak = radnoVremePocetak;
        this.radnoVremeKraj = radnoVremeKraj;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public Time getRadnoVremePocetak() {
        return radnoVremePocetak;
    }

    public void setRadnoVremePocetak(Time radnoVremePocetak) {
        this.radnoVremePocetak = radnoVremePocetak;
    }

    public Time getRadnoVremeKraj() {
        return radnoVremeKraj;
    }

    public void setRadnoVremeKraj(Time radnoVremeKraj) {
        this.radnoVremeKraj = radnoVremeKraj;
    }

    public double getCenaPoTerminu() {
        return cenaPoTerminu;
    }

    public void setCenaPoTerminu(double cenaPoTerminu) {
        this.cenaPoTerminu = cenaPoTerminu;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Resurs{");
        sb.append("id=").append(id);
        sb.append(", naziv=").append(naziv);
        sb.append(", tip=").append(tip);
        sb.append(", radnoVremePocetak=").append(radnoVremePocetak);
        sb.append(", radnoVremeKraj=").append(radnoVremeKraj);
        sb.append(", cenaPoTerminu=").append(cenaPoTerminu);
        sb.append('}');
        return sb.toString();
    }
}    


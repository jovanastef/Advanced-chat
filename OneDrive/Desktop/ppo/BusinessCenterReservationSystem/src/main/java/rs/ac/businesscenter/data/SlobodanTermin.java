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

public class SlobodanTermin {
    private Date pocetak;
    private Date kraj;
    
    public SlobodanTermin(Date pocetak, Date kraj) {
        this.pocetak = pocetak;
        this.kraj = kraj;
    }
    
    public Date getPocetak() { return pocetak; }
    public void setPocetak(Date pocetak) { this.pocetak = pocetak; }
    public Date getKraj() { return kraj; }
    public void setKraj(Date kraj) { this.kraj = kraj; }
    
    @Override
    public String toString() {
        return "SlobodanTermin{" +
               "pocetak=" + pocetak +
               ", kraj=" + kraj +
               '}';
    }
}


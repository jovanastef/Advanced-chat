/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.exception;

/**
 *
 * @author stefj
 */
import java.io.Serializable;
import java.util.Date;

public class SlobodanTermin implements Serializable {
    private Date pocetak;
    private Date kraj;
    private String formatPocetak;
    private String formatKraj;
    
    public SlobodanTermin() {}

    public SlobodanTermin(Date pocetak, Date kraj, String formatPocetak, String formatKraj) {
        this.pocetak = pocetak;
        this.kraj = kraj;
        this.formatPocetak = formatPocetak;
        this.formatKraj = formatKraj;
    }
    
    private String formatirajVreme(Date datum) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
        return sdf.format(datum);
    }

    public Date getPocetak() {
        return pocetak;
    }

    public void setPocetak(Date pocetak) {
        this.pocetak = pocetak;
    }

    public Date getKraj() {
        return kraj;
    }

    public void setKraj(Date kraj) {
        this.kraj = kraj;
    }

    public String getFormatPocetak() {
        return formatPocetak;
    }

    public String getFormatKraj() {
        return formatKraj;
    }

    @Override
    public String toString() {
        return "SlobodanTermin{" + 
                "pocetak=" + pocetak + 
                ", kraj=" + kraj + 
                ", formatPocetak=" + formatPocetak + '\'' +
                ", formatKraj=" + formatKraj + '\'' +
                '}';
    }
    
    
}

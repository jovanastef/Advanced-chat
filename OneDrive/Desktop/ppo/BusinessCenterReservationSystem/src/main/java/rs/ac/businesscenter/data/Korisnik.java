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

public class Korisnik implements Serializable {
    private int id;
    private String username;
    private String password;
    private String ime;
    private String email;
    private double novac;
    
    public Korisnik() {
    }

    public Korisnik(int id, String username, String password, String ime, String email, double novac) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.ime = ime;
        this.email = email;
        this.novac = novac;
    }

    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getNovac() {
        return novac;
    }

    public void setNovac(double novac) {
        this.novac = novac;
    }

    @Override
    public String toString() {
        return "Korisnik{" + "id=" + id + ", username=" + username + ", password=" + password + ", ime=" + ime + ", email=" + email + ", novac=" + novac + '}';
    }
    
    
}

    

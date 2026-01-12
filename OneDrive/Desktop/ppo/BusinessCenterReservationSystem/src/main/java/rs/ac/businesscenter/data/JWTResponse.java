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

public class JWTResponse implements Serializable {
    private String token;
    private String username;
    private String ime;
    private int id;
    
    public JWTResponse() {
    }

    public JWTResponse(String token, String username, String ime, int id) {
        this.token = token;
        this.username = username;
        this.ime = ime;
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "JWTResponse{" + 
                "token=" + (token != null ? token.substring(0, Math.min(10, token.length())) + "..." : "null") + 
                ", username=" + username + 
                ", ime=" + ime + 
                ", id=" + id + '}';
    }
} 
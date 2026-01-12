/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rs.ac.businesscenter.exception;

/**
 *
 * @author stefj
 */
public class BusinessCenterException extends Exception {
    private final String errorCode;
    
    public BusinessCenterException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
    }
    
    public BusinessCenterException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "GENERAL_ERROR";
    }
    
    public BusinessCenterException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessCenterException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

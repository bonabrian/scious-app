package id.bonabrian.scious.source.dao;

/**
 * @author Bona Brian Siagian (bonabrian)
 */

public class BaseModel {
    private int status;
    private String message;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

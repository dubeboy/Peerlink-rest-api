package za.co.dubedivine.networks.model.responseEntity;

public class StatusResponseEntity {
    private boolean status;
    private String message;

    public StatusResponseEntity(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

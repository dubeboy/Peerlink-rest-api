package za.co.dubedivine.networks.model.responseEntity;

//standard error msg class
public class StatusResponseEntity<T> {
    private boolean status;
    private String message;
    private T entity = null;

    public StatusResponseEntity(boolean status, String message, T entity) {
        this.status = status;
        this.message = message;
        this.entity = entity;
    }

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

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }
}

package za.co.dubedivine.networks.model;

import java.util.Date;

/**
 * Created by divine on 2017/08/13.
 */
public class Docs {
    private String name;
    private int size;
    private String type;
    private String location;
    private Date createAt = new Date();


    public Docs() {

    }

    public Docs(String name, int size, String type, String location) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public int getLimit() {
        return 5120; // 5 mb
    }

    public Date getCreateAt() {
        return createAt;
    }
}


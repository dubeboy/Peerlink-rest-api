package za.co.dubedivine.networks.model;

import java.util.Date;

/**
 * Created by divine on 2017/08/13.
 */
public class Video {
    private String name;
    private int size;
    private String type;
    private String location;
    private int limit = 5 * 1024; //5 mb
    private Date createAt = new Date();
}

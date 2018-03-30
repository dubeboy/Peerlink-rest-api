package za.co.dubedivine.networks.model;



import org.springframework.data.annotation.Id;

import java.util.Date;

/**
 * Created by divine on 2017/08/13.
 */
@org.springframework.data.elasticsearch.annotations.Document(indexName = "divine", type = "questions")
public class Media {
    @Id
    private String name;
    private long size;
    private char type;
    private String location;
    private Date createAt = new Date();

    public static final char VIDEO_TYPE = 'V';
    public static final char PICTURE_TYPE = 'P';
    public static final char DOCS_TYPE = 'D';

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public char getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }

    public int getLimit() {
        return 5120;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public Media(String name, long size, char type, String location) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.location = location;
    }

    public Media() {
    }
}

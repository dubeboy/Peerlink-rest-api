package za.co.dubedivine.networks.model;



import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;

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

    public char getType() {
        return type;
    }

    public String getLocation() {
        return location;
    }



    public Media(String name, char type, String location) {
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public Media() {
    }

    @Override
    public String toString() {
        return "Media{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", type=" + type +
                ", location='" + location + '\'' +
                ", createAt=" + createAt +
                '}';
    }
}

package za.co.dubedivine.networks.model;

import com.querydsl.core.annotations.QueryEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * Created by divine on 2017/08/13.
 */

/*
* TOP AGGREGATE CLASS DWAG
* */


//@QueryEntity what is this
public class Tag {
    private String name;
    private Date createAt = new Date();

    public Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Date getCreateAt() {
        return createAt;
    }
}

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
@Document
public class Tag {
    @Id
    private String id;
    private String name;
    @Indexed
    private List<Question> questions;
    private Date createAt = new Date();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Date getCreateAt() {
        return createAt;
    }
}

package za.co.dubedivine.networks.model;

import kotlin.jvm.internal.Intrinsics;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

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
    @Indexed
    private String name;
    private Date createAt = new Date();
    private Set<String> questionIds;

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

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Set<String> getQuestionIds() {
        return questionIds;
    }

    public void setQuestionIds(Set<String> tagIds) {
        this.questionIds = tagIds;
    }

    public void addQuestionIdToTag(String id) {
            this.questionIds.add(id);
    }
}

package za.co.dubedivine.networks.model.shared;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import za.co.dubedivine.networks.model.Tag;

import java.util.Date;
import java.util.Set;

public abstract class TagBase {
    @Id
    private String id;
    @Indexed
    private String name;
    private Date createAt = new Date();
    private Set<String> questionIds;

    public TagBase(String name) {
        this.name = name;
    }

    public TagBase() {

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

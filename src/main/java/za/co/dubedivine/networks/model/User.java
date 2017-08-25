package za.co.dubedivine.networks.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
public class User {
    @Id
    private String id;  // initialised by my faithful mongodb!!
    @Indexed
    private String name;
    private String email;
    @Indexed
    private List<Tag> tags;

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public User(String name, String email, List<Tag> tags) {
        this.name = name;
        this.email = email;
        this.tags = tags;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<Tag> getTags() {
        return tags;
    }
}

package za.co.dubedivine.networks.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Document
public class User {
    @Id
    private String id;  // initialised by my faithful mongodb!!
    @Indexed
    private String name;
    private String email;
    @Indexed
    private String nickname;
    @JsonProperty("photo_url")
    private String photoUrl;
    private String degree;
    private Set<String> modules;
    @Indexed
    private List<Tag> tags = new ArrayList<>();
    private String fcmToken;

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    //required for the jackson!!
    public User() {

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

    public String getNickname() {
        return nickname;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getDegree() {
        return degree;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public Set<String> getModules() {
        return modules;
    }

    public void setModules(Set<String> modules) {
        this.modules = modules;
    }

    public void addTag(Tag tag) {
        tags.add(tag);
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}

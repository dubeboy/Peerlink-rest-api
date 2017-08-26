package za.co.dubedivine.networks.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by divine on 2017/08/13.
 */

@Document
public class Question {
    @Id
    private String id;
    //    @Indexed  //todo: should somehow be able to search fast with title and body
    @TextIndexed
    private String title;
    @TextIndexed
    private String body;
    private long votes;
    private List<Comment> comments;
    private List<Answer> answers;
    @Indexed
    private List<Tag> tags;  //todo: bad it should be mapping!!
    private User user; // the user the
    private String type;
    private Media video;
    private List<Media> files; //this can be combined with video dwag
    private Date createdAt = new Date();


    public Question(String title, String body, long votes, List<Tag> tags, String type) {
        this.title = title;
        this.body = body;
        this.votes = votes;
        this.tags = tags;
        this.type = type;
    }


    public Question() { // for jackson man

    }
    //for video we will add another constructor which has video here

    public String getId() {
        return id;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public long getVotes() {
        return votes;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public Media getVideo() {
        return video;
    }

    public String getType() {
        return type;
    }

    public List<Media> getFiles() {
        return files;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public void setVideo(Media video) {
        this.video = video;
    }

    public void setFiles(List<Media> files) {
        this.files = files;
    }
}

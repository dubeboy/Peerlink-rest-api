package za.co.dubedivine.networks.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private String title;
    private String body;
    private long votes;
    private List<Comment> comments;
    private List<Answer> answers;
    @Indexed
    private List<Tag> tags;

    private Video video;
    private String type;
    private List<Docs> docs;
    private Date createAt = new Date();


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

    public Video getVideo() {
        return video;
    }

    public String getType() {
        return type;
    }

    public List<Docs> getDocs() {
        return docs;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public void setDocs(List<Docs> docs) {
        this.docs = docs;
    }
}

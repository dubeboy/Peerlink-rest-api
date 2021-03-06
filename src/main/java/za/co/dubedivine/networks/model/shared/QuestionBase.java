package za.co.dubedivine.networks.model.shared;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import za.co.dubedivine.networks.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


//this should hae probably been an interface
public abstract class QuestionBase {

    protected @Id
    String id;  // protected because it has a setter in the Elastic question child class
    private @Indexed
    String title;
    private String body;
    private long votes;
    @Field(type = FieldType.Nested)
    private ArrayList<Comment> comments = new ArrayList<>();
    private ArrayList<Answer> answers = new ArrayList<>();
    @Indexed
    @Field(type = FieldType.Nested)
    private List<Tag> tags;  //todo: bad it should be mapping!!
    private User user; // the user the
    private String type;
    private @Field(type = FieldType.Nested)
    Media video;
    private @Field(type = FieldType.Nested)
    List<Media> files; //this can be combined with video dwag
    private @Field(type = FieldType.Date)
    Date createdAt = new Date();
    private boolean answered = false;


    // satisfy jackson
    public QuestionBase() {}


    public QuestionBase(String title, String body, long votes, List<Tag> tags, String type) {
        this.title = title;
        this.body = body;
        this.votes = votes;
        this.tags = tags;
        this.type = type;
    }


    public QuestionBase(String title, String body, long votes, List<Tag> tags, String type, Media video) {
        this.title = title;
        this.body = body;
        this.votes = votes;
        this.tags = tags;
        this.type = type;
        this.video = video;
    }

    public QuestionBase(String title, String body, long votes, List<Tag> tags, String type, List<Media> files) {
        this.title = title;
        this.body = body;
        this.votes = votes;
        this.tags = tags;
        this.type = type;
        this.files = files;
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

    public void setVotes(long votes) {
        this.votes = votes;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public ArrayList<Answer> getAnswers() {
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

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public void setAnswers(ArrayList<Answer> answers) {
        this.answers = answers;
    }

    public void setVideo(Media video) {
        this.video = video;
    }

    public void setFiles(List<Media> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "QuestionBase{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", votes=" + votes +
                ", comments=" + comments +
                ", answers=" + answers +
                ", tags=" + tags +
                ", user=" + user +
                ", type='" + type + '\'' +
                ", video=" + video +
                ", files=" + files +
                ", createdAt=" + createdAt +
                '}';
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }
}

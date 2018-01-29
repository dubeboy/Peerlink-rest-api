package za.co.dubedivine.networks.model.shared;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import za.co.dubedivine.networks.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


//this should hae probably been an interface
public abstract class QuestionBase {

    protected @Id String id;  // protected because it has a setter in the Elastic question child class
    private @Indexed  String title;
    private String body;
    private long votes;
    @Field(type = FieldType.Nested)
    private ArrayList<Comment> comments;
    private ArrayList<Answer> answers;
    @Indexed
    @Field(type = FieldType.Nested)
    private List<Tag> tags;  //todo: bad it should be mapping!!
    private User user; // the user the
    private String type;
    private @Field(type = FieldType.Nested) Media video;
    private List<Media> files; //this can be combined with video dwag
    private @Field(type= FieldType.Date) Date createdAt = new Date();

    public QuestionBase() {

    }


    public QuestionBase(String title, String body, long votes, List<Tag> tags, String type) {
        this.title = title;
        this.body = body;
        this.votes = votes;
        this.tags = tags;
        this.type = type;
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
}

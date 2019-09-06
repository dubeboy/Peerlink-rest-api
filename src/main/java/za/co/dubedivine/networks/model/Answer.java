package za.co.dubedivine.networks.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by divine on 2017/08/13.
 */
@Document
public class Answer  {
    private String body;
    private long votes;


    private boolean isChosen = false;
    private Date createAt = new Date();
    private ArrayList<Comment> comments = new ArrayList<>();
    private Media video;
    private String id = UUID.randomUUID().toString();
    private List<Media> files;
    private User user;

    public Answer() {}

    public Answer(String body, long votes, boolean isChosen) {
        this.body = body;
        this.votes = votes;
        this.isChosen = isChosen;
    }

    public Answer(String body, long votes, boolean isChosen, ArrayList<Comment> comments, Media video) {
        this.body = body;
        this.votes = votes;
        this.isChosen = isChosen;
        this.comments = comments;
        this.video = video;
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

    public boolean isChosen() {
        return isChosen;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public Media getVideo() {
        return video;
    }
    public void setVideo(Media video) {
        this.video = video;
    }

    public String getId() {
        return id;
    }


    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "body='" + body + '\'' +
                ", votes=" + votes +
                ", isChosen=" + isChosen +
                ", createAt=" + createAt +
                ", comments=" + comments +
                ", video=" + video +
                ", id='" + id + '\'' +
                '}';
    }

    public List<Media> getFiles() {
        return files;
    }

    public void setFiles(List<Media> files) {
        this.files = files;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

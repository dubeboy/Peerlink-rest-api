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
public class Answer {
    private String body;
    private long votes;
    private boolean isChoosen;
    private Date createAt = new Date();
    private ArrayList<Comment> comments;
    private Media video;
    private String id = UUID.randomUUID().toString();

    public Answer() {
    }

    public Answer(String body, long votes, boolean isChoosen) {
        this.body = body;
        this.votes = votes;
        this.isChoosen = isChoosen;
    }

    public Answer(String body, long votes, boolean isChosen, ArrayList<Comment> comments, Media video) {
        this.body = body;
        this.votes = votes;
        this.isChoosen = isChosen;
        this.comments = comments;
        this.video = video;
    }

    public String getBody() {
        return body;
    }

    public long getVotes() {
        return votes;
    }

    public boolean isChoosen() {
        return isChoosen;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public Media getVideo() {
        return video;
    }

    public String getId() {
        return id;
    }

    // @Override
    // public String toString() {

    // }
}

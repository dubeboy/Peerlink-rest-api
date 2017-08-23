package za.co.dubedivine.networks.model;

import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

/**
 * Created by divine on 2017/08/13.
 */
public class Answer {
    private String body;
    private long votes;
    private boolean isChoosen;
    private Date createAt = new Date();
    private List<Comment> comments;
    private Video video;
    private long id = System.currentTimeMillis();

    public Answer() {
    }

    public Answer(String body, long votes, boolean isChoosen) {
        this.body = body;
        this.votes = votes;
        this.isChoosen = isChoosen;
    }

    public Answer(String body, long votes, boolean isChosen, List<Comment> comments, Video video) {
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

    public List<Comment> getComments() {
        return comments;
    }

    public Video getVideo() {
        return video;
    }

    public long getId() {
        return id;
    }
}

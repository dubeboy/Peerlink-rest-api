package za.co.dubedivine.networks.model;

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
}

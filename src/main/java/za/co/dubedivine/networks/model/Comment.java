package za.co.dubedivine.networks.model;

import java.util.Date;

/**
 * Created by divine on 2017/08/13.
 */
public class Comment {
    private String body;
    private long votes;
    private Date createdAt = new Date();

    public Comment(String body, long votes) {
        this.body = body;
        this.votes = votes;
    }

    public String getBody() {
        return body;
    }

    public long getVotes() {
        return votes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}

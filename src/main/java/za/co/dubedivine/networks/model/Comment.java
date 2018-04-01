package za.co.dubedivine.networks.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by divine on 2017/08/13.
 */
@Document
public class Comment {
    private String body;
    private long votes;
    private Date createdAt = new Date();


    public Comment() {
    }

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

    @Override
    public String toString() {
        return "Comment{" +
                "body='" + body + '\'' +
                ", votes=" + votes +
                ", createdAt=" + createdAt +
                '}';
    }
}

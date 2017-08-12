package za.co.dubedivine.networks.model;

import java.util.List;

/**
 * Created by divine on 2017/08/13.
 */
public class Question {
    private String title;
    private String body;
    private long votes;
    private List<Comment> comments;
    private List<Answer> answers;
    private Video video;
    private String type;
    private List<Docs> docs;

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
}

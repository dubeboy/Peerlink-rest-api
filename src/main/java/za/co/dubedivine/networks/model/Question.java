package za.co.dubedivine.networks.model;

import org.springframework.data.mongodb.core.mapping.Document;
import za.co.dubedivine.networks.model.shared.QuestionBase;

import java.util.List;

/**
 * Created by divine on 2017/08/13.
 */

@Document
public class Question extends QuestionBase {

   public Question() {}

    public Question(String title, String body, long votes, List<Tag> tags, String type) {
        super(title, body, votes, tags, type);
    }
}

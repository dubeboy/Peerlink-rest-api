package za.co.dubedivine.networks.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import za.co.dubedivine.networks.model.shared.QuestionBase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by divine on 2017/08/13.
 */

@Document
public class Question extends QuestionBase{

    public Question(String title, String body, long votes, List<Tag> tags, String type) {
        super(title, body, votes, tags, type);
    }
}

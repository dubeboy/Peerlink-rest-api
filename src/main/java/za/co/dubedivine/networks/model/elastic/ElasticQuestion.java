package za.co.dubedivine.networks.model.elastic;

import org.springframework.data.elasticsearch.annotations.Document;
import za.co.dubedivine.networks.model.Tag;
import za.co.dubedivine.networks.model.shared.QuestionBase;

import java.util.List;

@Document(indexName = "divine", type = "questions")
public class ElasticQuestion extends QuestionBase {


    public ElasticQuestion(String title, String body, long votes, List<Tag> tags, String type) {
        super(title, body, votes, tags, type);
    }

}

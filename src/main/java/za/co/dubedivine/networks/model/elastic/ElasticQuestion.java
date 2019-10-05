package za.co.dubedivine.networks.model.elastic;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.completion.Completion;
import za.co.dubedivine.networks.model.Tag;
import za.co.dubedivine.networks.model.shared.QuestionBase;

import java.util.List;

@JsonInclude(value = JsonInclude.Include.NON_NULL) // super handy dwag for non-null values!!!!
@Document(indexName = "peerlink", type = "question", refreshInterval = "-1")
public class ElasticQuestion extends QuestionBase {


    @CompletionField //this requires the Non Null @Json Includes
    private Completion suggest; //todo: for suggestions

    ElasticQuestion() { super(); }

    public void setId(String id) {
        super.id = id;
    }

    public ElasticQuestion(String title, String body, long votes, List<Tag> tags, String type) {
        super(title, body, votes, tags, type);
    }

    public Completion getSuggest() {
        return suggest;
    }

    public void setSuggest(Completion suggest) {
        this.suggest = suggest;
    }
}

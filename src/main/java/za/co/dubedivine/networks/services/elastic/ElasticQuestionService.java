package za.co.dubedivine.networks.services.elastic;

import za.co.dubedivine.networks.model.Question;
import za.co.dubedivine.networks.model.elastic.ElasticQuestion;
import za.co.dubedivine.networks.services.QuestionService;

import java.util.List;


public interface ElasticQuestionService {
    ElasticQuestion save(ElasticQuestion book);
    void delete(ElasticQuestion book);
    List<ElasticQuestion> findByTag(String tagName);
    List<ElasticQuestion> search(String q);
}

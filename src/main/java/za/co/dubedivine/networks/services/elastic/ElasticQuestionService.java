package za.co.dubedivine.networks.services.elastic;

import za.co.dubedivine.networks.model.elastic.ElasticQuestion;

import java.util.List;
import java.util.Set;


public interface ElasticQuestionService {
    ElasticQuestion save(ElasticQuestion book);

    void delete(ElasticQuestion book);

    List<ElasticQuestion> findByTag(String tagName);

    List<ElasticQuestion> search(String q);

    List<ElasticQuestion> findByTitleAndTagsName(String title, String tagName);
}

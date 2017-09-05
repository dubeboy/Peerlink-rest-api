package za.co.dubedivine.networks.repository.elastic;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;
import za.co.dubedivine.networks.model.elastic.ElasticQuestion;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@Repository
public interface ElasticQRepo extends ElasticsearchRepository<ElasticQuestion, String>, QueryDslPredicateExecutor<ElasticQuestion>{
    List<ElasticQuestion> findByTagsName(String tagName);

    Set<ElasticQuestion> findByTitleAndBodyAndTagsName(String title, String tagName);
}

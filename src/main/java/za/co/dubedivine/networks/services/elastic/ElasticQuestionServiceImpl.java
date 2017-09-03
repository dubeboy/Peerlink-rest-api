package za.co.dubedivine.networks.services.elastic;

import com.mongodb.QueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import za.co.dubedivine.networks.model.elastic.ElasticQuestion;
import za.co.dubedivine.networks.repository.elastic.ElasticQRepo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class ElasticQuestionServiceImpl implements ElasticQuestionService {

    private ElasticQRepo elasticQRepo;

    public ElasticQuestionServiceImpl(ElasticQRepo elasticQRepo) {
        this.elasticQRepo = elasticQRepo;
    }

    @Override
    public List<ElasticQuestion> findByTag(String tagName) {
        return elasticQRepo.findByTagsName(tagName);
    }

    @Override
    public List<ElasticQuestion> search(String q) {
//        SearchQuery searchQuery = SearchQuery
//        QueryBuilder queryBuilder = new QueryBuilder().
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("title", articleTitle).minimumShouldMatch("75%"))
                .build();
        elasticQRepo.search()

        return Collections.emptyList();
    }

    @Override
    public Set<ElasticQuestion> findByTitleAndTagsName(String title, String tagName) {
        return elasticQRepo.findByTitleAndTagsName(title, tagName);
    }

    @Override
    public ElasticQuestion save(ElasticQuestion question) {
        return elasticQRepo.save(question);
    }

    @Override
    public void delete(ElasticQuestion question) {
        elasticQRepo.delete(question);
    }
}

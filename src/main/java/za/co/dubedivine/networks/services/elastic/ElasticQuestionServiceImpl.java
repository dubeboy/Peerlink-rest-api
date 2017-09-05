package za.co.dubedivine.networks.services.elastic;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import za.co.dubedivine.networks.model.elastic.ElasticQuestion;
import za.co.dubedivine.networks.repository.elastic.ElasticQRepo;

import java.util.List;

@Service
public class ElasticQuestionServiceImpl implements ElasticQuestionService {

    private final ElasticsearchTemplate elasticsearchTemplate;
    private ElasticQRepo elasticQRepo;

    public ElasticQuestionServiceImpl(ElasticQRepo elasticQRepo, ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticQRepo = elasticQRepo;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public List<ElasticQuestion> findByTag(String tagName) {
        return elasticQRepo.findByTagsName(tagName);
    }

    @Override
    public List<ElasticQuestion> search(String q) {
        QueryBuilder queryBuilder =
                QueryBuilders
                        .boolQuery()
                        .should(
                                QueryBuilders
                                        .queryStringQuery(q)
                                        .lenient(true)
                                        .field("title")
                                        .field("body"))
                        .should(QueryBuilders.queryStringQuery("*" + q + "*")
                                .lenient(true)
                                .field("name")
                                .field("body")
                        );
        NativeSearchQuery build = new NativeSearchQueryBuilder().withQuery(queryBuilder).build();
        return elasticsearchTemplate.queryForList(build, ElasticQuestion.class);
    }

    @Override
    public List<ElasticQuestion> findByTitleAndTagsName(String title, String tagName) {
        //todo: need to find way to query based on child element
        QueryBuilder queryBuilder =
                QueryBuilders
                        .boolQuery()
                        .must(QueryBuilders
                                .queryStringQuery(tagName)
                                .lenient(true)
                                .field("tags.name"))
                        .should(
                                QueryBuilders
                                        .queryStringQuery(title)
                                        .lenient(true)
                                        .field("title")
                                        .field("body"))
                        .should(QueryBuilders.queryStringQuery("*" + title + "*")  // wild card matching
                                .lenient(true)
                                .field("name")
                                .field("body")
                        );
        NativeSearchQuery build = new NativeSearchQueryBuilder().withQuery(queryBuilder).build();
        return elasticsearchTemplate.queryForList(build, ElasticQuestion.class);
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

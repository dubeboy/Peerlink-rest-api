package za.co.dubedivine.networks.services.elastic

import org.elasticsearch.index.query.FuzzyQueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service
import za.co.dubedivine.networks.model.elastic.ElasticQuestion
import za.co.dubedivine.networks.model.elastic.ElasticTag
import za.co.dubedivine.networks.repository.elastic.ElasticTagRepo

@Service
class ElasticTagServiceImpl(private var elaticTagRepo: ElasticTagRepo,
                            private var elasticsearchTemplate: ElasticsearchTemplate) : ElasticTagService {


    //todo: should be renamed to search tags
    override fun suggestTag(tagName: String): List<ElasticTag> {
        val queryBuilder = QueryBuilders
                .boolQuery()
                .should(QueryBuilders.queryStringQuery("*$tagName*")  // wild card matching
                                        .lenient(true)
                                        .field("name"))

        val build = NativeSearchQueryBuilder().withQuery(queryBuilder).build()
        //elasticQRepo.findByTitleAndBodyAndTagsName(title, tagName);
        return elasticsearchTemplate.queryForList(build, ElasticTag::class.java)
    }

}
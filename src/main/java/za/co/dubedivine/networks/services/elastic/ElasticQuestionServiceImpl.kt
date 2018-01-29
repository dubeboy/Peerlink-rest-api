package za.co.dubedivine.networks.services.elastic

import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.stereotype.Service
import za.co.dubedivine.networks.model.Question
import za.co.dubedivine.networks.model.elastic.ElasticQuestion
import za.co.dubedivine.networks.repository.elastic.ElasticQRepo



//there is a problem when a person searches for a space it return all the results
@Service
class ElasticQuestionServiceImpl(private val elasticQRepo: ElasticQRepo,
                                 private val elasticsearchTemplate: ElasticsearchTemplate) : ElasticQuestionService {

    override fun findByTag(tagName: String): List<ElasticQuestion> {
        return elasticQRepo.findByTagsName(tagName)

    }

    override fun search(q: String): List<ElasticQuestion> {
        val queryBuilder = QueryBuilders
                .boolQuery()
                .should(
                        QueryBuilders
                                .queryStringQuery(q)
                                .lenient(true)
                                .field("title")
                                .field("body")
                                .fuzziness(Fuzziness.AUTO))
//                .should(QueryBuilders.queryStringQuery("*$q*")
//                        .lenient(true)
//                        .field("name")
//                        .field("body")
//                )
        val build = NativeSearchQueryBuilder().withQuery(queryBuilder).build()
        return elasticsearchTemplate.queryForList(build, ElasticQuestion::class.java)
    }

    override fun searchWithQuestionTag(title: String, tagName: String): List<ElasticQuestion> {

        println("starting with ###################")
        println("the tags is: " + tagName)
        println("the title: " + title)
        println("starting with ###################")
        //todo: need to find way to query based on child element
        val queryBuilder = QueryBuilders
                .boolQuery()
                .should(
                        QueryBuilders
                                .queryStringQuery(title)
                                .lenient(true)
                                .field("title")
                                .field("body"))
                .should(QueryBuilders.queryStringQuery("*$title*")  // wild card matching
                        .lenient(true)
                        .field("name")
                        .field("body")
                )
                .must(QueryBuilders.nestedQuery("tags",
                        QueryBuilders.termQuery("tags.name", tagName))
                ) //todo: i should be able to query child elements
        val build = NativeSearchQueryBuilder().withQuery(queryBuilder).build()
        //elasticQRepo.findByTitleAndBodyAndTagsName(title, tagName);
        return elasticsearchTemplate.queryForList(build, ElasticQuestion::class.java)
    }



    override fun save(question: ElasticQuestion): ElasticQuestion {
        return elasticQRepo.save(question)
    }

    override fun delete(question: ElasticQuestion) {
        elasticQRepo.delete(question)
    }

    override fun deleteAll() {
        elasticQRepo.deleteAll()
    }

    //todo: try to add suggestions use search instead
    override fun suggestQuestion(tile: String): List<ElasticQuestion> {
        throw IllegalStateException("not implemented failed to get " + "the suggestions working using search")
    }

    override fun saveQuestionToElastic(question: Question): ElasticQuestion {
        val eQ = ElasticQuestion(question.title,
                question.body, question.votes, question.tags, question.type)
        eQ.answers = question.answers
        eQ.id = question.id
        val saved =  elasticQRepo.save(eQ)
        return saved
    }
}

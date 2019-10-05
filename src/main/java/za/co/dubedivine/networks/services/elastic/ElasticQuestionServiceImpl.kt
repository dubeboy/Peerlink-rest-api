package za.co.dubedivine.networks.services.elastic

import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryBuilders.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.SearchQuery
import org.springframework.stereotype.Service
import za.co.dubedivine.networks.model.Question
import za.co.dubedivine.networks.model.elastic.ElasticQuestion
import za.co.dubedivine.networks.model.elastic.ElasticTag
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

    private fun generateBoostValueFromString(string: String): Float {
        return if (string.isEmpty()) 0F else 1f
    }

    private fun searchForQuestions(query: String,  tag: String, page: Int = 0, tag1: String = "", tag2: String="", tag3: String=""): Page<ElasticQuestion> {
//        FunctionScoreQuery.ScoreMode.AVG
        val searchQuery: SearchQuery = NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(
                                multiMatchQuery(query, "title^3", "body^2.5")
                                        .fuzziness(Fuzziness.TWO) // value type is Any because AUTO or any numerical value
                                        .prefixLength(0)
                                        .maxExpansions(100)
//                                .autoGenerateSynonymsPhraseQuery(true)
                                        .boost(1.0F)
                        )
                ).withFilter(
                        QueryBuilders.nestedQuery("tags",
                               boolQuery()
                                        .should(matchQuery("tags.name", tag).boost(generateBoostValueFromString(tag)))
                                        .should(matchQuery("tags.name", tag1).boost(generateBoostValueFromString(tag1)))
                                        .should(matchQuery("tags.name", tag2).boost(generateBoostValueFromString(tag2)))
                                        .should(matchQuery("tags.name", tag3).boost(generateBoostValueFromString(tag3))),
                                ScoreMode.Avg
                        )
                )
                .withPageable(PageRequest.of(page, 10))
                .build()

        val questions: Page<ElasticQuestion> = elasticsearchTemplate.queryForPage(searchQuery, ElasticQuestion::class.java)
        return questions
    }

    private fun searchForTags(partiallyCompletedTag: String): Page<ElasticTag> {
        val searchQuery: SearchQuery = NativeSearchQueryBuilder()
                .withQuery(
                        nestedQuery(
                                "tags",
                                fuzzyQuery("tags.name", partiallyCompletedTag)
                                        .boost(1F)
                                        .prefixLength(0)
                                        .maxExpansions(100)
                                        .fuzziness(Fuzziness.TWO),
                                ScoreMode.Avg
                        )
                )
                .withPageable(PageRequest.of(0, 5))
                .build()
        val tags: Page<ElasticTag> = elasticsearchTemplate.queryForPage(searchQuery, ElasticTag::class.java)
        return tags
    }


    override fun searchWithQuestionTag(title: String, tagName: String): List<ElasticQuestion> {

        println("starting with ###################")
        println("the tags is: $tagName")
        println("the title: $title")
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
                        QueryBuilders.termQuery("tags.name", tagName), ScoreMode.Avg)
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
        val eQ = ElasticQuestion(question.title, question.body, question.votes, question.tags, question.type)
        eQ.answers = question.answers
        eQ.id = question.id
        eQ.files = question.files
        eQ.video = question.video
        eQ.user = question.user
        return elasticQRepo.save(eQ)
    }
}

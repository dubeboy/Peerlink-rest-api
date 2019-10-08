package za.co.dubedivine.networks.services.elastic

import org.apache.lucene.search.join.ScoreMode
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.query.QueryBuilders.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage
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
        return searchForQuestions(q).content
    }

    private fun searchForQuestions(query: String): AggregatedPage<ElasticQuestion> {
        val searchQuery: SearchQuery = createBodySearchNativeQuery(query)
                .withPageable(PageRequest.of(0, 5))
                .build()
        return elasticsearchTemplate.queryForPage(searchQuery, ElasticQuestion::class.java)
    }



    override fun searchWithQuestionTag(query: String, tag: String, tag1: String, tag2: String, tag3: String): List<ElasticQuestion> {

        println("starting with ###################")
        println("the tags is: $tag")
        println("the query: $query")
        println("starting with ###################")

        return searchForQuestionsWithTags(query, tag, tag1, tag2, tag3).content
    }

    override fun save(book: ElasticQuestion): ElasticQuestion {
        return elasticQRepo.save(book)
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

    private fun generateBoostValueFromString(string: String): Float {
        return if (string.isEmpty()) 0F else 1f
    }

    private fun createBodySearchNativeQuery(query: String): NativeSearchQueryBuilder {
        return  NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.boolQuery()
                        .must(
                                multiMatchQuery(query)
                                        .field("title", 3F)
                                        .field("body", 2.5F)
                                        .fuzziness(Fuzziness.TWO) // value type is Any because AUTO or any numerical value
                                        .prefixLength(0)
                                        .maxExpansions(100)
                                        .boost(1.0F)
                        )
                )
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

    private fun searchForQuestionsWithTags(query: String,
                                           tag: String,
                                           tag1: String = "",
                                           tag2: String="",
                                           tag3: String="",
                                           page: Int = 0): AggregatedPage<ElasticQuestion> {
//        FunctionScoreQuery.ScoreMode.AVG
        val searchQuery: SearchQuery = createBodySearchNativeQuery(query)
                .withFilter(
                        nestedQuery("tags",
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

        return elasticsearchTemplate.queryForPage(searchQuery, ElasticQuestion::class.java)
    }
}

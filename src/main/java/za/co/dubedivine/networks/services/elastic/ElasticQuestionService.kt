package za.co.dubedivine.networks.services.elastic

import za.co.dubedivine.networks.model.Question
import za.co.dubedivine.networks.model.elastic.ElasticQuestion


interface ElasticQuestionService {
    fun save(book: ElasticQuestion): ElasticQuestion

    fun delete(question: ElasticQuestion)

    fun deleteAll()

    fun findByTag(tagName: String): List<ElasticQuestion>

    fun search(q: String): List<ElasticQuestion>

    fun searchWithQuestionTag(title: String, tagName: String): List<ElasticQuestion>

    fun suggestQuestion(tile: String): List<ElasticQuestion>

    fun saveQuestionToElastic(question: Question): ElasticQuestion
}

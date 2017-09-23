package za.co.dubedivine.networks.config

import org.springframework.boot.CommandLineRunner
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Component
import za.co.dubedivine.networks.model.Answer
import za.co.dubedivine.networks.model.Comment
import za.co.dubedivine.networks.model.Question
import za.co.dubedivine.networks.model.Tag
import za.co.dubedivine.networks.model.elastic.ElasticQuestion
import za.co.dubedivine.networks.model.elastic.ElasticTag
import za.co.dubedivine.networks.repository.QuestionRepository
import za.co.dubedivine.networks.repository.elastic.ElasticTagRepo
import za.co.dubedivine.networks.services.elastic.ElasticQuestionService
import javax.annotation.PreDestroy

/**
 * Created by divine on 2017/08/11.
 */
@Component
class DBHelper(private val questionRepository: QuestionRepository,
               private val elasticQuestionService: ElasticQuestionService,
               private val elasticSearchOperations: ElasticsearchOperations,
               private val elasticTagRepo: ElasticTagRepo) : CommandLineRunner {


    @PreDestroy
    fun deleteIndex() {
        println("calling this one here")
        elasticSearchOperations.deleteIndex(ElasticQuestion::class.java)
    }

    @Throws(Exception::class)
    override fun run(vararg strings: String) {
        val questionA = Question("what is an Atom",
                "people are telling me different things about an atom bro please help me!",
                10, arrayListOf(Tag("phy1a")),
                "QUESTION")
        questionA.answers = arrayListOf(Answer("an atom is the the basic building block of matter", 100, true),
                Answer("an atom is the smallest matter that the make up every known object", 50, false))
        questionA.comments = arrayListOf(Comment("did u try googling this?", 0),
                Comment("we would expect you to know this bro!", 2))
        val questionB: Question = Question("What is magnetic flux",
                "We doing magnets and electricity and i really dont get the gist of magnetic flux",
                10, arrayListOf(Tag("phy1a"), Tag("etn1b")),
                "QUESTION")
        questionB.answers = arrayListOf(Answer("the magnetic flux is the force field around a magnet",
                300, true),
                Answer("the magnetic flux is the name of invisible force that the magnet has on objects",
                        60, false))
        questionB.comments = arrayListOf(Comment("good question", 1))


        //todo : remove at production
        val run: MutableList<Question> = questionRepository.run {
            deleteAll() //delete all first
            save(arrayListOf(questionA, questionB))
        }

        println("calling this one here")
        elasticQuestionService.deleteAll()
        elasticSearchOperations.refresh(ElasticQuestion::class.java)
        run.forEach {
            val el = ElasticQuestion(it.title, it.body, it.votes, it.tags, it.type)
            el.answers = it.answers
            el.id = it.id
            elasticQuestionService.save(el)
        }
        elasticTagRepo.deleteAll()
        elasticTagRepo.save(arrayListOf(ElasticTag("phy1a"), ElasticTag("etn1b")))
    }
}

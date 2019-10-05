package za.co.dubedivine.networks.config

import org.springframework.boot.CommandLineRunner
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.stereotype.Component
import za.co.dubedivine.networks.model.*
import za.co.dubedivine.networks.model.elastic.ElasticQuestion
import za.co.dubedivine.networks.model.elastic.ElasticTag
import za.co.dubedivine.networks.repository.QuestionRepository
import za.co.dubedivine.networks.repository.UserRepository
import za.co.dubedivine.networks.repository.VoteEntityBridgeRepository
import za.co.dubedivine.networks.repository.elastic.ElasticTagRepo
import za.co.dubedivine.networks.services.elastic.ElasticQuestionService
import javax.annotation.PreDestroy
/**
 * Created by divine on 2017/08/11.
 */
@Component
class DBHelper(private val questionRepository: QuestionRepository,
               private val userRepository: UserRepository,
               private val elasticQuestionService: ElasticQuestionService,
               private val elasticSearchOperations: ElasticsearchOperations,
               private val voteEntityBridgeRepo: VoteEntityBridgeRepository,
               private val elasticTagRepo: ElasticTagRepo) : CommandLineRunner {
//
    @PreDestroy
    fun deleteIndex() {
        println("calling predestroy this one here")
        elasticSearchOperations.deleteIndex(ElasticQuestion::class.java)
    }
//
    @Throws(Exception::class)
    override fun run(vararg strings: String) {

       val user =  userRepository.save(User("DDD", "sss@msms.com", null))

        val questionA =
                Question("what is an Atom",
                        "people are telling me different things about an atom bro please help me!",
                        10, arrayListOf(Tag("phy1a"), Tag("atom")),
                        "QUESTION")
        val qA = Answer("an atom is the smallest matter that the make up every known object",
                50,
                false)
        qA.comments = arrayListOf(
                Comment("good comment", 0),
                Comment("bad comment", 0))
        questionA.answers = arrayListOf(
                Answer("an atom is the the basic building block of matter", 100, true),
                qA)
        questionA.comments = arrayListOf(Comment("did u try googling this?", 0),
                Comment("we would expect you to know this bro!", 2))

        questionA.user = user
        val questionB: Question = Question("What is magnetic flux",
                "We doing magnets and electricity and i really dont get the gist of magnetic flux",
                10, arrayListOf(Tag("phy1a"),
                Tag("etn1b")),
                "QUESTION")
        val answer = Answer("the magnetic flux is the force field around a magnet",
                300,
                true)
        answer.comments = arrayListOf(Comment("Hello", 0), Comment("Sup", 100))
        questionB.answers =
                arrayListOf(answer,
                        Answer("the magnetic flux is the name of invisible force that the magnet has on objects",
                                60,
                                false))
        questionB.comments = arrayListOf(Comment("good question", 1))

        questionB.user = user
//        //TODO : remove at production
        val savedQuestions: MutableList<Question> = questionRepository.run {
            deleteAll() //delete all first
            saveAll(arrayListOf(questionA, questionB))
        }

        userRepository.deleteAll()
        voteEntityBridgeRepo.save(VoteEntityBridge(Pair(savedQuestions[0].id, user.id ), true))

        println("deleting all the available content in the elastic database")
        questionRepository.deleteAll()
        elasticQuestionService.deleteAll()
        elasticSearchOperations.refresh(ElasticQuestion::class.java)
        savedQuestions.forEach {
            val el = ElasticQuestion(it.title, it.body, it.votes, it.tags, it.type)
            el.answers = it.answers
            el.comments = it.comments
            el.id = it.id
            elasticQuestionService.save(el)
        }
        elasticTagRepo.deleteAll()
        elasticTagRepo.saveAll(arrayListOf(ElasticTag("phy1a"), ElasticTag("etn1b")))
    }
}

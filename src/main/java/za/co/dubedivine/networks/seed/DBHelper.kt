package za.co.dubedivine.networks.seed

import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import za.co.dubedivine.networks.model.Answer
import za.co.dubedivine.networks.model.Comment
import za.co.dubedivine.networks.model.Question
import za.co.dubedivine.networks.model.Tag
import za.co.dubedivine.networks.model.repository.QuestionRepository

/**
 * Created by divine on 2017/08/11.
 */
@Component
class DBHelper(private val questionRepository: QuestionRepository) : CommandLineRunner {

    @Throws(Exception::class)
    override fun run(vararg strings: String) {
       val questionA: Question = Question("what is an Atom",
               "people are telling me different things about an atom bro please help me!",
               10, arrayListOf(Tag("phy1a")),
               "QUESTION")
        questionA.answers = arrayListOf( Answer("an atom is the the basic building block of matter", 100, true),
                                        Answer("an atom is the smallest matter that the make up every known object", 50, false))

        questionA.comments = arrayListOf(Comment("did u try googling this?", 0),
                Comment("we would expect you to know this bro!", 2))

        val questionB: Question = Question("What is magnetic flux",
                "We doing magnets and electricity and i really dont get the gist of magnetic flux",
                10, arrayListOf(Tag("phy1a"), Tag("etn1b")),
                "QUESTION")
        questionB.answers = arrayListOf( Answer("the magnetic flux is the force field around a magnet",
                300, true),
                Answer("the magnetic flux is the name of invisible force that the magnet has on objects",
                        60, false))

        questionB.comments = arrayListOf(Comment("good question", 1))

        //todo : just for testing
//        questionRepository.deleteAll() //delete all first
//        questionRepository.save(arrayListOf(questionA, questionB))

    }
}

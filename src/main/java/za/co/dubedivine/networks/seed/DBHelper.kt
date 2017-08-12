package za.co.dubedivine.networks.seed

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import za.co.dubedivine.networks.model.Answer
import za.co.dubedivine.networks.model.Comment
import za.co.dubedivine.networks.model.Question
import za.co.dubedivine.networks.model.Tag
import za.co.dubedivine.networks.model.repository.TagRepository
import java.util.*

/**
 * Created by divine on 2017/08/11.
 */
@Component
class DBHelper(private val tagRepository: TagRepository) : CommandLineRunner {

    @Throws(Exception::class)
    override fun run(vararg strings: String) {
        val t: Tag = Tag("csc2b",
                        arrayListOf(Question( "how to create a box", "I have been trying to create this thing kodwa iyoh", 0,
                                arrayListOf(),
                                arrayListOf(Answer(
                                        "you need ABX to create a box mchana",
                                        10,
                                        false,
                                        Date(),
                                        arrayListOf(Comment(
                                                "this is a dope aspect bro",
                                                10,
                                                Date())),
                                        null)),
                                null,
                                "QUESTION",
                                null)),
                        Date())
        val t2: Tag = Tag(
                "csc1a",
                arrayListOf(Question( "how to create a computer",
                        "I have been trying to connect abc an c on board but thol`ukuthi hey",
                        0, arrayListOf(Comment(
                        "the crazy body of nobody try difficult stuff for no reason at all",
                        10, Date())),
                        arrayListOf(Answer(
                                "you need ABX to create a box mchana",
                                10, true,
                                Date(), arrayListOf(Comment("this is a dope aspect bro",
                                10, Date())),
                                null)),
                        null,
                        "QUESTION",
                        null)),
                Date())



        //drop alll the collections
        tagRepository.deleteAll()
        tagRepository.save(arrayListOf(t, t2))
    }
}

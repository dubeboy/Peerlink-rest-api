package za.co.dubedivine.networks.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.IndexDirection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

/**
 * Created by divine on 2017/08/11.
 */


// these are database models dwag

@Document
class Tag(@Indexed
          val name: String,
          @Indexed(direction = IndexDirection.ASCENDING)
          val questions: List<Question>,
          val createdAt: Date = Date()) {
    @Id
    val id: String? = null
}



class Question(
        val title: String,
        val body: String,
        val votes: Long = 0, // the number of votes that this question has
        val comments: List<Comment>?,
        val answer: List<Answer>?, //ans can be null at first
        val video: Video?,
        val type: String, //indicating where this is a past paper or not?
        val files: List<Docs>?
        )



class Comment(
              val body: String,
              val votes: Long,
              val createdAt: Date = Date())

class Answer(
             val body: String,
             val votes: Long,
             val isChosen: Boolean, // where the user selected it as being the correct one
             val createdAt: Date = Date(),
             val comments: List<Comment>?,
             val video: Video?
             )

class Docs(val name: String,
           val size: String,
           val type: String,
           val location: String,
           val limit: Int = 5 * 1024,
           val createdAt: Date = Date()) //5 mb limit

class Video(val name: String,
            val type: String,
            val size: Int,
            val location: String,
            val limit: Int = 5 * 1024,
            val createdAt: Date = Date())



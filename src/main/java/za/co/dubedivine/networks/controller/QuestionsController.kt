package za.co.dubedivine.networks.controller

import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import za.co.dubedivine.networks.model.*
import za.co.dubedivine.networks.model.repository.QuestionRepository

import java.net.URI
import java.util.ArrayList

//todo: handling invalid data an dublicate data
//todo: split tags and questions
// todo: make pageable offset etc
@RestController
@RequestMapping("questions")
class QuestionsController(private val repository: QuestionRepository) {

    //TODO: Google post vs put
    val allQuestions: List<Question>
        @GetMapping
        get() {
            val sort = Sort(Sort.Direction.DESC, "createdAt")
            return repository.findAll(sort)
        }

    @PutMapping //adding anew entity
    fun addQuestion(@RequestBody question: Question): ResponseEntity<Any> {
        println("hello there we here bro")
        repository.insert(question)
        val uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(question.id).toUri()
        val httpHeaders = HttpHeaders()
        httpHeaders.location = uri
        println("hello there we here bro 6555555")

        return ResponseEntity<Any>(null, httpHeaders, HttpStatus.CREATED)
    }

    @PostMapping //for editing
    fun editQuestion(@RequestBody question: Question) {
        repository.save(question)
    }

    @PutMapping("/{q_id}/answer") // questions/1/answer
    fun addAnswer(@PathVariable("q_id") questionId: String, @RequestBody answer: Answer): ResponseEntity<StatusResponseEntity> {   // could also take in the the whole question
        val question = repository.findOne(questionId)
        val statusResponseEntity: StatusResponseEntity
        return if (question == null) {
            statusResponseEntity = StatusResponseEntity(false, "the question you are trying to update does not exist")
            ResponseEntity(statusResponseEntity, HttpStatus.BAD_REQUEST)
        } else {
            // todo: can be done better dwag
            val answers: ArrayList<Answer> = if (question.answers != null) {
                ArrayList(question.answers)
            } else {
                ArrayList()
            }

            answers.add(answer)
            question.answers = answers
            repository.save(question)
            statusResponseEntity = StatusResponseEntity(true, "Successfully added a new answer to this question")
            ResponseEntity(statusResponseEntity, HttpStatus.CREATED)

        }
    }

    @PutMapping("/{q_id}/comment") // // questions/1/comment
    fun addComment(@PathVariable("q_id") questionId: String, @RequestBody comment: Comment) {
        val question = repository.findOne(questionId)
        val comments = ArrayList<Comment>()
        comments.add(comment)
        question.comments = comments
        repository.save(question)
    }

    @DeleteMapping("/{q_id}") //questions/2
    fun deleteQuestion(@PathVariable("q_id") questionId: String) {
        //todo: should actually have another collection called deleted stuff where we move this stuff to
        repository.delete(questionId)
    }
}

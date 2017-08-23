package za.co.dubedivine.networks.controller

import org.springframework.web.bind.annotation.*
import za.co.dubedivine.networks.model.Comment
import za.co.dubedivine.networks.model.repository.QuestionRepository
import java.util.ArrayList


@RestController
@RequestMapping("questions")
class CommentsController(private val repository: QuestionRepository) {

    // you can comment on a question
    // you can comment on  an answer
    @PutMapping("/{q_id}/comment") // // questions/1/comment
    fun addComment(@PathVariable("q_id") questionId: String, @RequestBody comment: Comment) {
        val question = repository.findOne(questionId)
        val comments = ArrayList<Comment>()
        comments.add(comment)
        question.comments = comments
        repository.save(question)
    }

    @PostMapping("/{q_id}/comment") // // questions/1/comment
    fun editComment(@PathVariable("q_id") questionId: String, @RequestBody comment: Comment) {   //anything related to updating a comment is here
        val question = repository.findOne(questionId)
        val comments = ArrayList<Comment>()
        comments.add(comment)
        question.comments = comments
        repository.save(question)
    }


    fun voteOnComment() {

    }



}
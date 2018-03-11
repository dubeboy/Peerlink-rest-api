package za.co.dubedivine.networks.controller

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.*
import za.co.dubedivine.networks.model.Answer
import za.co.dubedivine.networks.model.Comment
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import za.co.dubedivine.networks.repository.QuestionRepository
import za.co.dubedivine.networks.services.elastic.ElasticQuestionService
import java.util.ArrayList

@RestController
@RequestMapping("questions")
class AnswersController(//operations that can be done on a Answers
        private val questionRepository: QuestionRepository,
        private val taskExecutor: ThreadPoolTaskExecutor,
        private val mongoTemplate: MongoTemplate,
        private val elasticQuestionService: ElasticQuestionService) {

    //todo: duplicates every time I save
    @PutMapping("/{q_id}/answer") // questions/1/answer
    fun addAnswer(@PathVariable("q_id") questionId: String, @RequestBody answer: Answer):
            ResponseEntity<StatusResponseEntity<Answer>> {   // could also take in the the whole question
        val question = questionRepository.findOne(questionId)
        println("the question $question and the id is ${question.id}")
        val statusResponseEntity: StatusResponseEntity<Answer>
        return if (question == null) {
            statusResponseEntity = StatusResponseEntity(false, "the question you are trying to update does not exist")
            ResponseEntity(statusResponseEntity, HttpStatus.BAD_REQUEST) //return this
        } else {
            if (question.answers != null) question.answers.add(answer) else question.answers = arrayListOf(answer)
            taskExecutor.execute({
                elasticQuestionService.saveQuestionToElastic(question)
            })
            val savedOne = questionRepository.save(question)
            println("after: the question $savedOne and the id is ${savedOne.id}")
            statusResponseEntity = StatusResponseEntity(true,
                    "Successfully added a new answer to this question",
                    answer)
            ResponseEntity(statusResponseEntity, HttpStatus.CREATED)

        }
    }

    @PostMapping("/{q_id}/answer/{a_id}/vote") //updating
    fun voteAnswer(@PathVariable("q_id") questionId: String,
                   @PathVariable("a_id") ans: String,
                   @RequestParam("vote") vote: Boolean): ResponseEntity<StatusResponseEntity<Answer>> {
        val answers: MutableList<Answer>
        val question = questionRepository.findOne(questionId)
        answers = question.answers
        for (answer in answers) {
            if (answer.id == ans) {
                if (vote) answer.votes = answer.votes + 1 else answer.votes = answer.votes - 1
                questionRepository.save(question)
                return ResponseEntity(StatusResponseEntity<Answer>(true,
                        "Vote ${if (vote) "added" else "removed"} ", null),
                        HttpStatus.OK)
            }
        }
        return ResponseEntity(StatusResponseEntity<Answer>(false,
                "sorry we cannot find this answer that you want to vote on", null), HttpStatus.BAD_REQUEST)
    }

    @PostMapping("/{q_id}/answer/{a_id}/comment")
    fun commentOnAnswer(@PathVariable("q_id") questionId: String,
                        @PathVariable("a_id") answerId: String,
                        @RequestBody comment: Comment): ResponseEntity<StatusResponseEntity<Answer>> {
        val question = questionRepository.findOne(questionId)
        //TODO: Bad code use Criteria criteria = Criteria.where("mappings.provider.name").is(provider.getName());
        val answers = question.answers
        if (question == null) return ResponseEntity(StatusResponseEntity<Answer>(false,
                "no such question can be found", null), HttpStatus.BAD_REQUEST)
        else
            for (answer in answers) {
                if (answer.id == answerId) {
                    answer.comments.add(comment)
                    questionRepository.save(question)
                    return ResponseEntity(StatusResponseEntity(true,
                            "Comment added ", answer),
                            HttpStatus.OK)
                }
            }
        return ResponseEntity(StatusResponseEntity<Answer>(false,
                "no such answer found can be found", null), HttpStatus.BAD_REQUEST)
    }
}

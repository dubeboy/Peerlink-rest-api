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

    //vote on a question please
    //flawed pass just add or minus
    @PostMapping("/{q_id}/answer") //updating
    fun voteAnswer(@PathVariable("q_id") questionId: String,
                   @RequestBody ans: Answer): ResponseEntity<StatusResponseEntity<Answer>> {
        val answers: MutableList<Answer>
        val question = questionRepository.findOne(questionId)
        answers = question.answers
        for (answer in answers) {
            if (answer.id == ans.id) {
                answers.remove(answer)
                answers.add(ans)
                question.answers = answers
                return ResponseEntity(StatusResponseEntity<Answer>(true,
                        "Votes added " + answer.votes + 1, null),
                        HttpStatus.OK)
            }
        }
        return ResponseEntity(StatusResponseEntity<Answer>(false,
                "no such question can be found", null), HttpStatus.BAD_REQUEST)
    }

    @PostMapping("/{q_id}/answer/{a_id}/comment")
    fun commentOnAnswer(@PathVariable("q_id") questionId: String, 
                            @PathVariable("a_id") answerId: String,
                             @RequestBody comment: Comment): ResponseEntity<StatusResponseEntity<Answer>> {
        val answers: MutableList<Answer>
        val question = questionRepository.findOne(questionId)
        answers = question.answers
        // Criteria criteria = Criteria.where("mappings.provider.name").is(provider.getName());
        //TODO: Bad code

        for (answer in answers) {
            if (answer.id == answerId) {
//                answers.remove(answer)
                answer.comments.add(comment)
//                answers.add(answer)
//                question.answers = answers
                questionRepository.save(question)
                return ResponseEntity(StatusResponseEntity(true,
                        "Votes added " + answer.votes + 1, answer ),
                        HttpStatus.OK)
            }
        }
        return ResponseEntity(StatusResponseEntity<Answer>(false,
                "no such question can be found", null), HttpStatus.BAD_REQUEST)
    }
}

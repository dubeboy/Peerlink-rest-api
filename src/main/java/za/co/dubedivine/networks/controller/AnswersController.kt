package za.co.dubedivine.networks.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.*
import za.co.dubedivine.networks.model.Answer
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import za.co.dubedivine.networks.repository.QuestionRepository
import za.co.dubedivine.networks.services.elastic.ElasticQuestionService
import java.util.ArrayList

@RestController
@RequestMapping("questions")
class AnswersController(//operations that can be done on a Answers
        private val questionRepository: QuestionRepository,
        private val taskExecutor: ThreadPoolTaskExecutor,
        private val elasticQuestionService: ElasticQuestionService
) {

    @PutMapping("/{q_id}/answer") // questions/1/answer
    fun addAnswer(@PathVariable("q_id") questionId: String, @RequestBody answer: Answer): ResponseEntity<StatusResponseEntity> {   // could also take in the the whole question
        val question = questionRepository.findOne(questionId)
        val statusResponseEntity: StatusResponseEntity
        return if (question == null) {
            statusResponseEntity = StatusResponseEntity(false, "the question you are trying to update does not exist")
            ResponseEntity(statusResponseEntity, HttpStatus.BAD_REQUEST) //return this
        } else {
            // todo: can be done better dwag
            val answers: ArrayList<Answer> = if (question.answers != null) {
                ArrayList(question.answers)
            } else {
                ArrayList()
            }
            answers.add(answer)  // todo: not very efficient but will do for now
            question.answers = answers

            //not saving the saved question because what if mongo goes wrong we might have a
            // chance of catching that value on mongo!!!
            taskExecutor.execute({
                elasticQuestionService.saveQuestionToElastic(question)
            })

            questionRepository.save(question)
            statusResponseEntity = StatusResponseEntity(true, "Successfully added a new answer to this question")
            ResponseEntity(statusResponseEntity, HttpStatus.CREATED)

        }
    }

    //vote on a question please
    @PostMapping("/{q_id}/answer") //updating
    fun voteAnswer(@PathVariable("q_id") questionId: String,
                   @RequestBody ans: Answer): ResponseEntity<StatusResponseEntity> {
        val answers: MutableList<Answer>
        val question = questionRepository.findOne(questionId)
        answers = question.answers
        for (answer in answers) {
            if (answer.id == ans.id) {
                answers.remove(answer)
                answers.add(ans)
                question.answers = answers
                return ResponseEntity(StatusResponseEntity(true,
                        "Votes added " + answer.votes + 1),
                        HttpStatus.OK)
            }
        }
        return ResponseEntity(StatusResponseEntity(false,
                "no such question can be found"), HttpStatus.BAD_REQUEST)
    }
}

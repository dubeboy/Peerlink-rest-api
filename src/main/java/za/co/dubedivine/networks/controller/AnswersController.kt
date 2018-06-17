package za.co.dubedivine.networks.controller

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.util.MimeType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import za.co.dubedivine.networks.model.*
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import za.co.dubedivine.networks.repository.QuestionRepository
import za.co.dubedivine.networks.repository.TagRepository
import za.co.dubedivine.networks.repository.UserRepository
import za.co.dubedivine.networks.repository.VoteEntityBridgeRepository
import za.co.dubedivine.networks.services.AndroidPushNotificationService
import za.co.dubedivine.networks.services.elastic.ElasticQuestionService
import za.co.dubedivine.networks.util.Data
import za.co.dubedivine.networks.util.ENTITY_TYPE
import za.co.dubedivine.networks.util.KUtils
import za.co.dubedivine.networks.util.KUtils.createVoteEntity
import java.util.ArrayList

@RestController
@RequestMapping("questions")
class AnswersController(//operations that can be done on a Answers
        private val questionRepository: QuestionRepository,
        private val taskExecutor: ThreadPoolTaskExecutor,
        private val mongoTemplate: MongoTemplate,
        private val tagRepository: TagRepository,
        private val userRepository: UserRepository,
        private val voteEntityBridgeRepo: VoteEntityBridgeRepository,
        private val elasticQuestionService: ElasticQuestionService,
        private val androidPushNotifications: AndroidPushNotificationService) {

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
            KUtils.executeJobOnThread {
                KUtils.getElasticTag(question, answer.user, tagRepository, userRepository)
                val users = KUtils.retrieveUsersInThread(userRepository, question)
                //notify users
                for (usr in users) {
                    KUtils.notifyUserInThread(androidPushNotifications,
                            "CQ: ${question.title}",
                            answer.body,
                            usr.fcmToken, Data(question.id, ENTITY_TYPE.ANSWER, """{"answer_id": ${answer.id} }"""))
                }
            }
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
                   @RequestParam("vote") vote: Boolean,
                   @RequestParam("user_id") userId: String): ResponseEntity<StatusResponseEntity<Answer>> {
        val voted =
                try {
                    val voteDirection: Boolean = voteEntityBridgeRepo.findOne(Pair(questionId, userId)).isVoteTheSameDirection == vote
                    voteEntityBridgeRepo.exists(Pair(questionId, userId)) && voteDirection
                } catch (npe: NullPointerException) {
                    false
                }

        if (voted) {
            return ResponseEntity(StatusResponseEntity<Answer>(false,
                    "you have already voted", null),
                    HttpStatus.OK)
        } else {
            createVoteEntity(voteEntityBridgeRepo, Pair(questionId, userId), vote)
            val answers: MutableList<Answer>
            val question = questionRepository.findOne(questionId)
            answers = question.answers
            for (answer in answers) {
                if (answer.id == ans) {
                    if (vote) answer.votes = answer.votes + 1 else answer.votes = answer.votes - 1
                    questionRepository.save(question)
                    KUtils.executeJobOnThread {
                        val user = userRepository.findOne(userId)
                        KUtils.getElasticTag(question, user, tagRepository, userRepository)
                        //notify users
                            KUtils.notifyUserInThread(androidPushNotifications,
                                    "CQ: ${question.title}",
                                    answer.body,
                                    user.fcmToken, Data(question.id, ENTITY_TYPE.ANSWER_VOTE, """{"answer_id": ${answer.id} }"""))
                    }
                    return ResponseEntity(StatusResponseEntity<Answer>(true,
                            "Vote ${if (vote) "added" else "removed"} ", null),
                            HttpStatus.OK)
                }
            }
            return ResponseEntity(StatusResponseEntity<Answer>(false,
                    "sorry we cannot find this answer that you want to vote on", null), HttpStatus.BAD_REQUEST)
        }
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
                    KUtils.executeJobOnThread {
                        KUtils.getElasticTag(question, comment.user, tagRepository, userRepository)
                        val users = KUtils.retrieveUsersInThread(userRepository, question)
                        //notify users
                        for (usr in users) {
                            KUtils.notifyUserInThread(androidPushNotifications,
                                    "AC: ${question.title}",
                                    comment.body,
                                    usr.fcmToken, Data(question.id, ENTITY_TYPE.ANSWER_COMMENT, """{"answer_id": ${answer.id} }"""))
                        }
                    }
                    return ResponseEntity(StatusResponseEntity(true,
                            "Comment added ", answer),
                            HttpStatus.OK)
                }

            }
        return ResponseEntity(StatusResponseEntity<Answer>(false,
                "no such answer found can be found", null), HttpStatus.BAD_REQUEST)
    }

    //TODO needs some refactoring bro
    @PostMapping("/{q_id}/answer/{a_id}/files")
    fun addFiles(@PathVariable("q_id") questionId: String,
                 @PathVariable("a_id") answerId: String,
                 @RequestPart files: List<MultipartFile>): ResponseEntity<StatusResponseEntity<Question>> {
        val question = questionRepository.findOne(questionId)
        if ((question) != null) {
            var answer: Answer? = null

            // look for the answer in the question using it ID and then
            // we assign it to answer
            for (it in question.answers) {
                if (it.id == answerId) {
                    answer = it
                    break
                }
            }

            if (answer != null) {
                val fs = KUtils.getGridFs(mongoTemplate)
                println("the bucket name is:  ${fs.bucketName} and the db:  ${fs.db}")
                if (files.size == 1 && KUtils.isFileAVideo(files[0].contentType)) {  //not the best way of checking, but i know the client will restrict this

                    val createFile = fs.createFile(files[0].inputStream, files[0].originalFilename, true)
                    val mime = KUtils.genMimeTypeForVideo(files[0].originalFilename)
                    println("mime is: $mime")
                    createFile.contentType = mime
                    createFile.put("questionId", questionId.toString())
                    createFile.save()
                    println("the is of the file is: ${createFile.id}")

                    answer.video = Media(files[0].originalFilename, createFile.length, Media.VIDEO_TYPE, createFile.id.toString())
                    val savedQuestion = questionRepository.save(question)
                    println(savedQuestion)
                    KUtils.saveQuestionOnElasticOnANewThread(elasticQuestionService, taskExecutor, savedQuestion)
                    return ResponseEntity(StatusResponseEntity(
                            true, "file created", savedQuestion), HttpStatus.CREATED)
                } else { // this application type is
                    val docs: ArrayList<Media> = arrayListOf()
                    files.forEach {
                        val createFile = fs.createFile(it.inputStream, it.originalFilename, true)
                        //need to change this to map to the proper mime
                     //   MimeType.valueOf()
                        createFile.contentType = it.contentType
                        createFile.put("questionId", questionId)
                        createFile.save()
                        docs.add(Media(
                                it.originalFilename,
                                createFile.length,
                                KUtils.genMediaTypeFromContentType(createFile.contentType),
                                createFile.id.toString()))
                    }
                    answer.files = docs
                    val savedQuestion = questionRepository.save(question)
                    KUtils.saveQuestionOnElasticOnANewThread(elasticQuestionService, taskExecutor, savedQuestion)
                    return ResponseEntity(StatusResponseEntity(true, "files created", question), HttpStatus.CREATED)
                }
            } else {
                return ResponseEntity(StatusResponseEntity(false,
                        "sorry could not add files because we could not find that question"), HttpStatus.NOT_FOUND)
            }
        } else {
            // todo look in
            return ResponseEntity(StatusResponseEntity(false,
                    "sorry could not add files because we could not find that question"), HttpStatus.NOT_FOUND)
        }
    }
}

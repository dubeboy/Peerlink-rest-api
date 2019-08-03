package za.co.dubedivine.networks.controller

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
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
        @Qualifier("taskExecutor") private val taskExecutor: ThreadPoolTaskExecutor,
        private val mongoTemplate: MongoTemplate,
        private val mongoDBOprations: GridFsOperations,
        private val tagRepository: TagRepository,
        private val userRepository: UserRepository,
        private val voteEntityBridgeRepo: VoteEntityBridgeRepository,
        private val elasticQuestionService: ElasticQuestionService,
        private val androidPushNotifications: AndroidPushNotificationService) {

    //todo: duplicates every time I save
    @PutMapping("/{q_id}/answer")
    fun addAnswer(@PathVariable("q_id") questionId: String, @RequestBody answer: Answer):
            ResponseEntity<StatusResponseEntity<Answer>> {   // could also take in the the whole question
        val questionOptional = questionRepository.findById(questionId)
        println("the question $questionOptional and the id is")
        val statusResponseEntity: StatusResponseEntity<Answer>
        return if (!questionOptional.isPresent) {
            statusResponseEntity = StatusResponseEntity(false, "the question you are trying to update does not exist")
            ResponseEntity(statusResponseEntity, HttpStatus.BAD_REQUEST) //return this
        } else {
            val question = questionOptional.get()
            if (question.answers != null) question.answers.add(answer) else question.answers = arrayListOf(answer)
            taskExecutor.execute {
                elasticQuestionService.saveQuestionToElastic(question)
            }
            val savedOne = questionRepository.save(question)
            KUtils.executeJobOnThread {
                KUtils.getElasticTag(question, answer.user, tagRepository, userRepository)
                val users = KUtils.retrieveUsersInThread(userRepository, question)
                val answerOwnerUser = userRepository.findByIdOrNull(answer.user.id)
                if (answerOwnerUser != null) {
                    val added = users.add(answerOwnerUser)
                    println("the user was not in the list $added")
                    //notify users
                    for (usr in users) {
                        KUtils.notifyUser(androidPushNotifications,
                                "New answer. Q: ${question.title}",
                                answer.body,
                                usr.fcmToken, Data(question.id, ENTITY_TYPE.ANSWER, """{"answer_id": ${answer.id} }"""))
                    }
                } else {
                    // todo log to elasticsearch that some how the user was not found
                    System.err.println("Answer Owner User is null")
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
                   @RequestParam("user_id") userId: String): ResponseEntity<StatusResponseEntity<Boolean>> {

        val voteDirection = voteEntityBridgeRepo.findByIdOrNull(Pair(questionId, userId))?.isVoteTheSameDirection == vote
        val voted = voteEntityBridgeRepo.existsById(Pair(questionId, userId)) && voteDirection

        if (voted) {
            return ResponseEntity(StatusResponseEntity(false,
                    "you have already voted", null),
                    HttpStatus.OK)
        } else {
            createVoteEntity(voteEntityBridgeRepo, Pair(questionId, userId), vote)
            val answers: MutableList<Answer>
            val question = questionRepository.findByIdOrNull(questionId)
            if (question != null) {
                answers = question.answers
                for (answer in answers) {
                    if (answer.id == ans) {
                        if (vote) answer.votes = answer.votes + 1 else answer.votes = answer.votes - 1
                        questionRepository.save(question)
                        KUtils.executeJobOnThread {
                            userRepository.findByIdOrNull(userId)?.let { votingUser ->
                                KUtils.getElasticTag(question, votingUser, tagRepository, userRepository)
                                userRepository.findByIdOrNull(answer.user.id)?.let { answerOwnerUser ->
                                    KUtils.notifyUser(androidPushNotifications,
                                            "${votingUser.nickname} ${if (vote) "up" else "down"} voted your answer",
                                            answer.body,
                                            answerOwnerUser.fcmToken, Data(question.id, ENTITY_TYPE.ANSWER_VOTE, """{"answer_id": ${answer.id} }"""))
                                }
                            }
                        }
                        return ResponseEntity(StatusResponseEntity(true,
                                "Vote ${if (vote) "added" else "removed"} ", vote),
                                HttpStatus.OK)
                    }
                }
            }

            return ResponseEntity(StatusResponseEntity(false,
                    "sorry we cannot find this answer that you want to vote on", null), HttpStatus.BAD_REQUEST)
        }
    }

    @PostMapping("/{q_id}/answer/{a_id}/comment")
    fun commentOnAnswer(@PathVariable("q_id") questionId: String,
                        @PathVariable("a_id") answerId: String,
                        @RequestBody comment: Comment): ResponseEntity<StatusResponseEntity<Answer>> {
        val question = questionRepository.findByIdOrNull(questionId)
        //TODO: Bad code use Criteria criteria = Criteria.where("mappings.provider.name").is(provider.getName());

        if (question == null) return ResponseEntity(StatusResponseEntity<Answer>(false,
                "no such question can be found", null), HttpStatus.BAD_REQUEST)
        else {
            val answers = question.answers
            for (answer in answers) {
                if (answer.id == answerId) {
                    answer.comments.add(comment)
                    questionRepository.save(question)
                    KUtils.executeJobOnThread {
                        KUtils.getElasticTag(question, comment.user, tagRepository, userRepository)
                        val users = KUtils.retrieveUsersInThread(userRepository, question)
                        val answerOwnerUser = userRepository.findByIdOrNull(answer.user.id)
                        val commentingUser = userRepository.findByIdOrNull(comment.user.id)
                        if (answerOwnerUser != null && commentingUser != null) {
                            val exists = users.add(answerOwnerUser)
                            println("owner of comment in thread $exists")
                            //notify users
                            for (usr in users) {
                                KUtils.notifyUser(androidPushNotifications,
                                        "${commentingUser.nickname} commented on answer",
                                        comment.body,
                                        usr.fcmToken, Data(question.id, ENTITY_TYPE.ANSWER_COMMENT, """{"answer_id": ${answer.id} }"""))
                            }
                        }
                    }
                    return ResponseEntity(StatusResponseEntity(true,
                            "Comment added ", answer),
                            HttpStatus.OK)
                }

            }
        }
        return ResponseEntity(StatusResponseEntity<Answer>(false,
                "no such answer found can be found", null), HttpStatus.BAD_REQUEST)
    }

    //TODO needs some refactoring bro
    @PostMapping("/{q_id}/answer/{a_id}/files")
    fun addFiles(@PathVariable("q_id") questionId: String,
                 @PathVariable("a_id") answerId: String,
                 @RequestPart files: List<MultipartFile>): ResponseEntity<StatusResponseEntity<Answer>> {
        val question = questionRepository.findByIdOrNull(questionId)
        if (question != null) {
            var answer: Answer? = null

            // look for the answer in the question using it ID and then
            // we assign it to answer
            for (it in question.answers) {
                if (it.id == answerId) {
                    answer = it
                    break
                }
            }

            val metaData = BasicDBObject()
            metaData["questionId"] = questionId
            if (answer != null) {
//                println("the bucket name is:  ${fs.bucketName} and the db:  ${fs.db}")
                if (files.size == 1 && KUtils.isFileAVideo(files[0].contentType)) {  //not the best way of checking,
                                                                                    // but I know the client will restrict this should use mime type
                    val mime = KUtils.genMimeTypeForVideo(files[0].originalFilename)
                    metaData[HttpHeaders.CONTENT_TYPE] = mime
                    val createFile = mongoDBOprations.store(files[0].inputStream, files[0].originalFilename, mime, metaData)
                    println("mime/content type  is: $mime")
                    println("the is of the file is: $createFile")

                    answer.video = Media(files[0].originalFilename, Media.VIDEO_TYPE, createFile.toString())
                    val savedQuestion = questionRepository.save(question)
                    println(savedQuestion)
                    KUtils.saveQuestionOnElasticOnANewThread(elasticQuestionService, taskExecutor, savedQuestion)
                    return ResponseEntity(StatusResponseEntity(
                            true, "file created", savedQuestion.answers.last()), HttpStatus.CREATED)
                } else { // this application type is
                    val docs: ArrayList<Media> = arrayListOf()
                    files.forEach {
                        println("the mime is ${it.contentType}")
                        metaData[HttpHeaders.CONTENT_TYPE] = it.contentType
                        val createFile = mongoDBOprations.store(it.inputStream, it.originalFilename, it.contentType, metaData)
                        //need to change this to map to the proper mime
                     //   MimeType.valueOf()
                        docs.add(Media(
                                it.originalFilename,
                                KUtils.genMediaTypeFromContentType(it.contentType),
                                createFile.toString()))
                    }
                    answer.files = docs
                    val savedQuestion = questionRepository.save(question)
                    KUtils.saveQuestionOnElasticOnANewThread(elasticQuestionService, taskExecutor, savedQuestion)
                    return ResponseEntity(StatusResponseEntity(true, "files created", savedQuestion.answers.last()), HttpStatus.CREATED)
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

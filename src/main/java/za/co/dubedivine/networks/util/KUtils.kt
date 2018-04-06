package za.co.dubedivine.networks.util

import com.mongodb.BasicDBObject
import com.mongodb.gridfs.GridFS
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import za.co.dubedivine.networks.config.AppConfig
import za.co.dubedivine.networks.model.*
import za.co.dubedivine.networks.model.elastic.ElasticTag
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import za.co.dubedivine.networks.repository.UserRepository
import za.co.dubedivine.networks.repository.VoteEntityBridgeRepository
import za.co.dubedivine.networks.services.elastic.ElasticQuestionService
import za.co.dubedivine.networks.services.notification.SendPushNotificationsService
import java.util.regex.Pattern

object KUtils {

    private const val REGEX = "#(\\d*[A-Za-z_]+\\w*)\\b(?!;)"

    enum class CONTENT_TYPE {
        VID_IMG, DOC
    }

    fun getFileType(mime: String): CONTENT_TYPE {
        return if (mime.startsWith("image") || mime.startsWith("video")) {
            CONTENT_TYPE.VID_IMG
        } else {
            CONTENT_TYPE.DOC
        }
    }

    fun isFileAVideo(mime: String): Boolean {
        return when (mime.substringAfter("/")) {
            "mp4", "3gp" ->
                true
            else ->
                false
        }
    }

    fun genDownloadUrlForFile(filename: String) {

    }

    fun <T> respond(status: Boolean, msg: String, obj: T): ResponseEntity<StatusResponseEntity<T>> {
        return ResponseEntity(StatusResponseEntity(status, msg, obj),
                if (status) HttpStatus.CREATED else HttpStatus.BAD_REQUEST)
    }

    fun getCleanTextAndTags(text: String): Pair<String, Set<String>> {
        println("the text is $text")
        val p = getPattern()
        val tags = mutableSetOf<String>()
        val sequenceOfTags = p.toRegex().findAll(text).distinct()
        sequenceOfTags.forEach {
            val tag = it.value.substringAfter('#')
            println("yeye thete are tags $tag ")
            tags.add(tag)
        }
        val cleanSearchText = p.matcher(text).replaceAll(" ")
        println("these are the tags fam $tags")
        return Pair(cleanSearchText, tags)
    }

    fun hasTags(text: String): Boolean {
        val p = getPattern()//pattern to match the has tags
        return p.toRegex().containsMatchIn(text)
    }

    private fun getPattern(): Pattern = Pattern.compile(KUtils.REGEX)


    // I think I was drunk here
    // well its just a lambda function,
    /**
     * @param savedTag  [Tag] to be saved
     *
     * saves a normal [Tag] to elastic search
     *
     * */
    val instantiateElasticTag: (savedTag: Tag) -> ElasticTag = {
        val elasticTag = ElasticTag(it.name)
        elasticTag.questions = it.questions
        elasticTag
    }

    private fun getContext(): AnnotationConfigApplicationContext {
        return AnnotationConfigApplicationContext(AppConfig::class.java)
    }

    fun getThreadPoolExecutor(): ThreadPoolTaskExecutor {
        return getContext().getBean("taskExecutor") as ThreadPoolTaskExecutor

    }

    fun cleanTag(tag: String): String {
        return tag.substringAfter('#')
    }

    fun getGridFs(mongoTemplate: MongoTemplate): GridFS {
        return GridFS(mongoTemplate.db)
    }

    fun genMediaTypeFromContentType(contentType: String) : Char {
        // so we know the video part we only have to check for the
        // pdf and the pictures
        return when (contentType.substringAfter(".")) {
            "pdf", "docx", "ppt", "xls", "txt", "doc"  -> {
                Media.DOCS_TYPE
            }
            else -> {
                Media.PICTURE_TYPE
            }
        }
    }

    fun genMimeTypeForVideo(filename: String): String {
        return when (filename.substringAfter(".")) {
            "mp4" -> {
                "video/mp4"
            }
            "3gp" -> {
                "video/3gpp"
            }
            else -> "video/*"
        }
    }

    fun saveQuestionOnElasticOnANewThread( elasticQuestionService: ElasticQuestionService,
                                                   taskExecutor: ThreadPoolTaskExecutor,
                                                   question: Question) {
        taskExecutor.execute({
            updateElasticQuestion(elasticQuestionService, question)
        })
    }

    private fun updateElasticQuestion(elasticQuestionService: ElasticQuestionService, question: Question) {
        elasticQuestionService.saveQuestionToElastic(question)
    }

    fun createVoteEntity(voteEntityBridgeRepo: VoteEntityBridgeRepository, id : Pair<String, String>, voteDirection: Boolean): VoteEntityBridge {
        if (voteEntityBridgeRepo.exists(id)) {
            return voteEntityBridgeRepo.findOne(id)
        } else {
            // if the user voted the other direction then we update the direction to the new direction
            return voteEntityBridgeRepo.save(VoteEntityBridge(id, voteDirection))
        }
    }


    fun sendPushNotifications(userRepository: UserRepository,
                              notification: SendPushNotificationsService,
                              question: Question,
                              answer: Answer? = null) {

        val userSet = HashSet<User>()
        for (tag in question.tags) {
            print("for this tag $tag")
            val users = userRepository.findAllByTag(tag.name)
            println(" found $users")
            userSet.addAll(users)
        }
        println(" and the user userSet $userSet")
        notification.notifyOnNewQuestion(question, userSet)
    }

}
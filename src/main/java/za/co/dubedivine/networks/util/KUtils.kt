package za.co.dubedivine.networks.util

import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import org.json.JSONObject
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import za.co.dubedivine.networks.config.AppConfig
import za.co.dubedivine.networks.model.*
import za.co.dubedivine.networks.model.elastic.ElasticTag
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import za.co.dubedivine.networks.repository.TagRepository
import za.co.dubedivine.networks.repository.UserRepository
import za.co.dubedivine.networks.repository.VoteEntityBridgeRepository
import za.co.dubedivine.networks.services.AndroidPushNotificationService
import za.co.dubedivine.networks.services.elastic.ElasticQuestionService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.regex.Pattern


//TODO object class can easily be a just a file with methods
object KUtils {

    private const val HASH_TAGS_REGEX = "#(\\d*[A-Za-z_]+\\w*)\\b(?!;)"
    private const val TOPIC = "Peerlink"

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

    private fun getPattern(): Pattern = Pattern.compile(HASH_TAGS_REGEX)


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



    fun getThreadPoolExecutor(): ThreadPoolTaskExecutor {

        fun getContext(): AnnotationConfigApplicationContext {
            return AnnotationConfigApplicationContext(AppConfig::class.java)
        }

        return getContext().getBean("taskExecutor") as ThreadPoolTaskExecutor

    }

    fun cleanTag(tag: String): String {
        return tag.substringAfter('#')
    }

    fun genMediaTypeFromContentType(contentType: String) : Char {
        // so we know the video part we only have to check for the
        // pdf and the pictures
        return when (contentType.substringAfter(".")) {
            "pdf", "docx", "ppt", "xls", "txt", "doc"  -> {
                Media.DOCS_TYPE
            }
            else -> {   // todo: would rather check if this if its an image then default to docs
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
        taskExecutor.execute {
            updateElasticQuestion(elasticQuestionService, question)
        }
    }

    private fun updateElasticQuestion(elasticQuestionService: ElasticQuestionService, question: Question) {
        elasticQuestionService.saveQuestionToElastic(question)
    }

    fun createVoteEntity(voteEntityBridgeRepo: VoteEntityBridgeRepository, id : Pair<String, String>, voteDirection: Boolean): VoteEntityBridge {
        return if (voteEntityBridgeRepo.existsById(id)) {
            val voteEntity = voteEntityBridgeRepo.findById(id).get()
            voteEntity.isVoteTheSameDirection = voteDirection
            voteEntityBridgeRepo.save(voteEntity)
        } else {
            // if the user voted the other direction then we update the direction to the new direction
            voteEntityBridgeRepo.save(VoteEntityBridge(id, voteDirection))
        }
    }


    fun retrieveUsersInThread(userRepository: UserRepository,
                               question: Question): HashSet<User> {

        val userSet = HashSet<User>()
        for (tag in question.tags) {
            print("for this tag $tag")
            val users = userRepository.findAllByTag(tag.name)
            println(" found $users")
            userSet.addAll(users)
        }
        println(" and the user userSet $userSet")
       return userSet
    }

    private fun createFCMBodyMessage(title: String, messagedBody: String, userFCMToken: String, dat: Data? = null): String {
        val body = JSONObject()
      //  body.put("to", "/topics/$TOPIC")
        body.put("to", userFCMToken)
        body.put("priority", "high")

        val notification = JSONObject()
        notification.put("title", title)
        notification.put("body", messagedBody)

        val data = JSONObject()
        data.put("key-1", dat)

        body.put("notification", notification)
        body.put("data", data)

        val jsonB = body.toString()
        println("JSON=$jsonB")
        return jsonB
    }

    fun notifyUser(androidPushNotificationsService: AndroidPushNotificationService,
                   title: String,
                   messagedBody: String,
                   userFCMToken: String,
                   dat: Data? = null) {
        println("sendig notifictions")

        val jsonB = createFCMBodyMessage(title.take(30), messagedBody.take(360), userFCMToken,  dat) //like a tweet
        val request = HttpEntity(jsonB)

        val pushNotification = androidPushNotificationsService.send(request)
        CompletableFuture.allOf(pushNotification).join()
        try {
            val firebaseResponse = pushNotification.get()
            println("firebase response is $firebaseResponse")
        //    return firebaseResponse.contains("message_id")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }
     //   return false
    }

    inline fun executeJobOnThread(crossinline job: () -> Unit) {
        getThreadPoolExecutor().execute {
            println("putting job on thread")
            job()
        }
    }

    //NB caution this function is misleading read carefully
    // it also assigns user to
    fun getElasticTag(question: Question,
                      user: User,
                      tagRepository: TagRepository,
                      userRepository: UserRepository): ElasticTag? {
        var elasticTag: ElasticTag? = null

        for (it in question.tags) {
            //check if the tag exists if not its equal to null
            val tag = tagRepository.findFirstByName(it.name)
            //todo: bad bro
            elasticTag = if (tag != null) {
                // this means that the tag has already been created
                //we don`t have to do anything more here all we have to do is
                val foundTag = tagRepository.findFirstByName(it.name)
                if (question !in foundTag.questions) {
                    foundTag.addQuestion(question)
                }
                // val savedTag = tagRepository.save(foundTag)
                // the tag exists but then the user might have it
                //todo this is a slow OPERATION WE SHOULD DELEGATE THIS TO THE DB
                if (!user.tags.contains(tag)) {
                    user.addTag(foundTag) //todo this should replaced by a userTagBridge
                }
                KUtils.instantiateElasticTag(foundTag)
            } else { // else create the tag
                it.addQuestion(question)
                val savedTag = tagRepository.save(Tag(it.name))
                // the tag does not exit so the user definitely does not have it
                user.addTag(savedTag)

                KUtils.instantiateElasticTag(savedTag) // the last line returned!!s
            }
        }
        //update the user as well
        userRepository.save(userRepository.findById(user.id).get())
        return elasticTag
    }
}

enum class ENTITY_TYPE {
    QUESTION, ANSWER, QUESTION_COMMENT, ANSWER_COMMENT, QUESTION_VOTE, ANSWER_VOTE
}

data class Data(private val itemId: String, private val entity_TYPE: ENTITY_TYPE,  private val msg: String? = null) {
    override fun toString(): String {
        val j = JSONObject()

        j.put("itemId", itemId)
        j.put("entity", entity_TYPE)
        j.put("msg", msg)

        return j.toString()
    }
}


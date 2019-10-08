package za.co.dubedivine.networks.controller

import com.mongodb.BasicDBObject
import com.mongodb.client.gridfs.model.GridFSFile
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import za.co.dubedivine.networks.config.AppConfig
import za.co.dubedivine.networks.model.*
import za.co.dubedivine.networks.model.elastic.ElasticQuestion
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import za.co.dubedivine.networks.repository.QuestionRepository
import za.co.dubedivine.networks.repository.TagRepository
import za.co.dubedivine.networks.repository.UserRepository
import za.co.dubedivine.networks.repository.VoteEntityBridgeRepository
import za.co.dubedivine.networks.repository.elastic.ElasticTagRepo
import za.co.dubedivine.networks.services.AndroidPushNotificationService
//import za.co.dubedivine.networks.services.AndroidPushNotificationService
import za.co.dubedivine.networks.services.elastic.ElasticQuestionService
import za.co.dubedivine.networks.util.Data
import za.co.dubedivine.networks.util.ENTITY_TYPE
import za.co.dubedivine.networks.util.KUtils
import java.util.*
import za.co.dubedivine.networks.util.KUtils.saveQuestionOnElasticOnANewThread

//todo: handling invalid data an duplicate data
//todo: split tags and questions
// todo: make pageable offset etc
@RestController
@RequestMapping("questions")
class QuestionsController(private val repository: QuestionRepository,
                          private val tagRepository: TagRepository,
                          private val elasticQuestionService: ElasticQuestionService,
                          private val elasticTagRepo: ElasticTagRepo,
                          private val userRepository: UserRepository,
                          private val voteEntityBridgeRepo: VoteEntityBridgeRepository,
                          private val gridFSOperations: GridFsOperations,
                          private val androidPushNotifications: AndroidPushNotificationService) {

    private final val context = AnnotationConfigApplicationContext(AppConfig::class.java)
    // could just inject this
    private final val taskExecutor = context.getBean("taskExecutor") as ThreadPoolTaskExecutor
    //TODO: Google post vs put
    val allQuestions: List<Question>
        @GetMapping
        get() {
            val sort = Sort(Sort.Direction.DESC, "createdAt")
            return repository.findAll(sort)
        }

    @GetMapping("/{q_id}")
    fun getQuestion(@PathVariable("q_id") questionId: String): ResponseEntity<StatusResponseEntity<Question>> {

        val q = repository.findByIdOrNull(questionId)
        return ResponseEntity(StatusResponseEntity(q != null,
                if (q == null) "could not find question" else "", q ?: Question()), HttpStatus.CREATED)
    }

    //needs a major refactoring
    @PutMapping //adding anew entity
    fun addQuestion(@RequestBody question: Question): ResponseEntity<StatusResponseEntity<Question>> {


        val user = userRepository.findByIdOrNull(question.user.id)
                ?: return ResponseEntity(StatusResponseEntity(false,
                "Sorry we could not ask your question " +
                        "because this user does not exist", Question()), HttpStatus.BAD_REQUEST)
        //giving the user a tag a and also instantiating an elastic that tag
        question.user = user
        val q = repository.insert(question)
        val elasticTagToSave = KUtils.getElasticTag(q, user, tagRepository, userRepository)

        taskExecutor.execute {
            //should stop auto enable mongo and then i can create a mongo template
            val elasticQuestion = ElasticQuestion(q.title, q.body, q.votes, q.tags, q.type)
            elasticQuestion.id = q.id
            elasticQuestion.user = user
            //   userRepository.save(user) // update the tags for this user
            //sends notification to the users

            elasticQuestionService.save(elasticQuestion)
            elasticTagRepo.save(elasticTagToSave)
            val users = KUtils.retrieveUsersInThread(userRepository, q)
            println("trying executing coolness")
            for (usr in users) {
                KUtils.notifyUser(androidPushNotifications,
                        "Q: ${q.title}",
                        q.body,
                        usr.fcmToken,
                        Data(q.id, ENTITY_TYPE.QUESTION))
            }
        }

        val uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(question.id).toUri()
        val httpHeaders = HttpHeaders()
        httpHeaders.location = uri

        return ResponseEntity(StatusResponseEntity(true,
                "saved question", q), httpHeaders, HttpStatus.CREATED)
    }


    @PostMapping //for editing
    fun editQuestion(@RequestBody question: Question) {
        //todo: should also edit the elastic search data
        repository.save(question)
    }


    //todo: add some point we need to add the files to elastic + Tika so that we can search thru them
    /*
     * so this function should actually handle both uploading of images and videos
     * and also uploading of many documents
     *
     */
    @PostMapping("/{q_id}/files")
    fun addFiles(@PathVariable("q_id") questionId: String,
                 @RequestPart files: List<MultipartFile>):
            ResponseEntity<StatusResponseEntity<Question>> {
        val question = repository.findByIdOrNull(questionId)
        val metaData = BasicDBObject()
        metaData["questionId"] = questionId
        if (question != null) {
//            println("the bucket name is:  ${fs.bucketName} and the db:  ${fs.db}")
            if (files.size == 1 && KUtils.isFileAVideo(files[0].contentType)) {  //not the best way of checking, but i know the client will restrict this

                val mime = KUtils.genMimeTypeForVideo(files[0].originalFilename)
                metaData[HttpHeaders.CONTENT_TYPE] = mime
                val createFile = gridFSOperations.store(
                        files[0].inputStream,
                        files[0].originalFilename, mime, metaData)
                println("mime is: $mime")
                println("the is of the file is: $createFile")
                question.video = Media(files[0].originalFilename, Media.VIDEO_TYPE, createFile.toString())
                val savedQuestion = repository.save(question)
                saveQuestionOnElasticOnANewThread(elasticQuestionService, taskExecutor, savedQuestion)
                return ResponseEntity(StatusResponseEntity(
                        true, "file created", savedQuestion), HttpStatus.CREATED)
            } else { // this application type is
                val docs: ArrayList<Media> = arrayListOf()
                files.forEach {
                    metaData[HttpHeaders.CONTENT_TYPE] = it.contentType
                    val createFile = gridFSOperations.store(it.inputStream, it.originalFilename, it.contentType, metaData)
                    println("mime is: ${it.contentType}")
                    println("the is of the file is: $createFile")
                    //need to change this to map to the proper mime
                    docs.add(Media(
                            it.originalFilename,
                            KUtils.genMediaTypeFromContentType(it.contentType),
                            createFile.toString()))
                }
                question.files = docs
                val savedQuestion = repository.save(question)
                saveQuestionOnElasticOnANewThread(elasticQuestionService, taskExecutor, savedQuestion)
                return ResponseEntity(StatusResponseEntity(true, "files created", question), HttpStatus.CREATED)
            }
        } else {
            return ResponseEntity(StatusResponseEntity<Question>(false,
                    "sorry could not add files because we could not find that question"), HttpStatus.NOT_FOUND)
        }
    }

    //todo: should append type of file here as well
    //function to get the files for a specific question
    @GetMapping("/{q_id}/files")
    fun getFile(@PathVariable("q_id") questionId: String,
                @RequestParam("type") type: String): ResponseEntity<Resource> {
        println("the question ID is $questionId and the type is $type")
//        val question = repository.findOne(questionId)

        val findOne: GridFSFile = gridFSOperations.findOne(Query.query(Criteria.where("_id").`is`(questionId)))
        val resource = InputStreamResource(gridFSOperations.getResource(findOne).inputStream)
        println("found one $resource")
//                val headers = ""
        return ResponseEntity.ok()
                .contentLength(findOne.length)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource)
    }

    @GetMapping("/tag_search")
    fun getTagQuestions(@RequestParam("tag_name") tagName: String): Set<Question> {
        return repository.findByTagsName(tagName).toSet()
    }

    //todo:use elastic search please link is here https://www.mkyong.com/spring-boot/spring-boot-spring-data-elasticsearch-example/
    //the search feature you can search by tag #hello or (question name) or just question
    //todo: should have a go deeper flag signifying that maybe we should also search in the answers as well
    //todo: http://ufasoli.blogspot.co.za/2013/08/mongodb-spring-data-elemmatch-in-field.html

    @GetMapping("/search") // elastic search
    fun search(@RequestParam("text") searchText: String): Set<ElasticQuestion> {
        println("the search term is $searchText")
        if (!KUtils.hasTags(searchText)) {
            println("in 1st phase ")
            val result = elasticQuestionService.search(searchText).toSet()
            println("the returned result is $result")
            return result
        } else {
            println("in 2nd phase")
            val (purifiedTitle, tags) = KUtils.getCleanTextAndTags(searchText)
            println("the purified tags is $purifiedTitle")
            println("there are ${tags.size} tags so looping ")

            var tag = getTag(tags, 0)
            var tag1 = getTag(tags, 1)
            var tag2 = getTag(tags, 2)
            var tag3 = getTag(tags, 3)

            val elasticQuestions = elasticQuestionService.searchWithQuestionTag(purifiedTitle, tag, tag1, tag2, tag3)
            return  elasticQuestions.toSet()
        }
    }

    //todo: should also delete from elastic search
    @DeleteMapping("/{q_id}") //questions/2
    fun deleteQuestion(@PathVariable("q_id") questionId: String) {
        //todo: should actually have another collection called deleted stuff where we move this stuff to
        repository.deleteById(questionId)

    }

    @PostMapping("/{q_id}/comment")
    fun commentOnQuestion(@PathVariable("q_id") questionId: String,
                          @RequestBody comment: Comment): ResponseEntity<StatusResponseEntity<Answer>> {
        val question = repository.findByIdOrNull(questionId)
        // todo: what happens if we cannot find the question!!!, it will crash and return a very hepfull spring msg
        val user = userRepository.findByIdOrNull(comment.user.id)
        if (question != null && user != null) {
            comment.user = user
            val comments = question.comments
            comments.add(comment)
            repository.save(question)

            KUtils.executeJobOnThread {
                KUtils.getElasticTag(question, user, tagRepository, userRepository)
                val users = KUtils.retrieveUsersInThread(userRepository, question)
                //notify users
                for (usr in users) {
                    println("looping at user $usr")
                    KUtils.notifyUser(androidPushNotifications,
                            "New comment. Q: ${question.title}",
                            comment.body,
                            usr.fcmToken, Data(question.id, ENTITY_TYPE.QUESTION_COMMENT))
                }
            }

            return ResponseEntity(StatusResponseEntity<Answer>(true,
                    "Comment added on question", null),
                    HttpStatus.OK)
        } else {
            return KUtils.respond(false, "Sorry could not add comment to question", Answer())
        }
    }

    @PostMapping("/{q_id}/vote") //updating
    fun voteQuestion(@PathVariable("q_id") questionId: String,
                     @RequestParam("vote") vote: Boolean,
                     @RequestParam("user_id") userId: String): ResponseEntity<StatusResponseEntity<Answer>> { // <answer??> any ways its null

        val voteDirection = voteEntityBridgeRepo.findByIdOrNull(Pair(questionId, userId))?.isVoteTheSameDirection == vote

        return when {
        //if the user has already voted
            voteDirection -> ResponseEntity(StatusResponseEntity(
                    false,
                    "No vote casted mate", Answer()),
                    HttpStatus.OK)
            else -> {
                KUtils.createVoteEntity(voteEntityBridgeRepo, Pair(questionId, userId), vote)
                val question = repository.findByIdOrNull(questionId)
                if (question != null) {
                    if (vote) question.votes = question.votes + 1 else question.votes = question.votes - 1
                    repository.save(question)

                    // send this task to a thread
                    KUtils.executeJobOnThread {
                        userRepository.findByIdOrNull(userId)?.let { votingUser ->
                            KUtils.getElasticTag(question, votingUser, tagRepository, userRepository)
                            KUtils.notifyUser(androidPushNotifications,
                                    "${votingUser.nickname} ${if(vote) "up" else "down"} voted a question you asked",
                                    question.title,
                                    userRepository.findById(question.user.id).get().fcmToken, // notify the the owner of the question
                                    Data(question.id, ENTITY_TYPE.QUESTION_VOTE))
                        }

                    }
                    ResponseEntity(StatusResponseEntity<Answer>(true,
                            "Vote ${if (vote) "added" else "removed"} ", null),
                            HttpStatus.OK)
                } else {
                    ResponseEntity(StatusResponseEntity(false,
                            "Sorry could not find the question that you want to vote on", Answer()),
                            HttpStatus.BAD_REQUEST)
                }

            }
        }
    }

    private fun getTag(tags: Set<String>, index: Int): String {
        return try { tags.elementAt(index) } catch (iob: IndexOutOfBoundsException) { "" }
    }
}

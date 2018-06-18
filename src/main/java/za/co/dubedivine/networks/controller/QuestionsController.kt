package za.co.dubedivine.networks.controller

import com.mongodb.gridfs.GridFSDBFile
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
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

//import za.co.dubedivine.networks.util.KUtils.retrieveUsersInThread

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
                          private val mongoTemplate: MongoTemplate,
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

        val q = repository.findOne(questionId)
        return ResponseEntity(StatusResponseEntity(q != null,
                if (q == null) "could not find question" else "", q), HttpStatus.CREATED)
    }

    //needs a major refactoring
    @PutMapping //adding anew entity
    fun addQuestion(@RequestBody question: Question): ResponseEntity<StatusResponseEntity<Question>> {
        val user = userRepository.findOne(question.user.id)
        //giving the user a tag a and also instantiating an elastic that tag
        question.user = user
        val q = repository.insert(question)
        val elasticTagToSave = KUtils.getElasticTag(q, user, tagRepository, userRepository)

        taskExecutor.execute(object : Runnable {
            override fun run() {
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
                            q.title,
                            q.body,
                            usr.fcmToken,
                            Data(q.id, ENTITY_TYPE.QUESTION))
                }
            }
        })

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
        val question = repository.findOne(questionId)
        if ((question) != null) {
            val fs = getGridFSInstance()

            println("the bucket name is:  ${fs.bucketName} and the db:  ${fs.db}")
            if (files.size == 1 && KUtils.isFileAVideo(files[0].contentType)) {  //not the best way of checking, but i know the client will restrict this
                val createFile = fs.createFile(files[0].inputStream, files[0].originalFilename, true)
                val mime = KUtils.genMimeTypeForVideo(files[0].originalFilename)
                println("mime is: $mime")
                createFile.contentType = mime
                createFile.put("questionId", questionId.toString())
                createFile.save()
                println("the is of the file is: ${createFile.id}")
                question.video = Media(files[0].originalFilename, createFile.length, Media.VIDEO_TYPE, createFile.id.toString())
                val savedQuestion = repository.save(question)
                saveQuestionOnElasticOnANewThread(elasticQuestionService, taskExecutor, savedQuestion)
                return ResponseEntity(StatusResponseEntity(
                        true, "file created", savedQuestion), HttpStatus.CREATED)
            } else { // this application type is
                val docs: ArrayList<Media> = arrayListOf()
                files.forEach {
                    val createFile = fs.createFile(it.inputStream, it.originalFilename, true)
                    //need to change this to map to the proper mime
                    createFile.contentType = it.contentType
                    createFile.put("questionId", questionId.toString())
                    createFile.save()
                    docs.add(Media(
                            it.originalFilename,
                            createFile.length,
                            KUtils.genMediaTypeFromContentType(createFile.contentType),
                            createFile.id.toString()))
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

    // could make this a property
    private fun getGridFSInstance() = KUtils.getGridFs(mongoTemplate)

    //todo: should append type of file here as well
    //function to get the files for a specific question
    @GetMapping("/{q_id}/files")
    fun getFile(@PathVariable("q_id") questionId: String,
                @RequestParam("type") type: String): ResponseEntity<Resource> {
        println("the question ID is $questionId and the type is $type")
        val fs = getGridFSInstance()
//        val question = repository.findOne(questionId)

        val findOne: GridFSDBFile = fs.findOne(questionId)
        val resource = InputStreamResource(findOne.inputStream)
        println("found one $resource")
//                val headers = ""
        return ResponseEntity.ok()
                .contentLength(findOne.length)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource)
    }

    @GetMapping("/tag_search?t_id={tag_name}")
    fun getTagQuestions(@PathVariable("tag_name") tagName: String): Set<Question> {
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
            val list: MutableList<ElasticQuestion> = mutableListOf()
            println("the purified text is $purifiedTitle")
            println("there are ${tags.size} tags so looping ")
            tags.forEachIndexed { index, tag ->
                println("loop $index")
                val elasticQuestions = elasticQuestionService.searchWithQuestionTag(purifiedTitle, tag)  //todo: should use Rx
                println("the list is $elasticQuestions")
                list.addAll(elasticQuestions)
            }
            val set = list.toSet()
            println("the returned result is $set")
            return set
        }
    }

    //todo: should also delete from elastic search
    @DeleteMapping("/{q_id}") //questions/2
    fun deleteQuestion(@PathVariable("q_id") questionId: String) {
        //todo: should actually have another collection called deleted stuff where we move this stuff to
        repository.delete(questionId)

    }

    @PostMapping("/{q_id}/comment")
    fun commentOnQuestion(@PathVariable("q_id") questionId: String,
                          @RequestBody comment: Comment): ResponseEntity<StatusResponseEntity<Answer>> {
        val question = repository.findOne(questionId)
        // todo: what happens if we cannot find the question!!!, it will crash and return a very hepfull spring msg
        val user = userRepository.findOne(comment.user.id)
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
                        "CQ: ${question.title}",
                        comment.body,
                        usr.fcmToken, Data(question.id, ENTITY_TYPE.QUESTION_COMMENT))
            }
        }

        return ResponseEntity(StatusResponseEntity<Answer>(true,
                "Comment added on question", null),
                HttpStatus.OK)
    }

    @PostMapping("/{q_id}/vote") //updating
    fun voteQuestion(@PathVariable("q_id") questionId: String,
                     @RequestParam("vote") vote: Boolean,
                     @RequestParam("user_id") userId: String): ResponseEntity<StatusResponseEntity<Answer>> { // <answer??> any ways its null

        val voted = try {
            val voteDirection = voteEntityBridgeRepo.findOne(Pair(questionId, userId)).isVoteTheSameDirection == vote
            voteEntityBridgeRepo.exists(Pair(questionId, userId)) && voteDirection
        } catch (npe: NullPointerException) {
            false
        }
        return when {
        //if the user has already voted
            voted -> ResponseEntity(StatusResponseEntity<Answer>(false,
                    "No vote casted mate", null),
                    HttpStatus.OK)
            else -> {
                KUtils.createVoteEntity(voteEntityBridgeRepo, Pair(questionId, userId), vote)
                val question = repository.findOne(questionId)
                if (vote) question.votes = question.votes + 1 else question.votes = question.votes - 1
                repository.save(question)

                // send this task to a thread
                KUtils.executeJobOnThread {
                    KUtils.getElasticTag(question, userRepository.findOne(userId), tagRepository, userRepository)
                    KUtils.notifyUser(androidPushNotifications,
                            "Q: ${question.title}",
                            question.body,
                            question.user.fcmToken, // notify the the owner of the question
                            Data(question.id, ENTITY_TYPE.QUESTION_VOTE))
                }

                ResponseEntity(StatusResponseEntity<Answer>(true,
                        "Vote ${if (vote) "added" else "removed"} ", null),
                        HttpStatus.OK)
            }
        }
    }
}

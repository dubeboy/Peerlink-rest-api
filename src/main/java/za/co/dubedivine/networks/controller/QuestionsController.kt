package za.co.dubedivine.networks.controller

import com.mongodb.DB
import com.mongodb.Mongo
import com.mongodb.MongoClient
import com.mongodb.QueryBuilder
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import com.mongodb.gridfs.GridFSInputFile
import com.querydsl.core.types.dsl.ListPath
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.TextCriteria
import org.springframework.data.mongodb.core.query.TextQuery
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import za.co.dubedivine.networks.model.*
import za.co.dubedivine.networks.model.repository.QuestionRepository
import za.co.dubedivine.networks.model.repository.TagRepository
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import za.co.dubedivine.networks.util.KUtils
import java.util.*
import java.util.regex.Pattern

//todo: handling invalid data an dublicate data
//todo: split tags and questions
// todo: make pageable offset etc
@RestController
@RequestMapping("questions")
class QuestionsController(private val repository: QuestionRepository,
                          private val tagRepository: TagRepository) {
    //TODO: Google post vs put
    val allQuestions: List<Question>
        @GetMapping
        get() {
            val sort = Sort(Sort.Direction.DESC, "createdAt")
            return repository.findAll(sort)
        }

    @PutMapping //adding anew entity
    fun addQuestion(@RequestBody question: Question): ResponseEntity<Any> {
        question.tags.forEach { questionItem ->
            val tag = tagRepository.findFirstByName(questionItem.name)
            fun check() = if(questionItem.questionIds == null) {
                questionItem.questionIds = setOf(questionItem.id)
            } else {
                tag.addQuestionIdToTag(questionItem.id)
            }
            if (tag != null) { // this means that the tag has already been created
                //we dont have to do anything more here all we have to do is
                // we just have to set the ID of this tag in questionItem to the one that exits
                questionItem.id = tag.id
                check()
                tagRepository.save(tag)
            } else { // else create the tag
                check()
                val savedTag = tagRepository.save(questionItem)
                questionItem.id = savedTag.id  // after creating the tag combine the tag wth the Q
            }
        }
        repository.insert(question)
        val uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(question.id).toUri()
        val httpHeaders = HttpHeaders()
        httpHeaders.location = uri

        return ResponseEntity<Any>(null, httpHeaders, HttpStatus.CREATED)
    }

    @PostMapping //for editing
    fun editQuestion(@RequestBody question: Question) {
        repository.save(question)
    }

    /**
     * so this function should actually handle both uploading of images and videos
     * and also uploading of many documents
     * */
    @PostMapping("/{q_id}/files")
    fun addFiles(@PathVariable("q_id") questionId: String,
                 @RequestParam("file") files: List<MultipartFile>): ResponseEntity<StatusResponseEntity> {
        val question = repository.findOne(questionId)
        if ((question) != null) {
            //todo: i gues this code is bad because it open a nother connection it was not supposed to do that!!
            val fs = getGridFSInstance()

            println("the bucket name is:  ${fs.bucketName} and the db:  ${fs.db}")
            if (files.size == 1) {  //not the best way of checking, but i know the client will restrict this
                val createFile: GridFSInputFile = fs.createFile(
                        files[0].inputStream,
                        questionId, //sae it with the question ID
                        true)
                createFile.save()
                val id = createFile.id
                println("the is of the file is: $id")
                question.video = Media(
                        files[0].originalFilename,
                        createFile.length,
                        createFile.contentType,
                        createFile.id.toString())
                repository.save(question)
                return ResponseEntity(StatusResponseEntity(
                        true,
                        "file created"),
                        HttpStatus.CREATED)
            } else { // this application type is
                val docs: ArrayList<Media> = arrayListOf()
                files.forEach {
                    val createFile: GridFSInputFile = fs.createFile(
                            it.inputStream,
                            questionId, true)
                    createFile.save()
                    docs.add(Media(it.originalFilename,
                            createFile.length,
                            createFile.contentType,
                            createFile.id.toString()))
                }
                question.files = docs
                repository.save(question)
                return ResponseEntity(StatusResponseEntity(true,
                        "files created"), HttpStatus.CREATED)
            }
        } else {
            return ResponseEntity(StatusResponseEntity(true,
                    "sorry could not add files"), HttpStatus.CREATED)
        }

    }

    private fun getGridFSInstance(): GridFS {
        // get the application context here and then hook to the mongo template and then you get all connections
        // session properties

        val mongo = MongoClient("localhost", 27017)
        val db: DB = mongo.getDB("NetworksDb") // ahh its even deprecated!!!
        return GridFS(db)
    }

    //function to get the files for a specific question
    @GetMapping("/{q_id}/files")
    fun getFile(@PathVariable("q_id") questionId: String,
                @RequestParam("type") type: String): ResponseEntity<Resource> {
        println("the question ID is $questionId and the type is $type")
        val fs = getGridFSInstance()
//        val question = repository.findOne(questionId)
        return when (type) {
            "F" -> {
                val list: MutableList<GridFSDBFile> = fs.find(questionId)
                list.forEach {

                }
                ResponseEntity.ok().body(null)
            } // F for file
            else -> {  // i guess this wil work for any file
                val findOne: GridFSDBFile = fs.findOne(questionId)
                val resource = InputStreamResource(findOne.inputStream)
                println("found one $resource")
//                val headers = ""
                ResponseEntity.ok()
                        .contentLength(findOne.length)
                        .contentType(MediaType.parseMediaType("application/octet-stream"))
                        .body(resource)
            } // M for media
        }
    }

    @GetMapping("/search?t_id={t_id}")
    fun getTagQuestions(@PathVariable("t_id") tagId: String): Set<Question> {
        val tag = tagRepository.findFirstByName(tagId)
        val questions = repository.findAll()  //todo: needs to be fixed asap
        val ques = hashSetOf<Question>()
        questions.forEach { q ->
            val tags = q.tags
            for (it in tags) {
                if (it == tag) {
                    ques.add(q)
                    break
                }
            }
        }
        return ques
    }

    //todo:use elastic search please link is here https://www.mkyong.com/spring-boot/spring-boot-spring-data-elasticsearch-example/
    //the search feature you can search by tag #hello or (question name) or just question
    //todo: should have a go deeper flag signifying that maybe we should also search in the answers as well
    @GetMapping("/search?{text}")
    fun search(@PathVariable("text") searchText: String): Set<Question> {
        val p = Pattern.compile(KUtils.REGEX) //pattern to match the has tags
        val ques = setOf<Question>()
        val purifiedSearchText = KUtils.cleanText(searchText)
        val mongoTemplate = MongoTemplate(Mongo(), "NetworksDb")

        if (p.toRegex().containsMatchIn(searchText)) {
//            val qQuestion = QQuestion("qquestion")
            var find: MutableList<Question> = mutableListOf()
            p.toRegex().findAll(searchText).forEach { tagString ->
//                val tags: ListPath<Tag, QTag> = qQuestion.tags
                QueryBuilder.start("question").exists("tag")
                val criteria: Criteria = Criteria.where("question.tags").elemMatch(Criteria.where("name").`is`(tagString))
                val query = Query.query(criteria)
                println(query.fields())
                find = mongoTemplate.find(query, Question::class.java)
            }
            return find.toHashSet()
        } else {  // NO TAGS SEARCH THROUGH ALL THE QUESTIONS BOSS
            val textCriteria: TextCriteria = TextCriteria
                    .forDefaultLanguage()
                    .matchingAny(purifiedSearchText)  //todo should test if this works

            val query: Query = TextQuery.queryText(textCriteria)
                    .sortByScore()
                    .with(PageRequest(0, 30))   //todo: should change page page params
            //todo: bad man!!
            val list: MutableList<Question> = mongoTemplate.find(query, Question::class.java)  //mongo template is deprected
            return list.toSet()
        }


    }


    @DeleteMapping("/{q_id}") //questions/2
    fun deleteQuestion(@PathVariable("q_id") questionId: String) {
        //todo: should actually have another collection called deleted stuff where we move this stuff to
        repository.delete(questionId)
    }
}

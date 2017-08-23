package za.co.dubedivine.networks.controller

import com.mongodb.DB
import com.mongodb.MongoClient
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSDBFile
import com.mongodb.gridfs.GridFSInputFile
import org.bson.types.ObjectId
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.data.domain.Sort
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
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.xml.ws.Response

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
        question.tags.forEach {
            val tag = Tag(it.name)
            val savedTag = tagRepository.save(tag)

            it.id = savedTag.id

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
                 @RequestParam("file") files: List <MultipartFile>): ResponseEntity<StatusResponseEntity> {
        val question = repository.findOne(questionId)
        if ((question) != null) {
            //todo: i gues this code is bad because it open a nother connection it was not supposed to do that!!

            val fs = getGridFSInstance()

            println("the bucket name is:  ${fs.bucketName} and the db:  ${fs.db}")
            if (files.size == 1 ) {  //not the best way of checking, but i know the client will restrict this
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
//                    val uuid = UUID.randomUUID().toString()
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
                return  ResponseEntity(StatusResponseEntity(true,
                        "files created"), HttpStatus.CREATED)
            }
        } else {
            return ResponseEntity(StatusResponseEntity(true,
                    "sorry could not add files"), HttpStatus.CREATED)

        }

    }

    private fun getGridFSInstance(): GridFS {
        val mongo = MongoClient("localhost", 27017)
        val db: DB = mongo.getDB("NetworksDb") // ahh its even deprecated!!!
        return GridFS(db)
    }

    //function to get the files for a specific question
    @GetMapping("/{q_id}/files")
    fun getFile(@PathVariable("q_id") questionId: String,
                @RequestParam("type") type: String) :ResponseEntity<Resource> {
        val fs = getGridFSInstance()
//        val question = repository.findOne(questionId)
        return when(type) {
            "F" -> {
                val list: MutableList<GridFSDBFile> = fs.find(questionId)
                list.forEach {

                }
                 ResponseEntity.ok().body(null)
            } // F for file
            "M" -> {
                val findOne: GridFSDBFile = fs.findOne(questionId)
                val resource: InputStreamResource  = InputStreamResource(findOne.inputStream)
                val headers = ""
                 ResponseEntity.ok()
                        .header(headers)
                        .contentLength(findOne.length)
                        .contentType(MediaType.parseMediaType("application/octet-stream"))
                        .body(resource)
            } // M for media
            else -> {
                ResponseEntity.ok().body(null)
            }
        }
    }





    @DeleteMapping("/{q_id}") //questions/2
    fun deleteQuestion(@PathVariable("q_id") questionId: String) {
        //todo: should actually have another collection called deleted stuff where we move this stuff to
        repository.delete(questionId)
    }
}

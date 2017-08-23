package za.co.dubedivine.networks.controller

import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.MongoClient
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSInputFile
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import za.co.dubedivine.networks.model.*
import za.co.dubedivine.networks.model.repository.QuestionRepository
import za.co.dubedivine.networks.model.repository.TagRepository
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity

import java.util.ArrayList

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

            val mongo = MongoClient("localhost", 27017)
            val db: DB = mongo.getDB("NetworksDb") // ahh its even deprecated!!!
            val fs = GridFS(db)
            val metaData = BasicDBObject()

            metaData.put("q_id", questionId)
            println("the bucket name is:  ${fs.bucketName} and the db:  ${fs.db}")
            if (files.size == 1) {
                val createFile: GridFSInputFile = fs.createFile(
                        files[0].inputStream,
                        files[0].originalFilename,
                        true)
                createFile.save()
                val id = createFile.id
                println("the is of the file is: $id")
                question.video = Media(
                        createFile.filename,
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
                            it.originalFilename, true)
                    createFile.save()
                    docs.add(Media(createFile.filename,
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


    @DeleteMapping("/{q_id}") //questions/2
    fun deleteQuestion(@PathVariable("q_id") questionId: String) {
        //todo: should actually have another collection called deleted stuff where we move this stuff to
        repository.delete(questionId)
    }
}

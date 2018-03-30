package za.co.dubedivine.networks.controller

import com.mongodb.BasicDBObject
import com.mongodb.gridfs.GridFSDBFile
import org.bson.types.ObjectId
import org.springframework.core.io.ByteArrayResource
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import sun.misc.IOUtils
import za.co.dubedivine.networks.util.KUtils
import java.io.*
import java.io.IOException


@RestController
@RequestMapping() // I would like to make the request look like its just getting a static resource
class FileDownloaderKontroller(mongoTemplate: MongoTemplate) {

    private val fsInstance = KUtils.getGridFs(mongoTemplate)
    @GetMapping("/{id}")
    fun getFile(@PathVariable("id") id: String): ResponseEntity<ByteArrayResource> {
        try {
            println("the Id id $id")
            val query = BasicDBObject()
            query["_id"] = ObjectId(id)
            val gridFSDBFile: GridFSDBFile? = fsInstance.findOne(query)

            if (gridFSDBFile != null && gridFSDBFile.length != 0L) {

                val inputStream = BufferedInputStream(gridFSDBFile.inputStream)
                println("input stream available is ${inputStream.available()}")
                val byteArray = getBytes(inputStream)
                val byteArrayResource = ByteArrayResource(byteArray)

//                IOUtils.readFully()


                val contentType = gridFSDBFile.contentType
                val filename = gridFSDBFile.filename

                println("the content Type is $contentType and the file name is $filename")
                println("the sizes ${byteArray.size} and the actual size from mongo is ${gridFSDBFile.length} ")

                val headers = HttpHeaders()
                headers.set(HttpHeaders.CONTENT_TYPE, contentType);
                headers.set(HttpHeaders.CONTENT_DISPOSITION, """attachment; filename="$filename"""")
                headers.set(HttpHeaders.CONTENT_LENGTH, byteArray.size.toString())

                return ResponseEntity(byteArrayResource, headers, HttpStatus.OK)
            } else {
                println("sorry could not find that file")
                return ResponseEntity(HttpStatus.NO_CONTENT)
            }
        } catch (ia: IllegalArgumentException) {
            println("sorry could not find that file")
            return ResponseEntity(HttpStatus.NO_CONTENT)
        }


    }

    @Throws(IOException::class)
    fun getBytes(inputStream: InputStream): ByteArray {
        var buf = ByteArray(1024)
        val bos = ByteArrayOutputStream(1025)
        while (inputStream.read(buf) != -1) {
            bos.write(buf)
        }
        buf = bos.toByteArray()
        return buf
    }
}
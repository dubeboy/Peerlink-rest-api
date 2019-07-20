package za.co.dubedivine.networks.controller

import com.mongodb.client.gridfs.model.GridFSFile
import org.springframework.core.io.ByteArrayResource
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.gridfs.GridFsOperations
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream


@RestController
@RequestMapping // I would like to make the request look like its just getting a static resource
class FileDownloaderKontroller(val gridFsOperations: GridFsOperations) {
    @GetMapping("/{id}")
    fun getFile(@PathVariable("id") id: String): ResponseEntity<ByteArrayResource> {
        try {
            println("the Id id $id")
            val gridFSFile: GridFSFile? = gridFsOperations.findOne(query(where("_id").`is`(id)))

            if (gridFSFile != null && gridFSFile.length != 0L) {

                val filename = gridFSFile.filename
                val metaData = gridFSFile.metadata // todo : get it from mine

                val inputStream = gridFsOperations.getResource(gridFSFile).inputStream
                println("input stream available is ${inputStream.available()}")
                val byteArray = getBytes(inputStream)
                val byteArrayResource = ByteArrayResource(byteArray)

                println("the metadata is $metaData the content Type is ${metaData!![HttpHeaders.CONTENT_TYPE]} and the file name is $filename")
                println("the sizes ${byteArray.size} and the actual size from mongo is ${gridFSFile.length} ")

                val headers = HttpHeaders()
                headers.set(HttpHeaders.CONTENT_TYPE, metaData[HttpHeaders.CONTENT_TYPE].toString())
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
        val bos = ByteArrayOutputStream(1024)
        while (inputStream.read(buf) != -1) {
            bos.write(buf)
        }
        buf = bos.toByteArray()
        return buf
    }
}
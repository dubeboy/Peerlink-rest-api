package za.co.dubedivine.networks.util

import com.sun.org.apache.xpath.internal.operations.Bool
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity

object KUtils {
    enum class CONTENT_TYPE {
        VID_IMG, DOC
    }

    fun getFileType(mime: String) : CONTENT_TYPE {
        return if(mime.startsWith("image") || mime.startsWith("video")) {
            CONTENT_TYPE.VID_IMG
        } else {
            CONTENT_TYPE.DOC
        }
    }

    fun respond(status: Boolean,  msg: String) : ResponseEntity<StatusResponseEntity> {
       return ResponseEntity(StatusResponseEntity(status, msg),
               if(status) HttpStatus.CREATED else  HttpStatus.BAD_REQUEST)
    }
}
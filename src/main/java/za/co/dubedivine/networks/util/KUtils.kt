package za.co.dubedivine.networks.util

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import java.util.regex.Pattern

object KUtils {

    const val REGEX = "#(\\d*[A-Za-z_]+\\w*)\\b(?!;)"
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

    fun cleanText(text: String): String {
        val p = Pattern.compile(REGEX)
        return p.matcher(text).replaceAll("")
    }
}
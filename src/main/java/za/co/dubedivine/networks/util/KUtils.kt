package za.co.dubedivine.networks.util

import com.sun.org.apache.xpath.internal.operations.Bool
import com.sun.xml.internal.fastinfoset.util.StringArray
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import java.util.regex.Pattern

object KUtils {

    private const val REGEX = "#(\\d*[A-Za-z_]+\\w*)\\b(?!;)"
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

    fun getCleanTextAndTags(text: String): Pair<String, Set<String>> {
        println("the text is $text")
        val p = getPattern()
        val strings = mutableSetOf<String>()
        val sequence = p.toRegex().findAll(text).distinct()
        sequence.forEach {
            println("yeye thete are tags ${it.value}")
            strings.add(it.value)
        }
        val  clean =  p.matcher(text).replaceAll(" ")
        println("these are the tags fam $strings")
        return Pair(clean, strings)
    }

    fun hasTags(text: String): Boolean {
        val p = getPattern()//pattern to match the has tags
        return p.toRegex().containsMatchIn(text)
    }

    fun getPattern(): Pattern = Pattern.compile(KUtils.REGEX)
}
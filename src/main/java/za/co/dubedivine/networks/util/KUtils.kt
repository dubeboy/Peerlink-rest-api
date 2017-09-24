package za.co.dubedivine.networks.util

import com.sun.org.apache.xpath.internal.operations.Bool
import com.sun.xml.internal.fastinfoset.util.StringArray
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import za.co.dubedivine.networks.config.AppConfig
import za.co.dubedivine.networks.model.Tag
import za.co.dubedivine.networks.model.elastic.ElasticTag
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import java.util.concurrent.ThreadPoolExecutor
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
        val tags = mutableSetOf<String>()
        val sequenceOfTags = p.toRegex().findAll(text).distinct()
        sequenceOfTags.forEach {
            val tag = it.value.substringAfter('#')
            println("yeye thete are tags $tag ")
            tags.add(tag)
        }
        val  cleanSearchText =  p.matcher(text).replaceAll(" ")
        println("these are the tags fam $tags")
        return Pair(cleanSearchText, tags)
    }

    fun hasTags(text: String): Boolean {
        val p = getPattern()//pattern to match the has tags
        return p.toRegex().containsMatchIn(text)
    }

    fun getPattern(): Pattern = Pattern.compile(KUtils.REGEX)

    val instantiateElasticTag: (savedTag: Tag) -> ElasticTag = {
        val elasticTag = ElasticTag(it.name)
        elasticTag.questions = it.questions
        elasticTag
    }

    private fun getContext(): AnnotationConfigApplicationContext {
        return AnnotationConfigApplicationContext(AppConfig::class.java)
    }

    fun getThreadPoolExecutor(): ThreadPoolTaskExecutor {
        return getContext().getBean("taskExecutor") as ThreadPoolTaskExecutor

    }

    fun cleanTag(tag: String): String {
        return tag.substringAfter('#')
    }
}
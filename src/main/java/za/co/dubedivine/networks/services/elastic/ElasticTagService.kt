package za.co.dubedivine.networks.services.elastic

import za.co.dubedivine.networks.model.elastic.ElasticQuestion
import za.co.dubedivine.networks.model.elastic.ElasticTag

interface ElasticTagService {

    fun suggestTag(tagName: String): List<ElasticTag>

    fun suggestTag2(tagName: String): List<ElasticTag>

}
package za.co.dubedivine.networks.repository.elastic

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import za.co.dubedivine.networks.model.elastic.ElasticQuestion

@Repository
interface ElasticQRepo : ElasticsearchRepository<ElasticQuestion, String> {
    fun findByTagsName(tagName: String): List<ElasticQuestion>

//    @Query(""" {
//  "query": {
//    "bool": {
//      "must": [
//        {
//          "match": {
//            "title": ?0
//          }
//        },
//        {
//          "match": {
//            "body": ?0
//          }
//        },
//        {
//          "nested": {
//            "path": "tags",
//            "score_mode": "max",
//            "query": {
//              "bool": {
//                "must": [
//                  {
//                    "match": {
//                      "tags.name": ?1
//                    }
//                  }
//                ]
//              }
//            }
//          }
//        }
//      ]
//    }
//  }
//}
//    """)
    fun findByTitleAndBodyAndTagsName(title: String, tagName: String): Set<ElasticQuestion>
}

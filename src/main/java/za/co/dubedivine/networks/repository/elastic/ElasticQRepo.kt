package za.co.dubedivine.networks.repository.elastic

import org.springframework.data.domain.Page
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import za.co.dubedivine.networks.model.elastic.ElasticQuestion

val query = """
    {
  "query": {
    "bool": {
      "must": {
        "function_score": {
            "query": {
                "multi_match": {
                    "query": "We doing magnets",
                    "fields": [
                        "title^3",
                        "body^2.5"
                    ],
                    "fuzziness" : "2",
                    "prefix_length" : 0,
                    "max_expansions": 100,
                    "boost" :         1.0
                }
            }
        }
      },
      "filter": {
        "nested" : {
            "path" : "tags",
            "query" : {
                       "bool": {
          "should": [
            {
              "match": {
                "tags.name": "phy1a"
              }
            }, 
            {
               "match": {
                "tags.name": "atom"
              }
            }
          ]
        }
            },
            "score_mode" : "avg"
        }
      }
    }
  }
}
""".trimIndent()

@Repository
interface ElasticQRepo : ElasticsearchRepository<ElasticQuestion, String> {

    fun findByTagsName(tagName: String): List<ElasticQuestion>
//
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
//   """)
    @Query("{\"bool\" : {\"must\" : {\"term\" : {\"message\" : \"?0\"}}}}")
    fun findByTitleAndBodyAndTagsName(title: String, tagName: String): Set<ElasticQuestion>
}

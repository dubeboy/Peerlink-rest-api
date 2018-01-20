package za.co.dubedivine.networks.repository.elastic;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import za.co.dubedivine.networks.model.elastic.ElasticTag;

@Repository
public interface ElasticTagRepo extends ElasticsearchRepository<ElasticTag, String> {
}

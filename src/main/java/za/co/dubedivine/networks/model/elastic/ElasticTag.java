package za.co.dubedivine.networks.model.elastic;

import org.springframework.data.elasticsearch.annotations.Document;
import za.co.dubedivine.networks.model.Tag;
import za.co.dubedivine.networks.model.shared.TagBase;

@Document(indexName = "peerlink", type = "tag")
public class ElasticTag extends TagBase {

    public ElasticTag() { }

    public ElasticTag(String name) {
        super(name);
    }
}

package za.co.dubedivine.networks.model.elastic;

import org.springframework.data.elasticsearch.annotations.Document;
import za.co.dubedivine.networks.model.Tag;
import za.co.dubedivine.networks.model.shared.TagBase;

@Document(indexName = "divine_tags", type = "tags")
public class ElasticTag extends TagBase {

    public ElasticTag(String name) {
        super(name);
    }
}

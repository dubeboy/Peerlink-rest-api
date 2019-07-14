package za.co.dubedivine.networks.config


import org.elasticsearch.client.Client
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import java.net.InetAddress
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean


@Configuration
@EnableElasticsearchRepositories(basePackages = ["za.co.dubedivine.networks.repository.elastic"])
class ElasticsearchConfiguration {

//    @Autowired private lateinit var operations: ElasticsearchOperations

    @Value("\${elasticsearch.host}")
    private val esHost: String? = null

    @Value("\${elasticsearch.port}")
    private val esPort: Int = 0

    @Value("\${elasticsearch.clustername}")
    private val esClusterName: String? = null

    @Bean
    fun client(): Client {

        val esSettings = Settings.builder()
                .put("cluster.name", esClusterName)
                .put("client.transport.sniff", true)
                .build()

        //https://www.elastic.co/guide/en/elasticsearch/guide/current/_transport_client_versus_node_client.html
        val client: TransportClient = PreBuiltTransportClient(esSettings)
        client.addTransportAddress(TransportAddress(InetAddress.getByName(esHost), esPort))
        return client
    }

//    @Bean
//    fun elasticsearchTemplate(): ElasticsearchOperations {
//        return ElasticsearchTemplate(client())
//    }
}
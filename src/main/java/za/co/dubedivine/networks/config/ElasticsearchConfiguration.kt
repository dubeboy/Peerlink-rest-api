package za.co.dubedivine.networks.config


import org.elasticsearch.client.Client
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories
import java.net.InetAddress
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean


@Configuration
@EnableElasticsearchRepositories(basePackages = arrayOf("za.co.dubedivine.networks.repository.elastic"))
class ElasticsearchConfiguration {

//    @Autowired private lateinit var operations: ElasticsearchOperations

    @Value("\${elasticsearch.host}")
    private val esHost: String? = null

    @Value("\${elasticsearch.port}")
    private val esPort: Int = 0

    @Value("\${elasticsearch.clustername}")
    private val esClusterName: String? = null

    @Bean
    @Throws(Exception::class)
    fun client(): Client {

        val esSettings = Settings.settingsBuilder()
                .put("cluster.name", esClusterName)
                .build()

        //https://www.elastic.co/guide/en/elasticsearch/guide/current/_transport_client_versus_node_client.html
        return TransportClient.builder()
                .settings(esSettings)
                .build()
                .addTransportAddress(
                        InetSocketTransportAddress(
                                InetAddress.getByName(esHost), esPort))
    }

    @Bean
    @Throws(Exception::class)
    fun elasticsearchTemplate(): ElasticsearchOperations {
        return ElasticsearchTemplate(client())
    }
}
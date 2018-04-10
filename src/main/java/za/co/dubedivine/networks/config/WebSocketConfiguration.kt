package za.co.dubedivine.networks.config

import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer
import org.springframework.web.socket.config.annotation.StompEndpointRegistry

//@Configuration
//@EnableWebSocketMessageBroker
class WebSocketConfiguration : AbstractWebSocketMessageBrokerConfigurer() {
    //STOMP - Simple Text Oriented Messaging Protocol
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // socket we will use to connect to the websocket server
        //
        registry.addEndpoint("/socket")
                .setAllowedOrigins("*")
                .withSockJS() // provide support for browser that do not support sock js
    }


    /*In this method,
     we’re configuring a message broker that will be used
     to route messages from one client to another.*/
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.setApplicationDestinationPrefixes("/app")
                .enableSimpleBroker("/notifications") //this subscribable topic
    }
}
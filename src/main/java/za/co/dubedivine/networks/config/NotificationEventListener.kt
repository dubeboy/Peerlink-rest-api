package za.co.dubedivine.networks.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent

//@Component
class  NotificationEventListener(private val template: SimpMessageSendingOperations) {

//    @EventListener
    fun handleWebSocketCnnectionListener(event: SessionConnectedEvent) {
        logger.info("Recieved a new websocket connection")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(NotificationEventListener::class.java)
    }
}
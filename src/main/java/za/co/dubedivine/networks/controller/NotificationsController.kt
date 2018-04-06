package za.co.dubedivine.networks.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import za.co.dubedivine.networks.model.NotificationsMessage
import java.text.SimpleDateFormat
import java.util.*

@Controller
class NotificationsController(private val template: SimpMessagingTemplate) {
    //  app/send/message
    @MessageMapping("/send/message")
    @SendTo("/notifications/notify")
    fun sendMessage(@Payload  message: NotificationsMessage) {
        template.convertAndSend("/chat",
                SimpleDateFormat("HH:mm:ss").format(Date()) + " - "+ message.message )
    }


}
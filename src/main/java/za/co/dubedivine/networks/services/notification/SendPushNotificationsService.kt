package za.co.dubedivine.networks.services.notification

import org.springframework.stereotype.Service
import za.co.dubedivine.networks.model.Question
import za.co.dubedivine.networks.model.User

// send push notifications service

@Service
class SendPushNotificationsService {
    fun notifyOnNewQuestion(user: Question, userSet: Set<User>) {

    }
}
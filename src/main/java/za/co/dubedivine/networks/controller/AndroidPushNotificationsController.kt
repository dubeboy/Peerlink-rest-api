package za.co.dubedivine.networks.controller


import org.json.JSONObject
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import za.co.dubedivine.networks.services.AndroidPushNotificationService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

@RestController
@RequestMapping("notifications")
class AndroidPushNotificationsController(private val androidPushNotificationsService: AndroidPushNotificationService) {

    @GetMapping("/send")
    private fun send(): ResponseEntity<String> {
        val body = JSONObject()
        body.put("to", "/topics/$TOPIC")
        body.put("priority", "high")

        val notification = JSONObject()
        notification.put("title", "JSA Notification")
        notification.put("body", "Happy Message!")

        val data = JSONObject()
        data.put("Key-1", "JSA Data 1")
        data.put("Key-2", "JSA Data 2")

        body.put("notification", notification)
        body.put("data", data)

        /**
        {
        "notification": {
        "title": "JSA Notification",
        "body": "Happy Message!"
        },
        "data": {
        "Key-1": "JSA Data 1",
        "Key-2": "JSA Data 2"
        },
        "to": "/topics/JavaSampleApproach",
        "priority": "high"
        }
         */

        val jsonB = body.toString()
        println("JSON=$jsonB")
        val request = HttpEntity(jsonB);

        val pushNotification = androidPushNotificationsService.send(request);
        CompletableFuture.allOf(pushNotification).join();

        try {
            val firebaseResponse = pushNotification.get();

            return ResponseEntity(firebaseResponse, HttpStatus.OK)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        return ResponseEntity("Push Notification ERROR!", HttpStatus.BAD_REQUEST);
    }

    companion object {
        private const val TOPIC = "Peerlink"
    }
}
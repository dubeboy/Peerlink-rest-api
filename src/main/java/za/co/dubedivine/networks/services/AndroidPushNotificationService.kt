package za.co.dubedivine.networks.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import za.co.dubedivine.networks.interceptor.ClientRequestInterceptor
import java.util.concurrent.CompletableFuture
import java.util.*


@Service
class AndroidPushNotificationService {

    @Async
    fun send(entity: HttpEntity<String>): CompletableFuture<String> {

        //syncronous bro this block!!!
        val restTemplate = RestTemplate()

        /*
            https://fcm.googleapis.com/fcm/send
            Content-Type:application/json
            Authorization:key=FIREBASE_SERVER_KEY
         */

        restTemplate.interceptors = createInterceptor()

        val firebaseResponse = restTemplate.postForObject(FIRE_BASE_API_URL, entity, String::class.java)
        return CompletableFuture.completedFuture(firebaseResponse)
    }

    private fun createInterceptor(): MutableList<ClientHttpRequestInterceptor>? {
        val interceptors = ArrayList<ClientHttpRequestInterceptor>()
        interceptors.add(ClientRequestInterceptor("Authorization", "key=$FCM_SERVER_KEY"))
        interceptors.add(ClientRequestInterceptor("Content-Type", "application/json"))
        return interceptors
    }

    companion object {
        private const val FCM_SERVER_KEY: String =
                "AAAAzJ91pxI:APA91bEvgCD_DOX61BuSK0gAESGSgRWK38x3MvrAFgMiAiAxMgbOBjalM0_Hy_l9AVqb1RxCK4d0GgnrlQgj4EDjgU6f0TjX22XYl_Fcw8MfK_Xo4hpzHRVRCbN7jv_e6YC5GZ833III"

        private const val FIRE_BASE_API_URL: String = "https://fcm.googleapis.com/fcm/send"
    }
}
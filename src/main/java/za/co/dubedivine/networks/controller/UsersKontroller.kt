package za.co.dubedivine.networks.controller

import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import za.co.dubedivine.networks.model.Tag
import za.co.dubedivine.networks.model.User
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import za.co.dubedivine.networks.repository.TagRepository
import za.co.dubedivine.networks.repository.UserRepository
import za.co.dubedivine.networks.util.KUtils

@RestController
@RequestMapping("users")
class UsersKontroller(private val userRepository: UserRepository,
                      private val tagRepository: TagRepository) {

    @PostMapping
    fun signIn(@RequestBody user: User): ResponseEntity<StatusResponseEntity<User>> {
        if (userRepository.existsByEmail(user.email)) {
            return ResponseEntity(StatusResponseEntity(true,
                    "User Exits", userRepository.findByEmail(user.email)),
                    HttpStatus.OK)
        } else if (!userRepository.existsByEmail(user.email) &&  //todo these are bad should be over to their own functions
                user.degree != null &&
                user.degree.isNotBlank()) {

            tagRepository.save(Tag(changeDegree(user.degree.toUpperCase())))
            for (module in user.modules) {
                val tag = Tag(changeDegree(module.trim().capitalize()))
                tagRepository.save(tag)
                user.tags.add(tag)
            }
            user.tags.add(Tag(changeDegree(user.degree)))
            return ResponseEntity(StatusResponseEntity(true,
                    "Created a new account", userRepository.save(user)),
                    HttpStatus.OK)
        }
        return ResponseEntity(StatusResponseEntity(false,
                "Please create a new user", userRepository.findByEmail(user.email)),
                HttpStatus.OK)
    }

    //i need to get the users tags
    // the path does not conform to our standards
    @GetMapping("tags_subscribed/{u_id}")
    fun getTags(@PathVariable("u_id") userId: String): ResponseEntity<StatusResponseEntity<List<Tag>>> {
        val user = userRepository.findByIdOrNull(userId)
        return if (user != null)
            KUtils.respond(true, "", user.tags)
        else
            KUtils.respond(false, "Cannot get tags for this non existent USER", User().tags)
    }

    //this function is used to update the fcm token everytime the fcm token changes on the phone which can happen
    // at any time
    @PostMapping("update_fcm_token/{token}")
    fun updateFCMToken(@RequestBody user: User, @PathVariable("token") fcmToken: String):
                                                                ResponseEntity<StatusResponseEntity<Boolean>> {
        println("updating user token $fcmToken")
        val findOne = userRepository.findByIdOrNull(user.id)
        return if (findOne != null) {
            findOne.fcmToken = fcmToken
            userRepository.save(findOne)
            KUtils.respond(true, "Updated FCM token", true)
        } else {
            KUtils.respond(false, "Sorry Could not update FCM token", false)
        }
    }

    private fun changeDegree(degree: String): String {
        return degree.trim().replace(" ", "_")
    }
}
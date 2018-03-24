package za.co.dubedivine.networks.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import za.co.dubedivine.networks.model.Tag
import za.co.dubedivine.networks.model.User
import za.co.dubedivine.networks.model.responseEntity.StatusResponseEntity
import za.co.dubedivine.networks.repository.TagRepository
import za.co.dubedivine.networks.repository.UserRepository

@RestController
@RequestMapping("users")
class UsersKontroller(private val userRepository: UserRepository,
                      private val tagRepository: TagRepository) {

    @PostMapping()
    fun signIn(@RequestBody user: User): ResponseEntity<StatusResponseEntity<User>> {
        if (userRepository.existsByEmail(user.email)) {
            return ResponseEntity(StatusResponseEntity(true,
                    "User Exits", userRepository.findByEmail(user.email)),
                    HttpStatus.OK)
        } else if (!userRepository.existsByEmail(user.email) && user.degree != null ) {
            tagRepository.save(Tag(user.degree))
            user.tags.add(Tag(changeDegree(user.degree)))
            return ResponseEntity(StatusResponseEntity(false,
                    "Created a new account", userRepository.save(user)),
                    HttpStatus.OK)
        }
        return ResponseEntity(StatusResponseEntity(true,
                "Please create a new user", userRepository.findByEmail(user.email)),
                HttpStatus.OK)
    }

    //i need to get the users tags
    @GetMapping("/{u_id}/tags")
    fun getTags(@PathVariable userId: String): List<Tag> {
        val user = userRepository.findOne(userId)
        return user.tags
    }

    fun changeDegree(degree: String): String {
        return degree
    }
}
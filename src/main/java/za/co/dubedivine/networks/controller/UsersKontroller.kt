package za.co.dubedivine.networks.controller

import org.springframework.web.bind.annotation.*
import za.co.dubedivine.networks.model.Tag
import za.co.dubedivine.networks.model.User
import za.co.dubedivine.networks.repository.TagRepository
import za.co.dubedivine.networks.repository.UserRepository

@RequestMapping("users")
class UsersKontroller(private val userRepository: UserRepository,
                      private val tagRepository: TagRepository) {

    @PutMapping
    fun signUp(@RequestBody user: User) {

    }

    @PostMapping
    fun signIn(@RequestBody user: User) {

    }

    //i need to get the users tags

    @GetMapping("/{u_id}/tags")
    fun getTags(@PathVariable userId: String): List<Tag> {
        val user = userRepository.findOne(userId)
        return user.tags
    }




}
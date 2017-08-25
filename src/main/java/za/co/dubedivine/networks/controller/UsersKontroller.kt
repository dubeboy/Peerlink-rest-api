package za.co.dubedivine.networks.controller

import org.springframework.web.bind.annotation.*
import za.co.dubedivine.networks.model.User
import za.co.dubedivine.networks.model.repository.TagRepository
import za.co.dubedivine.networks.model.repository.UserRepository

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

    @GetMapping("/tags")
    fun getTags() {

    }


}
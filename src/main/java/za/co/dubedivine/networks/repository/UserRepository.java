package za.co.dubedivine.networks.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import za.co.dubedivine.networks.model.Tag;
import za.co.dubedivine.networks.model.User;

import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {

    User findByNickname(String nickname);

    User findByEmail(String email);

    List<User> findAllByTags(List<Tag> tags);

    boolean existsByEmail(String email);

    @Query("{ 'tags._id' : ?0 }")
    List<User> findAllByTag(@NotNull String name);
}

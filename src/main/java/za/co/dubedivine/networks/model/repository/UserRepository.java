package za.co.dubedivine.networks.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import za.co.dubedivine.networks.model.User;

public interface UserRepository
        extends MongoRepository<User, String>, QueryDslPredicateExecutor<User> {
}

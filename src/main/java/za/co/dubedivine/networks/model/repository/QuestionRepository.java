package za.co.dubedivine.networks.model.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.stereotype.Repository;
import za.co.dubedivine.networks.model.Question;
import za.co.dubedivine.networks.model.Tag;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String>, QueryDslPredicateExecutor<Question> {
}

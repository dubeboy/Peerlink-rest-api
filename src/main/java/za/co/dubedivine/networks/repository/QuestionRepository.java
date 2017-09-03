package za.co.dubedivine.networks.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import za.co.dubedivine.networks.model.Question;
import za.co.dubedivine.networks.model.Tag;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String>, QueryDslPredicateExecutor<Question> {
   // @Query("question.find({\"tags.name\": \"?0\")")
    List<Question> getQuestionsByTags(@Param("t") String tags);

}

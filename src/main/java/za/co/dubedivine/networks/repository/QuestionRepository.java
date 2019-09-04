package za.co.dubedivine.networks.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import za.co.dubedivine.networks.model.Question;

import java.util.List;

@Repository
public interface QuestionRepository extends MongoRepository<Question, String> {
//  @Query("question.find({\"tags.name\": \"?0\")")
    List<Question> findByTagsName(String tag);
}

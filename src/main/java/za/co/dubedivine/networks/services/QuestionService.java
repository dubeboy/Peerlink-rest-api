package za.co.dubedivine.networks.services;

import za.co.dubedivine.networks.model.Question;

import java.util.List;

public interface QuestionService {
    List<Question> findByTag(String tagName);
    List<Question> search(String q);

}

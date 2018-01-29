package za.co.dubedivine.networks.services;

import org.springframework.stereotype.Service;
import za.co.dubedivine.networks.model.Question;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {
    @Override
    public List<Question> findByTag(String tagName) {
        return null;
    }

    @Override
    public List<Question> search(String q) {
        return null;
    }
}
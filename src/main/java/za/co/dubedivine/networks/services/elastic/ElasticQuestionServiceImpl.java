package za.co.dubedivine.networks.services.elastic;

import org.springframework.data.domain.PageRequest;
import za.co.dubedivine.networks.model.Question;
import za.co.dubedivine.networks.model.elastic.ElasticQuestion;
import za.co.dubedivine.networks.repository.elastic.ElasticQRepo;

import java.util.List;

public class ElasticQuestionServiceImpl implements ElasticQuestionService {

    private ElasticQRepo elasticQRepo;

    public ElasticQuestionServiceImpl(ElasticQRepo elasticQRepo) {
        this.elasticQRepo = elasticQRepo;
    }

    @Override
    public List<ElasticQuestion> findByTag(String tagName) {
        return elasticQRepo.findByTagsName(tagName);
    }

    @Override
    public List<ElasticQuestion> search(String q) {
        return null;
    }

    @Override
    public ElasticQuestion save(ElasticQuestion question) {
        return elasticQRepo.save(question);
    }

    @Override
    public void delete(ElasticQuestion question) {
        elasticQRepo.delete(question);
    }
}

package za.co.dubedivine.networks.model.shared;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import za.co.dubedivine.networks.model.Question;
import za.co.dubedivine.networks.model.Tag;

import java.util.Date;
import java.util.List;
import java.util.Set;

public abstract class TagBase {
    @Indexed
    @Id
    private String name;  // name is the ID means
    private Date createAt = new Date();
    private List<Question> questions;
    public TagBase(String name) {
        this.name = name;
    }

    public TagBase() {

    }


    public String getName() {
        return name;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void addQuestion(Question q) {
        questions.add(q);
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}

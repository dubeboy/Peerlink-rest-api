package za.co.dubedivine.networks.model.shared;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import za.co.dubedivine.networks.model.Question;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class TagBase {
    @Indexed
    @Id
    private String name;  // name is the ID means
    private Date createAt = new Date();
    @JsonIgnore
    private List<Question> questions;

    public TagBase(String name) {
        this.name = name;
    }

    protected TagBase() {

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

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(Question q) {
        if (questions != null) {
            questions.add(q);
        } else {
            questions = new ArrayList<>();
            questions.add(q);
        }
    }
}

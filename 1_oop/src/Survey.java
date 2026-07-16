import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Survey {

    private String title;
    private String topic;
    private String description;
    private Set<Question> questions = new HashSet<>();

    public Survey(String title, String topic, String description) {
        this.title = title;
        this.topic = topic;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(Set<Question> questions) {
        this.questions = questions == null ? new HashSet<>() : questions;
    }

    public boolean addQuestion(Question question) {
        if (question == null) {
            System.out.println("Question cannot be null");
            return false;
        }

        if (questions.size() >= 40) {
            System.out.println("Survey cannot have more than 40 questions");
            return false;
        }

        for (Question existingQuestion : questions) {
            if (existingQuestion.getQuestion().equals(question.getQuestion())) {
                System.out.println("Survey has duplicate questions");
                return false;
            }
        }

        return questions.add(question);
    }

    public boolean removeQuestion(Question question) {
        if (question == null) {
            System.out.println("Question cannot be null");
            return false;
        }

        Question questionToRemove = null;

        for (Question existingQuestion : questions) {
            if (existingQuestion.getQuestion().equals(question.getQuestion())) {
                questionToRemove = existingQuestion;
                break;
            }
        }

        if (questionToRemove == null) {
            System.out.println("Question not found");
            return false;
        }

        return questions.remove(questionToRemove);
    }

    public boolean validateSurvey() {
        if (questions.isEmpty()) {
            System.out.println("Survey has no questions");
            return false;
        }

        if (questions.size() < 10) {
            System.out.println("Survey has less than 10 questions");
            return false;
        }

        if (questions.size() > 40) {
            System.out.println("Survey cannot have more than 40 questions");
            return false;
        }

        Set<String> questionTexts = new HashSet<>();

        for (Question question : questions) {
            String questionText = question.getQuestion().toLowerCase();

            if (!questionTexts.add(questionText)) {
                System.out.println("Survey has duplicate questions");
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Survey survey = (Survey) o;
        return Objects.equals(title, survey.title)
                && Objects.equals(topic, survey.topic)
                && Objects.equals(description, survey.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, topic, description);
    }
}
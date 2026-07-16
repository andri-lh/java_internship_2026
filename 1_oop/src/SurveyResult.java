import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class SurveyResult {

    private Survey survey;
    private Candidate candidate;
    private LocalDate dateTaken;
    private Map<Question, Answer> answers = new HashMap<>();


    public SurveyResult(Survey survey, Candidate candidate) {
        this.survey = survey;
        this.candidate = candidate;
        dateTaken = LocalDate.now();
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public LocalDate getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(LocalDate dateTaken) {
        this.dateTaken = dateTaken;
    }

    public Map<Question, Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Question, Answer> answers) {
        this.answers = answers;
    }

    public void answerQuestion(Question q, Answer answer) {
        if (q == null) {
            System.out.println("Question cannot be null");
            return;
        }

        if (survey == null || survey.getQuestions() == null || !survey.getQuestions().contains(q)) {
            System.out.println("Question does not belong to this survey");
            return;
        }

        answers.put(q, answer);
    }
}

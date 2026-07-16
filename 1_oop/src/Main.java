public class Main {
    public static void main(String[] args) {
        Survey survey = new Survey(
                "Employee Satisfaction",
                "HR",
                "Annual survey");
        for (int i = 1; i <= 10; i++) {
            survey.addQuestion(
                    new Question("Question " + i));
        }
        Candidate c1 = new Candidate(
                "John",
                "Smith",
                "john@test.com",
                "111111");
        Candidate c2 = new Candidate(
                "Mary",
                "Jones",
                "mary@test.com",
                "222222");
        SurveyResult r1 =
                new SurveyResult(survey, c1);
        SurveyResult r2 =
                new SurveyResult(survey, c2);
        for (Question q : survey.getQuestions()) {
            r1.answerQuestion(q, Answer.AGREE);
            r2.answerQuestion(q, Answer.SLIGHTLY_AGREE);
        }
        SurveyManager manager =
                new SurveyManager();
        manager.addSurveyResult(r1);
        manager.addSurveyResult(r2);
        System.out.println(
                "Survey valid: "
                        + survey.validateSurvey());
        System.out.println(
                "Most common answer: " + manager.findMostGivenAnswer(survey));
                System.out.println(
                        "Top candidate: "
                                + manager.findCandidateWithMostSurveys());
        manager.printSurveyResult(survey);
    }
}
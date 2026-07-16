import java.util.*;

public class SurveyManager {

    private final List<SurveyResult> results = new ArrayList<>();

    public void addSurveyResult(SurveyResult result) {
        results.add(result);
    }

    public Answer findMostGivenAnswer(Survey survey) {
        if (survey == null || survey.getQuestions() == null
                || survey.getQuestions().isEmpty()) {
            System.out.println("Survey is null or has no questions");
            return null;
        }

        Map<Answer, Integer> answerCounts = new HashMap<>();

        for (SurveyResult result : results) {
            if (result == null
                    || result.getSurvey() == null
                    || !result.getSurvey().equals(survey)
                    || result.getAnswers() == null) {
                continue;
            }

            for (Question question : survey.getQuestions()) {
                Answer answer = result.getAnswers().get(question);

                if (answer != null) {
                    answerCounts.put(
                            answer,
                            answerCounts.getOrDefault(answer, 0) + 1
                    );
                }
            }
        }

        Answer mostGivenAnswer = null;
        int maxCount = 0;

        for (Map.Entry<Answer, Integer> entry : answerCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostGivenAnswer = entry.getKey();
            }
        }

        return mostGivenAnswer;
    }

    public void printSurveyResult(Survey survey) {
        if (survey == null || survey.getQuestions() == null || survey.getQuestions().isEmpty()) {
            System.out.println("Survey is null or has no questions");
            return;
        }

        for (Question question : survey.getQuestions()) {
            int agreeCount = 0;
            int slightlyAgreeCount = 0;
            int slightlyDisagreeCount = 0;
            int disagreeCount = 0;

            for (SurveyResult result : results) {
                if (result == null
                        || result.getSurvey() == null
                        || !result.getSurvey().equals(survey)
                        || result.getAnswers() == null) {
                    continue;
                }

                Answer answer = result.getAnswers().get(question);

                if (answer == null) {
                    continue;
                }

                if (answer == Answer.AGREE) {
                    agreeCount++;
                } else if (answer == Answer.SLIGHTLY_AGREE) {
                    slightlyAgreeCount++;
                } else if (answer == Answer.SLIGHTLY_DISAGREE) {
                    slightlyDisagreeCount++;
                } else if (answer == Answer.DISAGREE) {
                    disagreeCount++;
                }
            }

            System.out.println("Question: " + question.getQuestion());
            System.out.println("Agree: " + agreeCount);
            System.out.println("Slightly Agree: " + slightlyAgreeCount);
            System.out.println("Slightly Disagree: " + slightlyDisagreeCount);
            System.out.println("Disagree: " + disagreeCount);
            System.out.println();
        }
    }

    public Map<Question, Answer> findAnswersByCandidate(Candidate candidate, Survey survey) {
        if (candidate == null || survey == null) {
            System.out.println("Candidate or survey is null");
            return null;
        }

        for (SurveyResult result : results) {
            if (result == null) {
                continue;
            }

            if (result.getCandidate() == null || result.getSurvey() == null) {
                continue;
            }

            if (result.getCandidate().equals(candidate) && result.getSurvey().equals(survey)) {
                return result.getAnswers();
            }
        }

        System.out.println("No answers found for this candidate in this survey");
        return null;

    }

    public Candidate findCandidateWithMostSurveys() {
        if (results.isEmpty()) {
            System.out.println("No survey results found");
            return null;
        }

        Map<Candidate, Integer> candidateSurveyCounts = getCandidateIntegerMap();

        Candidate candidateWithMostSurveys = null;
        int maxSurveys = 0;

        for (Map.Entry<Candidate, Integer> entry : candidateSurveyCounts.entrySet()) {
            if (entry.getValue() > maxSurveys) {
                maxSurveys = entry.getValue();
                candidateWithMostSurveys = entry.getKey();
            }
        }

        return candidateWithMostSurveys;
    }

    public void removeLowResponseQuestions(Survey survey) {
        if (survey == null || survey.getQuestions() == null || survey.getQuestions().isEmpty()) {
            System.out.println("Survey is null or has no questions");
            return;
        }

        int totalCandidates = 0;

        for (SurveyResult result : results) {
            if (result != null && result.getSurvey() != null && result.getSurvey().equals(survey)) {
                totalCandidates++;
            }
        }

        if (totalCandidates == 0) {
            System.out.println("No survey results found for this survey");
            return;
        }

        Set<Question> questionsToRemove = new HashSet<>();

        for (Question question : survey.getQuestions()) {
            int answeredCount = 0;

            for (SurveyResult result : results) {
                if (result == null
                        || result.getSurvey() == null
                        || !result.getSurvey().equals(survey)
                        || result.getAnswers() == null) {
                    continue;
                }

                Answer answer = result.getAnswers().get(question);

                if (answer != null) {
                    answeredCount++;
                }
            }

            if (answeredCount < totalCandidates * 0.5) {
                questionsToRemove.add(question);
            }
        }

        for (Question question : questionsToRemove) {
            survey.removeQuestion(question);
        }
    }



    //extracting the Candidate Integer Map for further use
    private Map<Candidate, Integer> getCandidateIntegerMap() {
        Map<Candidate, Integer> candidateSurveyCounts = new HashMap<>();

        for (SurveyResult result : results) {
            if (result == null || result.getCandidate() == null) {
                continue;
            }

            Candidate candidate = result.getCandidate();

            if (candidateSurveyCounts.containsKey(candidate)) {
                candidateSurveyCounts.put(candidate, candidateSurveyCounts.get(candidate) + 1);
            } else {
                candidateSurveyCounts.put(candidate, 1);
            }
        }
        return candidateSurveyCounts;
    }
}
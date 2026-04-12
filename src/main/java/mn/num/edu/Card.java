package mn.num.edu;

public class Card {
    private final String question;
    private final String answer;
    private int correctStreak = 0;
    private int totalAttempts = 0;
    private boolean lastAttemptWrong = false;

    public Card(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public int getCorrectStreak() { return correctStreak; }
    public int getTotalAttempts() { return totalAttempts; }
    public boolean isLastAttemptWrong() { return lastAttemptWrong; }

    public void recordAttempt(boolean isCorrect) {
        totalAttempts++;
        if (isCorrect) {
            correctStreak++;
            lastAttemptWrong = false;
        } else {
            correctStreak = 0;
            lastAttemptWrong = true;
        }
    }
}
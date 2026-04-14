package mn.num.edu;

public class Card {
    private final String question;
    private final String answer;
    private int correctStreak  = 0;  // Дараалсан зөв хариултын тоо
    private int totalAttempts  = 0;  // Нийт оролдлогын тоо
    private int totalCorrect   = 0;  // Нийт зөв хариултын тоо
    private int totalWrong     = 0;  // Нийт буруу хариултын тоо
    private boolean lastAttemptWrong = false;

    public Card(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() { return question; }
    public String getAnswer()   { return answer; }
    public int getCorrectStreak()  { return correctStreak; }
    public int     getTotalAttempts()   { return totalAttempts; }
    public int     getTotalCorrect()    { return totalCorrect; }
    public int     getTotalWrong()      { return totalWrong; }
    public boolean isLastAttemptWrong() { return lastAttemptWrong; }

    public void recordAttempt(boolean isCorrect) {
        totalAttempts++;
        if (isCorrect) {
            correctStreak++;
            totalCorrect++;
            lastAttemptWrong = false;
        } else {
            correctStreak = 0;
            totalWrong++;
            lastAttemptWrong = true;
        }
    }
}
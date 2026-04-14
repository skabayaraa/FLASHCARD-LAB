package mn.num.edu;

public class Card {
    private final String question;
    private final String answer;
    private int correctStreak  = 0;  // Дараалсан зөв хариултын тоо
    private int totalAttempts  = 0;  // Нийт оролдлогын тоо
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
    
    public int     getTotalWrong()      { return totalWrong; }
    public boolean isLastAttemptWrong() { return lastAttemptWrong; }


    // Энэ функцийг заавал нэмээрэй (Main.java-д хэрэгтэй байгаа)
    public int getTotalCorrect() {
        return totalAttempts - totalWrong;
    }
    
    public void recordAttempt(boolean isCorrect) {
        totalAttempts++;
        if (isCorrect) {
            correctStreak++;
            lastAttemptWrong = false;
        } else {
            correctStreak = 0;
            totalWrong++;
            lastAttemptWrong = true;
        }
    }
}
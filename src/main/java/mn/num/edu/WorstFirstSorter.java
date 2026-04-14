package mn.num.edu;

import java.util.*;

public class WorstFirstSorter implements CardOrganizer {
    @Override
    public void organize(List<Card> cards) {
        // Алдаа хамгийн их гаргасан картыг эхэнд тавина
        // Алдааны тоо = нийт оролдлого - зөв дараалал
        // Нийт алдааны тоогоор буурах дарааллаар эрэмбэлнэ
        cards.sort((a, b) -> Integer.compare(b.getTotalWrong(), a.getTotalWrong()));
    }
}
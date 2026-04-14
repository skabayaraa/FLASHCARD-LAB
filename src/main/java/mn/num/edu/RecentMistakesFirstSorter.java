package mn.num.edu;

import java.util.*;

public class RecentMistakesFirstSorter implements CardOrganizer {
    @Override
    public void organize(List<Card> cards) {
        List<Card> wrongOnes = new ArrayList<>();
        List<Card> others = new ArrayList<>();

        for (Card card : cards) {
            if (card.isLastAttemptWrong()) wrongOnes.add(card);
            else others.add(card);
        }

        cards.clear();
        cards.addAll(wrongOnes);
        cards.addAll(others);
    }
}
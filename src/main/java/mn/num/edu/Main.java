package mn.num.edu;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        // Encoding asuudlaas sergiilj zuvhun Latin usguud ashiglav.

        // 1. Command line options
        Options options = new Options();
        options.addOption("h", "help",         false, "Tuslamjiin medeelel haruulah");
        options.addOption("o", "order",        true,  "Erembeleh: random, recent-mistakes-first, worst-first");
        options.addOption("r", "repetitions",  true,  "Neg kartiig heden udaa zuv hariulah (target repetitions)");
        options.addOption("i", "invertCards", false, "Asuult, hariultiig solij haruulah");

        try {
            CommandLine cmd = new DefaultParser().parse(options, args);

            if (cmd.hasOption("help")) {
                new HelpFormatter().printHelp("flashcard <file> [options]", options);
                return;
            }

            String[] remainingArgs = cmd.getArgs();
            if (remainingArgs.length == 0) {
                System.out.println("ALDAA: Cards-file-iin zamiig zaaj ugnu uu.");
                return;
            }

            runSession(cmd, remainingArgs[0]);

        } catch (ParseException e) {
            System.out.println("Tushaal buruu baina: " + e.getMessage());
        }
    }

    private static void runSession(CommandLine cmd, String filePath) {
        List<Card> deck = loadCards(filePath);
        if (deck.isEmpty()) return;

        int    targetReps = Integer.parseInt(cmd.getOptionValue("repetitions", "1"));
        String order      = cmd.getOptionValue("order", "random");
        boolean invert    = cmd.hasOption("invertCards");

        Set<String> earnedAchievements = new HashSet<>();

        // Scanner-iig standart encoding-oor ashiglav
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                // Kart zohion baiguulalt (Sorting)
                switch (order.toLowerCase()) {
                    case "recent-mistakes-first" -> new RecentMistakesFirstSorter().organize(deck);
                    case "worst-first"           -> new WorstFirstSorter().organize(deck);
                    default                      -> Collections.shuffle(deck);
                }

                boolean isAllFinished      = true;
                boolean currentRoundPerfect = true;

                for (Card card : deck) {
                    if (card.getCorrectStreak() >= targetReps) continue;

                    isAllFinished = false;

                    String question     = invert ? card.getAnswer()   : card.getQuestion();
                    String correctAnswer = invert ? card.getQuestion() : card.getAnswer();

                    System.out.print(question + " ? ");
                    String userInput = scanner.nextLine().trim();

                    boolean isCorrect = userInput.equalsIgnoreCase(correctAnswer);
                    card.recordAttempt(isCorrect);

                    if (isCorrect) {
                        System.out.println("Zuv!");
                    } else {
                        System.out.println("Buruu! Hariult: " + correctAnswer);
                        currentRoundPerfect = false;
                    }

                    checkIndividualAchievements(card, earnedAchievements);
                }

                // CORRECT achievement
                if (currentRoundPerfect && !deck.isEmpty() && earnedAchievements.add("CORRECT")) {
                    System.out.println(">>> AMJILT: CORRECT (Suuliin toirogt buh kart zuv!)");
                }

                if (isAllFinished) break;
            }
        }
        System.out.println("\nBayar hurgeye! Surgalt amjilttai duuslaa.");
    }

    private static void checkIndividualAchievements(Card card, Set<String> earned) {
        // REPEAT achievement
        if (card.getTotalAttempts() > 5 && earned.add("REPEAT")) {
            System.out.println(">>> AMJILT: REPEAT (Neg kartiig 5-aas olon udaa oroldloo)");
        }
        // CONFIDENT achievement
        if (card.getTotalCorrect() >= 3 && earned.add("CONFIDENT")) {
            System.out.println(">>> AMJILT: CONFIDENT (Neg kartiig niit 3 udaa zuv hariullaa)");
        }
    }

    private static List<Card> loadCards(String path) {
        List<Card> list = new ArrayList<>();
        File file = new File(path);

        if (!file.exists()) {
            System.out.println("ALDAA: " + path + " fail oldsongui.");
            return list;
        }

        try (Scanner fs = new Scanner(file)) {
            while (fs.hasNextLine()) {
                String line = fs.nextLine().trim();
                if (line.contains("|")) {
                    String[] parts = line.split("\\|", 2);
                    if (parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank()) {
                        list.add(new Card(parts[0].trim(), parts[1].trim()));
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Fail unshihad aldaa garlaa: " + e.getMessage());
        }
        return list;
    }
}
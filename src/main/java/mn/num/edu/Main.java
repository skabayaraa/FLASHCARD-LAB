package mn.num.edu;

import org.apache.commons.cli.*;
import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        // 1. Консол руу Монгол үсэг зөв хэвлэх тохиргоо
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {}

        // 2. Командын мөрийн тохиргоо
        Options options = new Options();
        options.addOption("h", "help",        false, "Тусламжийн мэдээлэл харуулах");
        options.addOption("o", "order",        true, "Зохион байгуулалт: random, recent-mistakes-first, worst-first");
        options.addOption("r", "repetitions",  true, "Нэг картыг хэдэн удаа зөв хариулахыг тохируулах");
        options.addOption("i", "invertCards", false, "Асуулт, хариултыг сольж харуулна");

        try {
            CommandLine cmd = new DefaultParser().parse(options, args);

            if (cmd.hasOption("help")) {
                new HelpFormatter().printHelp("flashcard <file> [options]", options);
                return;
            }

            String[] remainingArgs = cmd.getArgs();
            if (remainingArgs.length == 0) {
                System.out.println("Алдаа: Cards-file-ын замыг зааж өгнө үү.");
                return;
            }

            runSession(cmd, remainingArgs[0]);

        } catch (ParseException e) {
            System.out.println("Тушаал буруу байна: " + e.getMessage());
        }
    }

    private static void runSession(CommandLine cmd, String filePath) {
        List<Card> deck = loadCards(filePath);
        if (deck.isEmpty()) return;

        int    targetReps = Integer.parseInt(cmd.getOptionValue("repetitions", "1"));
        String order      = cmd.getOptionValue("order", "random");
        boolean invert    = cmd.hasOption("invertCards");

        Set<String> earnedAchievements = new HashSet<>();

        try (Scanner scanner = new Scanner(System.in, "UTF-8")) {
            while (true) {
                // Карт зохион байгуулалт
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
                        System.out.println("Зөв!");
                    } else {
                        System.out.println("Буруу! Хариулт: " + correctAnswer);
                        currentRoundPerfect = false;
                    }

                    checkIndividualAchievements(card, earnedAchievements);
                }

                // CORRECT амжилт: тойрогт бүх карт зөв
                if (currentRoundPerfect && !deck.isEmpty() && earnedAchievements.add("CORRECT")) {
                    System.out.println(">>> АМЖИЛТ: CORRECT (Сүүлийн тойрогт бүх карт зөв!)");
                }

                if (isAllFinished) break;
            }
        }
        System.out.println("\nБаяр хүргэе! Сургалт амжилттай дууслаа.");
    }

    private static void checkIndividualAchievements(Card card, Set<String> earned) {
        // REPEAT: Нэг картад нийт 5-аас олон удаа хариулсан
        if (card.getTotalAttempts() > 5 && earned.add("REPEAT")) {
            System.out.println(">>> АМЖИЛТ: REPEAT (Нэг картыг 5-аас олон удаа оролдлоо)");
        }
        // CONFIDENT: Нэг картад нийт дор хаяж 3 удаа зөв хариулсан
        if (card.getTotalCorrect() >= 3 && earned.add("CONFIDENT")) {
            System.out.println(">>> АМЖИЛТ: CONFIDENT (Нэг картыг нийт 3 удаа зөв хариуллаа)");
        }
    }

    private static List<Card> loadCards(String path) {
        List<Card> list = new ArrayList<>();
        File file = new File(path);

        if (!file.exists()) {
            System.out.println("Алдаа: " + path + " файл олдсонгүй.");
            return list;
        }

        try (Scanner fs = new Scanner(file, "UTF-8")) {
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
            System.out.println("Файл уншихад алдаа гарлаа: " + e.getMessage());
        }
        return list;
    }
}
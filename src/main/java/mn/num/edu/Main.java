package mn.num.edu;

import org.apache.commons.cli.*;
import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        // 1. Консол руу Монгол үсэг зөв хэвлэх тохиргоо (Encoding fix)
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {}

        // 2. Командын мөрийн тохиргоо (CLI Options - Task 1)
        Options options = new Options();
        options.addOption("h", "help", false, "Тусламжийн мэдээлэл харуулах");
        options.addOption("o", "order", true, "Зохион байгуулалт: random, recent-mistakes-first, worst-first");
        options.addOption("r", "repetitions", true, "Нэг картыг хэдэн удаа зөв хариулахыг тохируулах");
        options.addOption("i", "invertCards", false, "Асуулт, хариултыг сольж харуулна");

        try {
            CommandLine cmd = new DefaultParser().parse(options, args);

            // --help тушаал (Бусад тушаал байсан ч зөвхөн тусламж харуулаад хаагдана)
            if (cmd.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("flashcard <file> [options]", options);
                return;
            }

            // Файл зааж өгсөн эсэхийг шалгах
            String[] remainingArgs = cmd.getArgs();
            if (remainingArgs.length == 0) {
                System.out.println("Алдаа: Cards-file-ын замыг зааж өгнө үү.");
                return;
            }

            // Программыг эхлүүлэх
            runSession(cmd, remainingArgs[0]);

        } catch (ParseException e) {
            System.out.println("Тушаал буруу байна: " + e.getMessage());
        }
    }

    private static void runSession(CommandLine cmd, String filePath) {
        List<Card> deck = loadCards(filePath);
        if (deck.isEmpty()) return;

        // Аргументуудыг унших
        int targetReps = Integer.parseInt(cmd.getOptionValue("repetitions", "1"));
        String order = cmd.getOptionValue("order", "random");
        boolean invert = cmd.hasOption("invertCards");

        Set<String> earnedAchievements = new HashSet<>();
        
        // Resource leak-ээс сэргийлж try-with-resources ашиглав
        try (Scanner scanner = new Scanner(System.in, "UTF-8")) {
            while (true) {
                // 3. Карт зохион байгуулалт (Task 2)
                if (order.equalsIgnoreCase("recent-mistakes-first")) {
                    new RecentMistakesFirstSorter().organize(deck);
                } else {
                    Collections.shuffle(deck);
                }

                boolean isAllFinished = true;
                boolean currentRoundPerfect = true;

                for (Card card : deck) {
                    if (card.getCorrectStreak() < targetReps) {
                        isAllFinished = false;
                        
                        // Асуулт ба Хариултыг тодорхойлох
                        String question = invert ? card.getAnswer() : card.getQuestion();
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

                        // 4. Амжилтуудыг шалгах (Task 3)
                        checkIndividualAchievements(card, earnedAchievements);
                    }
                }

                // CORRECT амжилт: Тухайн тойрогт бүх карт зөв хариулагдсан бол
                if (currentRoundPerfect && !deck.isEmpty() && earnedAchievements.add("CORRECT")) {
                    System.out.println(">>> АМЖИЛТ: CORRECT (Сүүлийн тойрогт бүх карт зөв!)");
                }

                if (isAllFinished) break;
            }
        }
        System.out.println("\nБаяр хүргэе! Сургалт амжилттай дууслаа.");
    }

    private static void checkIndividualAchievements(Card card, Set<String> earned) {
        // REPEAT: Нэг картад 5-аас олон удаа хариулсан
        if (card.getTotalAttempts() > 5 && earned.add("REPEAT")) {
            System.out.println(">>> АМЖИЛТ: REPEAT (Нэг картыг 5-аас олон удаа оролдлоо)");
        }
        // CONFIDENT: Нэг картад дор хаяж 3 удаа зөв хариулсан
        if (card.getCorrectStreak() >= 3 && earned.add("CONFIDENT")) {
            System.out.println(">>> АМЖИЛТ: CONFIDENT (Энэ картыг 3 дараалж зөв хариуллаа)");
        }
    }

    private static List<Card> loadCards(String path) {
        List<Card> list = new ArrayList<>();
        File file = new File(path);
        
        if (!file.exists()) {
            System.out.println("Алдаа: " + path + " файл олдсонгүй.");
            return list;
        }

        try (Scanner fileScanner = new Scanner(file, "UTF-8")) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                if (line.contains("|")) {
                    String[] parts = line.split("\\|");
                    if (parts.length == 2) {
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
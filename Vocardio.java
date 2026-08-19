import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Vocardio {

    private static final String KEY =
            "KUKJDHKJHDGKJHEWFIYFTGIWUIPOIXKZLKFLNHFRGUFIUEOHDSLKJBNSXKBJNX";

    private static final Random RANDOM = new Random();
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) throws IOException {

        int score = 0;
        int hearts = 8;
        int wordsDone = 0;
        Set<Character> guesses = new HashSet<>();
        String message = "Welcome to Vocardio. Good luck!";

        List<String> content = readAllLines("wlist.txt");

        int wordNum = RANDOM.nextInt(1295);
        String currWord = content.get(wordNum * 4).toUpperCase();
        String info1 = content.get(wordNum * 4 + 1).toUpperCase();
        String info2 = content.get(wordNum * 4 + 2).toUpperCase();
        String sentence = content.get(wordNum * 4 + 3).toUpperCase();
        sentence = sentence.replace(currWord, repeat("*", currWord.length()));
        String currentGuess = repeat("-", currWord.length());

        while (true) {
            clearScreen();
            System.out.println("============== V O C A R D I O ==============\n\n" + message + "\n");
            System.out.println("FIND THE WORD:     " + currentGuess + " (" + currWord.length() + ")");
            System.out.println();
            System.out.println(info1 + "\n" + info2 + "\n" + sentence + "\n");
            for (int i = 0; i < hearts; i++) {
                System.out.print("\u2764\ufe0f  ");
            }
            System.out.println("\n");
            System.out.println("Words Completed: " + wordsDone + "     Score: " + score);
            System.out.println();

            System.out.print("Type a single charcter and press enter, or type QUIT to quit: ");
            String guessInput = SCANNER.nextLine();
            guessInput = guessInput.toUpperCase();

            if (guessInput.isEmpty()) {
                continue;
            }
            if (guessInput.equals("QUIT")) {
                message = "You quit the game";
                break;
            }

            char guess = guessInput.charAt(0);

            if (guesses.contains(guess)) {
                message = "You already guessed " + guess;
                continue;
            } else {
                guesses.add(guess);
            }

            if (currWord.indexOf(guess) >= 0) {
                StringBuilder newGuess = new StringBuilder();
                for (int i = 0; i < currWord.length(); i++) {
                    if (currWord.charAt(i) == guess) {
                        newGuess.append(guess);
                    } else {
                        newGuess.append(currentGuess.charAt(i));
                    }
                }
                currentGuess = newGuess.toString();

                if (currentGuess.equals(currWord)) {
                    message = "Yes! The word was " + currWord;
                    if (hearts == 8) {
                        score += 100;
                    } else {
                        score += hearts * 10;
                    }
                    hearts = 8;
                    wordsDone++;
                    guesses.clear();

                    content = readAllLines("wlist.txt");
                    wordNum = RANDOM.nextInt(1295);
                    currWord = content.get(wordNum * 4).toUpperCase();
                    info1 = content.get(wordNum * 4 + 1).toUpperCase();
                    info2 = content.get(wordNum * 4 + 2).toUpperCase();
                    sentence = content.get(wordNum * 4 + 3).toUpperCase();
                    sentence = sentence.replace(currWord, repeat("*", currWord.length()));
                    currentGuess = repeat("-", currWord.length());
                } else {
                    message = "Yes, it contains " + guess;
                }
            } else {
                message = "No, it does not contain " + guess;
                hearts--;
                if (hearts == 0) {
                    message = "You ran out of hearts! GAME OVER";
                    break;
                }
            }
        }

        clearScreen();
        System.out.println("============== V O C A R D I O ==============\n\n" + message + "\n");
        System.out.println("Words Completed: " + wordsDone + "     Score: " + score + "\n");
        System.out.println("Thank you for playing Vocardio!");

        List<String> hsLines = readAllLines("localhs.txt");
        List<Object[]> highScores = new ArrayList<>();
        for (String line : hsLines) {
            String deLine = decrypt(line, KEY);
            String[] scoreLine = deLine.split(" ");
            highScores.add(new Object[]{scoreLine[0], Integer.parseInt(scoreLine[1])});
        }

        String saveHs = "N";
        if (highScores.isEmpty() || highScores.size() < 10) {
            if (score > 0) {
                System.out.print("You have a high score! Would you like to save it? (Y/N):");
                saveHs = SCANNER.nextLine();
            }
        } else {
            int lastScore = (int) highScores.get(highScores.size() - 1)[1];
            if (lastScore < score) {
                System.out.print("\nYou have a high score! Would you like to save it? (Y/N):");
                saveHs = SCANNER.nextLine();
            }
        }
        saveHs = saveHs.toUpperCase();

        if (saveHs.equals("Y")) {
            System.out.print("Enter your name or nickname (max 10 char):");
            String name = SCANNER.nextLine();
            if (name.length() > 10) {
                name = name.substring(0, 10);
            }
            highScores.add(new Object[]{name, score});
            String codeForUp = encrypt(name + " " + score, KEY);

            System.out.println("\n-----------NEW HIGH SCORES------------");
            sort(highScores);

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("localhs.txt"), StandardCharsets.UTF_8))) {
                for (int i = 0; i < highScores.size(); i++) {
                    String nm = (String) highScores.get(i)[0];
                    int sc = (int) highScores.get(i)[1];
                    writer.write(encrypt(nm + " " + sc, KEY));
                    writer.newLine();
                    System.out.println(nm + repeat(" ", 15 - nm.length()) + sc);
                    if (i == 9) break;
                }
            }
            System.out.println("Use this code to submit your high score online: " + codeForUp);
        } else {
            System.out.println("\n-----------HIGH SCORES------------");
            for (Object[] entry : highScores) {
                String nm = (String) entry[0];
                int sc = (int) entry[1];
                System.out.println(nm + repeat(" ", 15 - nm.length()) + sc);
            }
        }
    }

    private static String encrypt(String scoreLine, String key) {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder prepend = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            prepend.append(letters.charAt(RANDOM.nextInt(letters.length())));
        }
        String toEncrypt = prepend + scoreLine;
        StringBuilder encrypted = new StringBuilder();
        for (int i = 0; i < toEncrypt.length(); i++) {
            encrypted.append((char) ((toEncrypt.charAt(i) + key.charAt(i)) % 126));
        }
        return encrypted.toString();
    }

    private static String decrypt(String encrypted, String key) {
        StringBuilder scoreLine = new StringBuilder();
        for (int i = 0; i < encrypted.length(); i++) {
            int value = ((encrypted.charAt(i) - key.charAt(i)) % 126 + 126) % 126;
            scoreLine.append((char) value);
        }
        return scoreLine.substring(5);
    }

    private static void sort(List<Object[]> scores) {
        scores.sort(Comparator.comparingInt(a -> (int) a[1]));
    }

    private static List<String> readAllLines(String path) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private static String repeat(String s, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) sb.append(s);
        return sb.toString();
    }

    private static void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
        }
    }
}
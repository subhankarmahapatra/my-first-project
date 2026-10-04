import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class EmotionInsightEngine {

    private static final Scanner INPUT = new Scanner(System.in);
    private static final String LOG_FILE = "emotion_log.csv";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static final Set<String> NEGATIONS = Set.of(
            "not", "no", "never", "without", "isn't", "wasn't",
            "aren't", "don't", "didn't", "can't", "cannot",
            "couldn't", "won't", "wouldn't", "hardly"
    );

    private static final Map<String, Double> POSITIVE = Map.ofEntries(
            Map.entry("happy", 6.0), Map.entry("joy", 7.0),
            Map.entry("amazing", 7.0), Map.entry("love", 6.0),
            Map.entry("grateful", 6.0), Map.entry("excited", 7.0),
            Map.entry("good", 3.0), Map.entry("great", 5.0),
            Map.entry("wonderful", 7.0), Map.entry("hopeful", 5.0),
            Map.entry("peaceful", 5.0), Map.entry("confident", 5.0),
            Map.entry("proud", 5.0), Map.entry("relaxed", 4.0),
            Map.entry("delighted", 7.0), Map.entry("enjoy", 4.0)
    );

    private static final Map<String, Double> NEGATIVE = Map.ofEntries(
            Map.entry("sad", -6.0), Map.entry("hurt", -5.0),
            Map.entry("pain", -5.0), Map.entry("angry", -7.0),
            Map.entry("lonely", -6.0), Map.entry("depressed", -8.0),
            Map.entry("bad", -3.0), Map.entry("terrible", -8.0),
            Map.entry("upset", -5.0), Map.entry("miserable", -8.0),
            Map.entry("stressed", -6.0), Map.entry("tired", -3.0),
            Map.entry("hopeless", -8.0), Map.entry("afraid", -6.0),
            Map.entry("worried", -5.0), Map.entry("anxious", -6.0),
            Map.entry("frustrated", -6.0), Map.entry("hate", -7.0)
    );

    private static final Map<String, Map<String, Double>> EMOTIONS =
            createEmotionDictionary();

    public static void main(String[] args) {
        printBanner();

        while (true) {
            System.out.println("Commands: exit | history | stats");
            System.out.print("\nTell me how you feel: ");

            String input = INPUT.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Session ended. Take care!");
                break;
            }

            if (input.equalsIgnoreCase("history")) {
                showHistory();
                continue;
            }

            if (input.equalsIgnoreCase("stats")) {
                showStatistics();
                continue;
            }

            if (input.isEmpty()) {
                System.out.println("Please enter some text.");
                continue;
            }

            analyze(input);
        }

        INPUT.close();
    }

    private static void printBanner() {
        System.out.println("==============================================");
        System.out.println("       EMOTION INSIGHT ENGINE PRO");
        System.out.println("==============================================");
        System.out.println("Rule-based emotion and sentiment analyzer");
        System.out.println("Includes emotion bars, emoji detection,");
        System.out.println("history, statistics and personalized tips.");
    }

    private static Map<String, Map<String, Double>>
    createEmotionDictionary() {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();

        result.put("Happiness", weights(
                "happy:3", "joy:3", "excited:3", "amazing:3",
                "grateful:2", "wonderful:3", "great:2",
                "good:1", "delighted:3", "cheerful:2",
                "hopeful:2", "enjoy:2"
        ));

        result.put("Sadness", weights(
                "sad:3", "cry:2", "lonely:3", "depressed:4",
                "hurt:2", "pain:2", "upset:2", "miserable:4",
                "hopeless:4", "heartbroken:4", "grief:4"
        ));

        result.put("Anger", weights(
                "angry:3", "mad:2", "hate:3", "frustrated:3",
                "furious:4", "annoyed:2", "rage:4", "irritated:2"
        ));

        result.put("Fear", weights(
                "scared:3", "fear:2", "worried:2", "anxious:3",
                "nervous:2", "terrified:4", "afraid:3",
                "panic:4", "insecure:2"
        ));

        result.put("Love", weights(
                "love:4", "caring:2", "special:2", "affection:3",
                "adore:4", "romantic:3", "cherish:3", "beloved:3"
        ));

        result.put("Surprise", weights(
                "surprised:3", "shocked:3", "unexpected:2",
                "astonished:4", "wow:2", "unbelievable:3"
        ));

        result.put("Disgust", weights(
                "disgusted:4", "gross:3", "awful:2",
                "revolting:4", "nasty:3", "repulsive:4"
        ));

        return Collections.unmodifiableMap(result);
    }

    private static Map<String, Double> weights(String... entries) {
        Map<String, Double> result = new HashMap<>();

        for (String entry : entries) {
            String[] parts = entry.split(":");
            result.put(parts[0], Double.parseDouble(parts[1]));
        }

        return result;
    }

    private static void analyze(String originalText) {
        String text = originalText.toLowerCase(Locale.ROOT);
        String[] words = tokenize(text);

        Map<String, Double> scores = new LinkedHashMap<>();

        for (Map.Entry<String, Map<String, Double>> emotion
                : EMOTIONS.entrySet()) {

            double score = 0.0;

            for (int i = 0; i < words.length; i++) {
                Double weight = emotion.getValue().get(words[i]);

                if (weight != null && !isNegated(words, i)) {
                    score += weight * intensityModifier(words, i);
                }
            }

            scores.put(emotion.getKey(), score);
        }

        // Emoji signals add extra emotion points.
        addEmojiScores(text, scores);

        String dominant = findDominant(scores);
        double sentiment = sentimentScore(words);
        double intensity = intensityScore(scores);
        String mood = classifyMood(sentiment);
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);

        System.out.println("\n============== ANALYSIS RESULT ==============");
        System.out.println("Dominant emotion : " + dominant);
        System.out.printf(Locale.ROOT,
                "Sentiment score  : %.1f / 100%n", sentiment);
        System.out.printf(Locale.ROOT,
                "Intensity        : %.1f%%%n", intensity);
        System.out.println("Mood category    : " + mood);
        System.out.println("Matched keywords : " + matchedKeywords(words));
        System.out.println("Reflection       : " + reflection(dominant));
        System.out.println("Suggestion       : " + suggestion(dominant));
        System.out.println("Timestamp        : " + timestamp);

        showEmotionBars(scores);

        try {
            saveLog(timestamp, originalText, dominant,
                    sentiment, intensity, mood);
            System.out.println("\nAnalysis saved to " + LOG_FILE);
        } catch (IOException e) {
            System.out.println("Could not save log: " + e.getMessage());
        }

        System.out.println(
                "\nNote: This is a keyword-based estimate, not a clinical assessment."
        );
        System.out.println("==============================================\n");
    }

    private static String[] tokenize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .split("[^a-z']+");
    }

    private static boolean isNegated(String[] words, int index) {
        // Check the preceding three tokens for a negation.
        for (int i = Math.max(0, index - 3); i < index; i++) {
            if (NEGATIONS.contains(words[i])) {
                return true;
            }
        }
        return false;
    }

    private static double intensityModifier(String[] words, int index) {
        double modifier = 1.0;

        for (int i = Math.max(0, index - 2); i < index; i++) {
            switch (words[i]) {
                case "very":
                case "extremely":
                case "really":
                case "incredibly":
                case "so":
                    modifier *= 1.5;
                    break;

                case "slightly":
                case "somewhat":
                case "little":
                case "mildly":
                    modifier *= 0.5;
                    break;

                case "absolutely":
                case "totally":
                    modifier *= 1.3;
                    break;

                default:
                    break;
            }
        }

        return Math.min(modifier, 2.5);
    }

    private static void addEmojiScores(
            String text, Map<String, Double> scores) {

        addEmoji(text, scores, "😂", "Happiness", 3);
        addEmoji(text, scores, "😊", "Happiness", 3);
        addEmoji(text, scores, "😄", "Happiness", 3);
        addEmoji(text, scores, "🥰", "Love", 3);
        addEmoji(text, scores, "❤️", "Love", 4);
        addEmoji(text, scores, "❤", "Love", 4);
        addEmoji(text, scores, "😢", "Sadness", 3);
        addEmoji(text, scores, "😭", "Sadness", 4);
        addEmoji(text, scores, "😡", "Anger", 4);
        addEmoji(text, scores, "🤬", "Anger", 4);
        addEmoji(text, scores, "😨", "Fear", 3);
        addEmoji(text, scores, "😰", "Fear", 3);
        addEmoji(text, scores, "😲", "Surprise", 3);
        addEmoji(text, scores, "😮", "Surprise", 2);
        addEmoji(text, scores, "🤢", "Disgust", 3);
        addEmoji(text, scores, "🤮", "Disgust", 4);
    }

    private static void addEmoji(
            String text, Map<String, Double> scores,
            String emoji, String emotion, double points) {

        int count = 0;
        int position = 0;

        while ((position = text.indexOf(emoji, position)) != -1) {
            count++;
            position += emoji.length();
        }

        scores.put(emotion,
                scores.get(emotion) + count * points);
    }

    private static double sentimentScore(String[] words) {
        double score = 50.0;

        for (int i = 0; i < words.length; i++) {
            Double value = POSITIVE.get(words[i]);

            if (value == null) {
                value = NEGATIVE.get(words[i]);
            }

            if (value != null) {
                double modifier = intensityModifier(words, i);

                if (isNegated(words, i)) {
                    score -= value * modifier;
                } else {
                    score += value * modifier;
                }
            }
        }

        return Math.max(0, Math.min(100, score));
    }

    private static String findDominant(
            Map<String, Double> scores) {

        return scores.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Neutral");
    }

    private static double intensityScore(
            Map<String, Double> scores) {

        double total = scores.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        return Math.min(total * 5.0, 100.0);
    }

    private static String classifyMood(double score) {
        if (score >= 80) return "Extremely Positive";
        if (score >= 65) return "Positive";
        if (score >= 45) return "Neutral";
        if (score >= 25) return "Negative";
        return "Very Negative";
    }

    private static String matchedKeywords(String[] words) {
        Set<String> found = new LinkedHashSet<>();

        for (String word : words) {
            if (POSITIVE.containsKey(word)
                    || NEGATIVE.containsKey(word)) {
                found.add(word);
            }

            for (Map<String, Double> dictionary
                    : EMOTIONS.values()) {
                if (dictionary.containsKey(word)) {
                    found.add(word);
                }
            }
        }

        return found.isEmpty() ? "None detected" : found.toString();
    }

    private static void showEmotionBars(
            Map<String, Double> scores) {

        double total = scores.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        System.out.println("\n----------- EMOTION BREAKDOWN -----------");

        if (total <= 0) {
            System.out.println("No emotion signals detected.");
            return;
        }

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            double value = entry.getValue();
            double percent = value / total * 100.0;
            int barLength = Math.min(25, (int) Math.round(percent / 4));

            System.out.printf(Locale.ROOT, "%-10s | %-25s %5.1f%%%n",
                    entry.getKey(),
                    "#".repeat(barLength),
                    percent);
        }
    }

    private static String reflection(String emotion) {
        switch (emotion) {
            case "Happiness":
                return "Notice what is going well and appreciate it.";
            case "Sadness":
                return "Give yourself time; you do not need to solve everything at once.";
            case "Anger":
                return "Pause before reacting and identify what triggered the feeling.";
            case "Fear":
                return "Separate what you know from what you are imagining.";
            case "Love":
                return "Recognize the people and connections that matter to you.";
            case "Surprise":
                return "Take a moment to process the unexpected event.";
            case "Disgust":
                return "Identify what crossed your boundaries or felt unacceptable.";
            default:
                return "Check in with yourself without judging your feelings.";
        }
    }

    private static String suggestion(String emotion) {
        switch (emotion) {
            case "Happiness":
                return "Record one good thing that happened today.";
            case "Sadness":
                return "Consider talking to someone you trust or taking a gentle break.";
            case "Anger":
                return "Take a few slow breaths before deciding what to do.";
            case "Fear":
                return "Break the concern into one small, manageable next step.";
            case "Love":
                return "Express appreciation to someone important to you.";
            case "Surprise":
                return "Gather the facts before making a decision.";
            case "Disgust":
                return "Consider a constructive boundary or change to the situation.";
            default:
                return "Describe your feelings in more detail to get a clearer result.";
        }
    }

    private static void saveLog(
            String timestamp, String text, String emotion,
            double sentiment, double intensity, String mood)
            throws IOException {

        Path path = Paths.get(LOG_FILE);

        if (Files.notExists(path)) {
            Files.write(path,
                    Collections.singletonList(
                            "Timestamp,Text,DominantEmotion,Sentiment,Intensity,Mood"),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE);
        }

        String row = csv(timestamp) + "," + csv(text) + ","
                + csv(emotion) + "," + sentiment + ","
                + intensity + "," + csv(mood);

        Files.write(path, Collections.singletonList(row),
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND);
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private static void showHistory() {
        Path path = Paths.get(LOG_FILE);

        if (Files.notExists(path)) {
            System.out.println("No history available yet.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(
                    path, StandardCharsets.UTF_8);

            System.out.println("\n----------- RECENT HISTORY -----------");

            int start = Math.max(1, lines.size() - 5);

            for (int i = start; i < lines.size(); i++) {
                System.out.println(lines.get(i));
            }

            if (lines.size() <= 1) {
                System.out.println("No analyses recorded yet.");
            }

        } catch (IOException e) {
            System.out.println("Could not read history: " + e.getMessage());
        }
    }

    private static void showStatistics() {
        Path path = Paths.get(LOG_FILE);

        if (Files.notExists(path)) {
            System.out.println("No statistics available yet.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(
                    path, StandardCharsets.UTF_8);

            Map<String, Integer> counts = new TreeMap<>();
            int validRows = 0;

            for (int i = 1; i < lines.size(); i++) {
                String[] columns = parseCsvLine(lines.get(i));

                if (columns.length >= 6) {
                    counts.merge(columns[2], 1, Integer::sum);
                    validRows++;
                }
            }

            System.out.println("\n----------- SESSION STATISTICS -----------");
            System.out.println("Total saved analyses: " + validRows);

            if (validRows == 0) {
                System.out.println("No recorded analyses yet.");
                return;
            }

            counts.forEach((emotion, count) ->
                    System.out.printf("%-12s : %d%n", emotion, count));

        } catch (IOException e) {
            System.out.println("Could not read statistics: " + e.getMessage());
        }
    }

    // Handles quoted commas and escaped quotes in CSV fields.
    private static String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                if (quoted && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    field.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (ch == ',' && !quoted) {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(ch);
            }
        }

        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }
            }
                              

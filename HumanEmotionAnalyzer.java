
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class HumanEmotionAnalyzer {

    private static final Scanner SC = new Scanner(System.in);
    private static final String HISTORY_FILE = "emotion_history.csv";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static final Map<String, Double> POSITIVE_WORDS =
            createWeights(new String[]{
                "happy", "joy", "amazing", "love", "grateful",
                "excited", "good", "great", "wonderful", "hopeful"
            }, 7.0);

    private static final Map<String, Double> NEGATIVE_WORDS =
            createWeights(new String[]{
                "sad", "hurt", "pain", "angry", "lonely",
                "depressed", "bad", "terrible", "upset", "miserable"
            }, -7.0);

    private static final Set<String> NEGATIONS = new HashSet<>(
            Arrays.asList(
                "not", "no", "never", "without", "isn't",
                "wasn't", "aren't", "don't", "didn't",
                "can't", "cannot", "couldn't", "won't",
                "wouldn't", "hardly"
            )
    );

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("   HUMAN EMOTION ANALYZER - JAVA");
        System.out.println("========================================");
        System.out.println("Analyze feelings using keyword-based rules.");
        System.out.println();

        while (true) {
            System.out.print("Enter your feelings (or type 'exit'): ");

            String input = SC.nextLine().trim().toLowerCase(Locale.ROOT);

            if (input.equals("exit")) {
                System.out.println("Thank you for using Emotion Analyzer!");
                break;
            }

            if (input.isEmpty()) {
                System.out.println("Please enter some text.\n");
                continue;
            }

            analyzeAndDisplay(input);
        }

        SC.close();
    }

    // Main analysis workflow
    public static void analyzeAndDisplay(String input) {

        Map<String, Double> emotionScores = analyzeEmotion(input);
        String primaryEmotion = getPrimaryEmotion(emotionScores);
        double sentiment = calculateSentiment(input);
        double intensity = calculateIntensity(emotionScores);
        String mood = getMoodLevel(sentiment);
        String timestamp = LocalDateTime.now().format(FORMATTER);

        System.out.println("\n========== ANALYSIS RESULT ==========");
        System.out.println("Primary Emotion     : " + primaryEmotion);
        System.out.printf(Locale.ROOT,
                "Sentiment Score     : %.1f / 100%n", sentiment);
        System.out.printf(Locale.ROOT,
                "Emotional Intensity : %.1f%%%n", intensity);
        System.out.println("Mood Level          : " + mood);
        System.out.println("Keywords Detected   : " + detectImportantWords(input));
        System.out.println("Reflection          : " + generateReflection(primaryEmotion));
        System.out.println("Recommendation      : " + generateRecommendation(primaryEmotion));
        System.out.println("Analysis Time       : " + timestamp);

        System.out.println("\n---------- Emotion Weights ----------");

        emotionScores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> System.out.printf(
                        Locale.ROOT, "%-12s : %.2f%n",
                        entry.getKey(), entry.getValue()));

        showEmotionDistribution(emotionScores);

        try {
            saveHistory(timestamp, input, primaryEmotion, sentiment, intensity, mood);
            System.out.println("\nAnalysis saved to " + HISTORY_FILE);
        } catch (IOException e) {
            System.out.println("\nCould not save history: " + e.getMessage());
        }

        System.out.println("======================================\n");
    }

    // Create a weighted word dictionary
    private static Map<String, Double> createWeights(
            String[] words, double weight) {

        Map<String, Double> result = new HashMap<>();

        for (String word : words) {
            result.put(word, weight);
        }

        return Collections.unmodifiableMap(result);
    }

    // Analyze five emotion categories
    public static Map<String, Double> analyzeEmotion(String text) {

        Map<String, Double> scores = new LinkedHashMap<>();

        scores.put("Happiness", weightedCount(text, Map.of(
                "happy", 2.0, "joy", 3.0, "excited", 2.5,
                "amazing", 2.5, "grateful", 2.0, "wonderful", 2.5,
                "good", 1.0, "great", 2.0, "hopeful", 2.0
        )));

        scores.put("Sadness", weightedCount(text, Map.of(
                "sad", 2.0, "cry", 2.0, "lonely", 2.5,
                "depressed", 3.0, "hurt", 2.0, "pain", 2.0,
                "upset", 2.0, "miserable", 3.0
        )));

        scores.put("Anger", weightedCount(text, Map.of(
                "angry", 2.2, "mad", 1.8, "hate", 2.5,
                "frustrated", 2.2, "furious", 3.0,
                "annoyed", 1.8
        )));

        scores.put("Fear", weightedCount(text, Map.of(
                "scared", 2.3, "fear", 2.0, "worried", 2.2,
                "anxious", 2.5, "nervous", 2.0,
                "terrified", 3.0
        )));

        scores.put("Love", weightedCount(text, Map.of(
                "love", 3.0, "caring", 2.0, "special", 2.0,
                "affection", 2.5, "adore", 3.0
        )));

        return scores;
    }

    // Count weighted keywords, excluding negated keywords
    public static double weightedCount(
            String text, Map<String, Double> wordWeights) {

        String[] words = tokenize(text);
        double score = 0;

        for (int i = 0; i < words.length; i++) {
            Double weight = wordWeights.get(words[i]);

            if (weight != null && !isNegated(words, i)) {
                score += weight;
            }
        }

        return score;
    }

    // Simple negation detection using the preceding three tokens
    public static boolean isNegated(String[] words, int index) {

        for (int i = Math.max(0, index - 3); i < index; i++) {
            if (NEGATIONS.contains(words[i])) {
                return true;
            }
        }

        return false;
    }

    // Tokenize words and common contractions
    private static String[] tokenize(String text) {
        return text.toLowerCase(Locale.ROOT)
                .split("[^a-z']+");
    }

    // Find the strongest emotion, or Neutral if no keywords match
    public static String getPrimaryEmotion(
            Map<String, Double> scores) {

        return scores.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > 0)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Neutral");
    }

    // Calculate a rule-based sentiment score from 0 to 100
    public static double calculateSentiment(String text) {

        String[] words = tokenize(text);
        double score = 50.0;

        for (int i = 0; i < words.length; i++) {
            Double value = POSITIVE_WORDS.get(words[i]);

            if (value == null) {
                value = NEGATIVE_WORDS.get(words[i]);
            }

            if (value != null) {
                if (isNegated(words, i)) {
                    score -= value;
                } else {
                    score += value;
                }
            }
        }

        return Math.max(0.0, Math.min(100.0, score));
    }

    // Calculate intensity from matched emotion weights
    public static double calculateIntensity(
            Map<String, Double> scores) {

        double total = scores.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        return Math.min(total * 8.0, 100.0);
    }

    // Classify sentiment score
    public static String getMoodLevel(double sentiment) {

        if (sentiment >= 80) return "Extremely Positive";
        if (sentiment >= 65) return "Positive";
        if (sentiment >= 45) return "Neutral";
        if (sentiment >= 25) return "Negative";

        return "Very Negative";
    }

    // Display each emotion's share of all matched weights
    public static void showEmotionDistribution(
            Map<String, Double> scores) {

        double total = scores.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        System.out.println("\n------ Emotion Distribution ------");

        if (total <= 0) {
            System.out.println("No emotion keywords detected.");
            return;
        }

        scores.forEach((emotion, score) -> {
            double percentage = score / total * 100.0;

            System.out.printf(Locale.ROOT,
                    "%-12s : %5.1f%%%n", emotion, percentage);
        });
    }

    // Find recognized emotion-related words
    public static String detectImportantWords(String text) {

        Set<String> importantWords = new LinkedHashSet<>(
                Arrays.asList(
                    "happy", "sad", "love", "angry", "excited",
                    "hurt", "pain", "scared", "worried", "joy",
                    "anxious", "lonely", "grateful", "frustrated"
                )
        );

        Set<String> found = new LinkedHashSet<>();

        for (String word : tokenize(text)) {
            if (importantWords.contains(word)) {
                found.add(word);
            }
        }

        return found.isEmpty()
                ? "No recognized emotional keywords"
                : String.join(", ", found);
    }

    // Generate a reflection
    public static String generateReflection(String emotion) {

        switch (emotion) {
            case "Happiness":
                return "Your words include positive or uplifting signals.";
            case "Sadness":
                return "Your words include signals associated with sadness.";
            case "Anger":
                return "Your words include signals associated with frustration.";
            case "Fear":
                return "Your words include signals associated with worry.";
            case "Love":
                return "Your words include signals associated with affection.";
            default:
                return "No dominant emotion was identified by the keyword rules.";
        }
    }

    // Generate a general recommendation
    public static String generateRecommendation(String emotion) {

        switch (emotion) {
            case "Happiness":
                return "Notice what is going well and enjoy the moment.";
            case "Sadness":
                return "Consider talking with someone you trust or doing a comforting activity.";
            case "Anger":
                return "Pause, take a few slow breaths, and consider your next step.";
            case "Fear":
                return "Identify what is within your control and take one manageable step.";
            case "Love":
                return "Consider expressing appreciation to someone important to you.";
            default:
                return "Reflect on your feelings and describe them in more detail if helpful.";
        }
    }

    // Save analysis history to CSV
    public static void saveHistory(
            String timestamp,
            String input,
            String emotion,
            double sentiment,
            double intensity,
            String mood) throws IOException {

        Path path = Paths.get(HISTORY_FILE);
        boolean needsHeader =
                !Files.exists(path) || Files.size(path) == 0;

        try (java.io.BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            if (needsHeader) {
                writer.write(
                    "Timestamp,Input,Primary Emotion,Sentiment,Intensity,Mood");
                writer.newLine();
            }

            writer.write(csv(timestamp) + ","
                    + csv(input) + ","
                    + csv(emotion) + ","
                    + sentiment + ","
                    + intensity + ","
                    + csv(mood));

            writer.newLine();
        }
    }

    // Escape values according to basic CSV quoting rules
    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}

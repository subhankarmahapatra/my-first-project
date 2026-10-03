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

    // Weighted dictionaries
    private static final Map<String, Double> POSITIVE =
            assignWeights(new String[]{
                "happy", "joy", "amazing", "love", "grateful",
                "excited", "good", "great", "wonderful", "hopeful"
            }, 6.5);

    private static final Map<String, Double> NEGATIVE =
            assignWeights(new String[]{
                "sad", "hurt", "pain", "angry", "lonely",
                "depressed", "bad", "terrible", "upset", "miserable"
            }, -6.5);

    private static final Set<String> NEGATION_WORDS = new HashSet<>(
            Arrays.asList(
                "not", "no", "never", "without", "isn't",
                "wasn't", "aren't", "don't", "didn't",
                "can't", "cannot", "couldn't", "won't",
                "wouldn't", "hardly"
            )
    );

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("        EMOTION INSIGHT ENGINE");
        System.out.println("========================================");
        System.out.println("Keyword-based emotional analysis tool.\n");

        while (true) {
            System.out.print("Enter your feelings (or type 'exit'): ");
            String text = INPUT.nextLine().trim().toLowerCase(Locale.ROOT);

            if (text.equals("exit")) {
                System.out.println("Session ended. Stay mindful!");
                break;
            }

            if (text.isEmpty()) {
                System.out.println("⚠ Please enter some text.\n");
                continue;
            }

            runAnalysis(text);
        }

        INPUT.close();
    }

    // Core analysis workflow
    private static void runAnalysis(String text) {
        Map<String, Double> emotionMap = evaluateEmotions(text);
        String dominantEmotion = findDominantEmotion(emotionMap);
        double sentimentScore = computeSentiment(text);
        double intensityLevel = computeIntensity(emotionMap);
        String moodCategory = classifyMood(sentimentScore);
        String timeStamp = LocalDateTime.now().format(DATE_FORMAT);

        System.out.println("\n========== RESULT ==========");
        System.out.println("Dominant Emotion    : " + dominantEmotion);
        System.out.printf(Locale.ROOT, "Sentiment Score     : %.1f / 100%n", sentimentScore);
        System.out.printf(Locale.ROOT, "Intensity Level     : %.1f%%%n", intensityLevel);
        System.out.println("Mood Category       : " + moodCategory);
        System.out.println("Keywords Found      : " + extractKeywords(text));
        System.out.println("Reflection          : " + buildReflection(dominantEmotion));
        System.out.println("Suggestion          : " + buildSuggestion(dominantEmotion));
        System.out.println("Analyzed At         : " + timeStamp);

        System.out.println("\n------ Emotion Breakdown ------");
        emotionMap.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> System.out.printf("%-12s : %.2f%n", entry.getKey(), entry.getValue()));

        showDistribution(emotionMap);

        try {
            saveLog(timeStamp, text, dominantEmotion, sentimentScore, intensityLevel, moodCategory);
            System.out.println("\n✔ Analysis saved to " + LOG_FILE);
        } catch (IOException e) {
            System.out.println("\n✘ Could not save log: " + e.getMessage());
        }

        System.out.println("======================================\n");
    }

    // Assign weights to words
    private static Map<String, Double> assignWeights(String[] words, double weight) {
        Map<String, Double> map = new HashMap<>();
        for (String word : words) {
            map.put(word, weight);
        }
        return Collections.unmodifiableMap(map);
    }

    // Evaluate emotions
    private static Map<String, Double> evaluateEmotions(String text) {
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

    // Weighted keyword count
    private static double weightedCount(String text, Map<String, Double> weights) {
        String[] words = tokenize(text);
        double score = 0;
        for (int i = 0; i < words.length; i++) {
            Double weight = weights.get(words[i]);
            if (weight != null && !isNegated(words, i)) {
                score += weight;
            }
        }
        return score;
    }

    // Negation detection
    private static boolean isNegated(String[] words, int index) {
        for (int i = Math.max(0, index - 3); i < index; i++) {
            if (NEGATION_WORDS.contains(words[i])) return true;
        }
        return false;
    }

    // Tokenizer
    private static String[] tokenize(String text) {
        return text.toLowerCase(Locale.ROOT).split("[^a-z']+");
    }

    // Find dominant emotion
    private static String findDominantEmotion(Map<String, Double> scores) {
        return scores.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Neutral");
    }

    // Sentiment calculation
    private static double computeSentiment(String text) {
        String[] words = tokenize(text);
        double score = 50.0;
        for (int i = 0; i < words.length; i++) {
            Double val = POSITIVE.get(words[i]);
            if (val == null) val = NEGATIVE.get(words[i]);
            if (val != null) {
                score += isNegated(words, i) ? -val : val;
            }
        }
        return Math.max(0.0, Math.min(100.0, score));
    }

    // Intensity calculation
    private static double computeIntensity(Map<String, Double> scores) {
        double total = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        return Math.min(total * 8.0, 100.0);
    }

    // Mood classification
    private static String classifyMood(double sentiment) {
        if (sentiment >= 80) return "Extremely Positive";
        if (sentiment >= 65) return "Positive";
        if (sentiment >= 45) return "Neutral";
        if (sentiment >= 25) return "Negative";
        return "Very Negative";
    }

    // Distribution display
    private static void showDistribution(Map<String, Double> scores) {
        double total = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("\n------ Distribution ------");
        if (total <= 0) {
            System.out.println("No emotion keywords detected.");
            return;
        }
        scores.forEach((emotion, score) -> {
            double percent = score / total * 100.0;
            System.out.printf("%-12s : %5.1f%%%

import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

/**
 * ============================================================
 *                 EMOTION INSIGHT ENGINE PRO
 * ============================================================
 *
 * A rule-based emotion and sentiment analysis application.
 *
 * Features:
 *  1. 12+ emotion categories
 *  2. Sentiment score
 *  3. Emotion confidence
 *  4. Emotion intensity
 *  5. Negation detection
 *  6. Intensity modifiers
 *  7. Emoji detection
 *  8. Keyword detection
 *  9. Mood classification
 * 10. Personalized suggestions
 * 11. Reflection messages
 * 12. CSV history
 * 13. History viewer
 * 14. Statistics
 * 15. Average sentiment
 * 16. Average intensity
 * 17. Most frequent emotion
 * 18. Emotion distribution
 * 19. Search history
 * 20. Clear history
 * 21. Export text report
 * 22. Session statistics
 * 23. Help menu
 * 24. Word statistics
 * 25. Question detection
 * 26. Stress/anxiety indicator
 *
 * Compile:
 *      javac EmotionInsightEngine.java
 *
 * Run:
 *      java EmotionInsightEngine
 *
 * ============================================================
 */

public class EmotionInsightEngine {

    // =========================================================
    // CONFIGURATION
    // =========================================================

    private static final Scanner INPUT = new Scanner(System.in);

    private static final String LOG_FILE = "emotion_log.csv";
    private static final String REPORT_FILE = "emotion_report.txt";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static final String VERSION = "2.0 PRO";

    // =========================================================
    // NEGATION WORDS
    // =========================================================

    private static final Set<String> NEGATIONS = Set.of(
            "not",
            "no",
            "never",
            "without",
            "isn't",
            "wasn't",
            "aren't",
            "don't",
            "didn't",
            "can't",
            "cannot",
            "couldn't",
            "won't",
            "wouldn't",
            "hardly",
            "neither",
            "nor"
    );

    // =========================================================
    // INTENSIFIERS
    // =========================================================

    private static final Map<String, Double> INTENSIFIERS =
            Map.ofEntries(

                    Map.entry("very", 1.5),
                    Map.entry("really", 1.5),
                    Map.entry("extremely", 2.0),
                    Map.entry("incredibly", 1.8),
                    Map.entry("so", 1.4),
                    Map.entry("too", 1.4),
                    Map.entry("absolutely", 1.8),
                    Map.entry("totally", 1.6),
                    Map.entry("completely", 1.7),
                    Map.entry("deeply", 1.7),
                    Map.entry("highly", 1.5),
                    Map.entry("super", 1.6),
                    Map.entry("quite", 1.2),
                    Map.entry("truly", 1.4)
            );

    // =========================================================
    // DIMINISHERS
    // =========================================================

    private static final Map<String, Double> DIMINISHERS =
            Map.ofEntries(

                    Map.entry("slightly", 0.5),
                    Map.entry("somewhat", 0.6),
                    Map.entry("little", 0.5),
                    Map.entry("mildly", 0.5),
                    Map.entry("kindof", 0.7),
                    Map.entry("kinda", 0.7),
                    Map.entry("bit", 0.6),
                    Map.entry("barely", 0.4)
            );

    // =========================================================
    // POSITIVE WORDS
    // =========================================================

    private static final Map<String, Double> POSITIVE =
            Map.ofEntries(

                    Map.entry("happy", 6.0),
                    Map.entry("joy", 7.0),
                    Map.entry("joyful", 7.0),
                    Map.entry("amazing", 7.0),
                    Map.entry("love", 6.0),
                    Map.entry("lovely", 6.0),
                    Map.entry("grateful", 6.0),
                    Map.entry("excited", 7.0),
                    Map.entry("good", 3.0),
                    Map.entry("great", 5.0),
                    Map.entry("wonderful", 7.0),
                    Map.entry("hopeful", 5.0),
                    Map.entry("peaceful", 5.0),
                    Map.entry("confident", 5.0),
                    Map.entry("proud", 5.0),
                    Map.entry("relaxed", 4.0),
                    Map.entry("delighted", 7.0),
                    Map.entry("enjoy", 4.0),
                    Map.entry("enjoyed", 4.0),
                    Map.entry("success", 6.0),
                    Map.entry("successful", 6.0),
                    Map.entry("win", 5.0),
                    Map.entry("winner", 6.0),
                    Map.entry("best", 6.0),
                    Map.entry("awesome", 7.0),
                    Map.entry("fantastic", 7.0),
                    Map.entry("brilliant", 7.0),
                    Map.entry("perfect", 7.0),
                    Map.entry("smile", 5.0),
                    Map.entry("laugh", 5.0),
                    Map.entry("fun", 4.0),
                    Map.entry("calm", 4.0),
                    Map.entry("safe", 4.0),
                    Map.entry("thankful", 6.0),
                    Map.entry("optimistic", 6.0)
            );

    // =========================================================
    // NEGATIVE WORDS
    // =========================================================

    private static final Map<String, Double> NEGATIVE =
            Map.ofEntries(

                    Map.entry("sad", -6.0),
                    Map.entry("hurt", -5.0),
                    Map.entry("pain", -5.0),
                    Map.entry("angry", -7.0),
                    Map.entry("lonely", -6.0),
                    Map.entry("depressed", -8.0),
                    Map.entry("bad", -3.0),
                    Map.entry("terrible", -8.0),
                    Map.entry("upset", -5.0),
                    Map.entry("miserable", -8.0),
                    Map.entry("stressed", -6.0),
                    Map.entry("tired", -3.0),
                    Map.entry("hopeless", -8.0),
                    Map.entry("afraid", -6.0),
                    Map.entry("worried", -5.0),
                    Map.entry("anxious", -6.0),
                    Map.entry("frustrated", -6.0),
                    Map.entry("hate", -7.0),
                    Map.entry("horrible", -8.0),
                    Map.entry("awful", -7.0),
                    Map.entry("failure", -6.0),
                    Map.entry("fail", -5.0),
                    Map.entry("problem", -3.0),
                    Map.entry("cry", -5.0),
                    Map.entry("crying", -6.0),
                    Map.entry("broken", -7.0),
                    Map.entry("disappointed", -6.0),
                    Map.entry("disappointing", -6.0),
                    Map.entry("exhausted", -6.0),
                    Map.entry("nervous", -5.0),
                    Map.entry("panic", -7.0),
                    Map.entry("scared", -6.0),
                    Map.entry("fear", -6.0),
                    Map.entry("confused", -3.0),
                    Map.entry("regret", -5.0)
            );

    // =========================================================
    // EMOTION DICTIONARY
    // =========================================================

    private static final Map<String, Map<String, Double>> EMOTIONS =
            createEmotionDictionary();

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        printBanner();

        initializeLogFile();

        while (true) {

            printMenu();

            System.out.print("\nEnter command or describe your feelings:\n> ");

            String input = INPUT.nextLine().trim();

            if (input.equalsIgnoreCase("exit")
                    || input.equalsIgnoreCase("quit")) {

                showSessionSummary();

                System.out.println("\nSession ended.");
                System.out.println("Take care of yourself. ❤️");
                break;
            }

            if (input.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            }

            if (input.equalsIgnoreCase("history")) {
                showHistory();
                continue;
            }

            if (input.equalsIgnoreCase("stats")) {
                showStatistics();
                continue;
            }

            if (input.equalsIgnoreCase("trend")) {
                showTrend();
                continue;
            }

            if (input.equalsIgnoreCase("search")) {
                searchHistory();
                continue;
            }

            if (input.equalsIgnoreCase("clear")) {
                clearHistory();
                continue;
            }

            if (input.equalsIgnoreCase("report")) {
                exportReport();
                continue;
            }

            if (input.equalsIgnoreCase("about")) {
                showAbout();
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

    // =========================================================
    // BANNER
    // =========================================================

    private static void printBanner() {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("              EMOTION INSIGHT ENGINE PRO");
        System.out.println("                       VERSION " + VERSION);
        System.out.println("============================================================");
        System.out.println(" Rule-based emotion + sentiment intelligence system");
        System.out.println("------------------------------------------------------------");
        System.out.println(" Emotions : 12+");
        System.out.println(" Analysis : Sentiment | Intensity | Confidence");
        System.out.println(" Storage  : CSV");
        System.out.println("============================================================");
    }

    // =========================================================
    // MENU
    // =========================================================

    private static void printMenu() {

        System.out.println();
        System.out.println("------------------------------------------------------------");
        System.out.println("COMMANDS");
        System.out.println("------------------------------------------------------------");
        System.out.println(" analyze text  -> Analyze your feelings");
        System.out.println(" history       -> Show recent analyses");
        System.out.println(" stats         -> Show statistics");
        System.out.println(" trend         -> Show emotion trend");
        System.out.println(" search        -> Search history");
        System.out.println(" report        -> Export report");
        System.out.println(" clear         -> Clear history");
        System.out.println(" help          -> Show help");
        System.out.println(" about         -> About program");
        System.out.println(" exit          -> Exit");
        System.out.println("------------------------------------------------------------");
    }

    // =========================================================
    // EMOTION DICTIONARY
    // =========================================================

    private static Map<String, Map<String, Double>>
    createEmotionDictionary() {

        Map<String, Map<String, Double>> result =
                new LinkedHashMap<>();

        result.put("Happiness", weights(
                "happy:3",
                "joy:3",
                "joyful:3",
                "excited:3",
                "amazing:3",
                "grateful:3",
                "wonderful:3",
                "great:2",
                "good:1",
                "delighted:3",
                "cheerful:3",
                "hopeful:2",
                "enjoy:2",
                "awesome:3",
                "fantastic:3",
                "smile:2",
                "laugh:2",
                "fun:2"
        ));

        result.put("Sadness", weights(
                "sad:3",
                "cry:3",
                "crying:3",
                "lonely:3",
                "depressed:4",
                "hurt:2",
                "pain:2",
                "upset:2",
                "miserable:4",
                "hopeless:4",
                "heartbroken:4",
                "grief:4",
                "broken:3",
                "disappointed:3",
                "disappointing:3
        ));

        result.put("Anger", weights(
                "angry:3",
                "mad:2",
                "hate:3",
                "frustrated:3",
                "furious:4",
                "annoyed:2",
                "rage:4",
                "irritated:2",
                "enraged:4",
                "revenge:3"
        ));

        result.put("Fear", weights(
                "scared:3",
                "fear:3",
                "worried:2",
                "anxious:3",
                "nervous:2",
                "terrified:4",
                "afraid:3",
                "panic:4",
                "insecure:2",
                "danger:3",
                "threat:3
        ));

        result.put("Love", weights(
                "love:4",
                "lovely:3",
                "caring:2",
                "special:2",
                "affection:3",
                "adore:4",
                "romantic:3",
                "cherish:3",
                "beloved:3",
                "kiss:2",
                "relationship:2",
                "care:2
        ));

        result.put("Surprise", weights(
                "surprised:3",
                "shocked:3",
                "unexpected:2",
                "astonished:4",
                "wow:2",
                "unbelievable:3",
                "suddenly:2,
                "unexpectedly:2
        ));

        result.put("Disgust", weights(
                "disgusted:4",
                "gross:3",
                "awful:2",
                "revolting:4",
                "nasty:3",
                "repulsive:4",
                "disgusting:4,
                "dirty:2
        ));

        result.put("Trust", weights(
                "trust:4",
                "trusted:4",
                "reliable:3",
                "honest:3",
                "loyal:3",
                "faithful:3",
                "dependable:3",
                "secure:2
        ));

        result.put("Anticipation", weights(
                "expect:2",
                "expecting:3",
                "anticipate:3",
                "future:2",
                "waiting:2",
                "ready:2",
                "soon:2",
                "upcoming:2
        ));

        result.put("Confusion", weights(
                "confused:4",
                "confusing:4",
                "uncertain:3",
                "unsure:3",
                "doubt:3",
                "doubtful:3",
                "lost:3",
                "unclear:3
        ));

        result.put("Pride", weights(
                "proud:4",
                "achievement:4",
                "achieved:4",
                "success:3",
                "successful:3",
                "winner:4",
                "winning:4,
                "accomplished:4
        ));

        result.put("Calmness", weights(
                "calm:4",
                "peaceful:4",
                "relaxed:3",
                "relax:3",
                "quiet:2",
                "comfortable:3",
                "safe:3",
                "serene:4
        ));

        result.put("Stress", weights(
                "stress:4",
                "stressed:4",
                "pressure:3",
                "overwhelmed:4",
                "busy:2",
                "exhausted:3",
                "deadline:3",
                "tension:3
        ));

        return Collections.unmodifiableMap(result);
    }

    // =========================================================
    // CREATE WEIGHTS
    // =========================================================

    private static Map<String, Double> weights(String... entries) {

        Map<String, Double> result = new HashMap<>();

        for (String entry : entries) {

            String[] parts = entry.split(":");

            if (parts.length == 2) {

                try {

                    result.put(
                            parts[0],
                            Double.parseDouble(parts[1])
                    );

                } catch (NumberFormatException ignored) {
                }
            }
        }

        return result;
    }

    // =========================================================
    // ANALYZE
    // =========================================================

    private static void analyze(String originalText) {

        String text = originalText
                .toLowerCase(Locale.ROOT)
                .trim();

        String[] words = tokenize(text);

        if (words.length == 0) {

            System.out.println("No meaningful words detected.");
            return;
        }

        Map<String, Double> scores =
                new LinkedHashMap<>();

        // -----------------------------------------------------
        // Calculate emotion scores
        // -----------------------------------------------------

        for (Map.Entry<String, Map<String, Double>> emotion
                : EMOTIONS.entrySet()) {

            double score = 0.0;

            for (int i = 0; i < words.length; i++) {

                Double weight =
                        emotion.getValue().get(words[i]);

                if (weight != null) {

                    if (!isNegated(words, i)) {

                        score += weight *
                                intensityModifier(words, i);
                    }
                }
            }

            scores.put(emotion.getKey(), score);
        }

        // -----------------------------------------------------
        // Emoji
        // -----------------------------------------------------

        addEmojiScores(text, scores);

        // -----------------------------------------------------
        // Main metrics
        // -----------------------------------------------------

        String dominant =
                findDominant(scores);

        double sentiment =
                sentimentScore(words);

        double intensity =
                intensityScore(scores);

        double confidence =
                emotionConfidence(scores);

        String mood =
                classifyMood(sentiment);

        int positiveWords =
                countPositiveWords(words);

        int negativeWords =
                countNegativeWords(words);

        boolean question =
                originalText.trim().endsWith("?");

        boolean stressWarning =
                detectStressWarning(text);

        String timestamp =
                LocalDateTime.now().format(DATE_FORMAT);

        // -----------------------------------------------------
        // Output
        // -----------------------------------------------------

        System.out.println();
        System.out.println("============================================================");
        System.out.println("                    ANALYSIS RESULT");
        System.out.println("============================================================");

        System.out.println("Dominant emotion : " + dominant);

        System.out.printf(
                Locale.ROOT,
                "Confidence       : %.1f%%%n",
                confidence
        );

        System.out.printf(
                Locale.ROOT,
                "Sentiment score  : %.1f / 100%n",
                sentiment
        );

        System.out.printf(
                Locale.ROOT,
                "Emotion intensity: %.1f%%%n",
                intensity
        );

        System.out.println(
                "Mood category    : " + mood
        );

        System.out.println(
                "Positive words   : " + positiveWords
        );

        System.out.println(
                "Negative words   : " + negativeWords
        );

        System.out.println(
                "Questions        : " +
                        (question ? "Yes" : "No")
        );

        System.out.println(
                "Matched keywords : " +
                        matchedKeywords(words)
        );

        System.out.println(
                "Reflection       : " +
                        reflection(dominant)
        );

        System.out.println(
                "Suggestion       : " +
                        suggestion(dominant)
        );

        if (stressWarning) {

            System.out.println();
            System.out.println(
                    "⚠ Stress indicator: Multiple stress-related signals detected."
            );

            System.out.println(
                    "   Consider taking a short break and focusing on one"
            );

            System.out.println(
                    "   manageable task at a time."
            );
        }

        System.out.println(
                "Timestamp        : " + timestamp
        );

        showEmotionBars(scores);

        showTopEmotions(scores);

        System.out.println(
                "\n------------------------------------------------------------"
        );

        System.out.println(
                "Word count       : " + words.length
        );

        System.out.println(
                "Character count  : " + originalText.length()
        );

        System.out.println(
                "------------------------------------------------------------"
        );

        // -----------------------------------------------------
        // Save
        // -----------------------------------------------------

        try {

            saveLog(
                    timestamp,
                    originalText,
                    dominant,
                    sentiment,
                    intensity,
                    mood
            );

            System.out.println(
                    "✓ Analysis saved to " + LOG_FILE
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not save log: " +
                            e.getMessage()
            );
        }

        System.out.println();
        System.out.println(
                "Note: This is a rule-based estimate, not a clinical assessment."
        );

        System.out.println(
                "============================================================"
        );
    }

    // =========================================================
    // TOKENIZE
    // =========================================================

    private static String[] tokenize(String text) {

        String cleaned =
                text.toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-zA-Z0-9'!?]+", " ")
                        .trim();

        if (cleaned.isEmpty()) {
            return new String[0];
        }

        return cleaned.split("\\s+");
    }

    // =========================================================
    // NEGATION
    // =========================================================

    private static boolean isNegated(
            String[] words,
            int index) {

        int start =
                Math.max(0, index - 3);

        for (int i = start; i < index; i++) {

            if (NEGATIONS.contains(words[i])) {
                return true;
            }
        }

        return false;
    }

    // =========================================================
    // INTENSITY MODIFIER
    // =========================================================

    private static double intensityModifier(
            String[] words,
            int index) {

        double modifier = 1.0;

        int start =
                Math.max(0, index - 3);

        for (int i = start; i < index; i++) {

            String word = words[i];

            if (INTENSIFIERS.containsKey(word)) {

                modifier *=
                        INTENSIFIERS.get(word);

            } else if (DIMINISHERS.containsKey(word)) {

                modifier *=
                        DIMINISHERS.get(word);
            }
        }

        return Math.min(modifier, 3.0);
    }

    // =========================================================
    // EMOJI SCORES
    // =========================================================

    private static void addEmojiScores(
            String text,
            Map<String, Double> scores) {

        addEmoji(text, scores, "😂", "Happiness", 3);
        addEmoji(text, scores, "😊", "Happiness", 3);
        addEmoji(text, scores, "😄", "Happiness", 3);
        addEmoji(text, scores, "😁", "Happiness", 3);
        addEmoji(text, scores, "🤣", "Happiness", 4);
        addEmoji(text, scores, "🥰", "Love", 3);
        addEmoji(text, scores, "😍", "Love", 4);
        addEmoji(text, scores, "❤️", "Love", 4);
        addEmoji(text, scores, "❤", "Love", 4);
        addEmoji(text, scores, "💕", "Love", 3);

        addEmoji(text, scores, "😢", "Sadness", 3);
        addEmoji(text, scores, "😭", "Sadness", 4);
        addEmoji(text, scores, "💔", "Sadness", 4);

        addEmoji(text, scores, "😡", "Anger", 4);
        addEmoji(text, scores, "🤬", "Anger", 4);
        addEmoji(text, scores, "😠", "Anger", 3);

        addEmoji(text, scores, "😨", "Fear", 3);
        addEmoji(text, scores, "😰", "Fear", 3);
        addEmoji(text, scores, "😱", "Fear", 4);

        addEmoji(text, scores, "😲", "Surprise", 3);
        addEmoji(text, scores, "😮", "Surprise", 2);
        addEmoji(text, scores, "😯", "Surprise", 2);

        addEmoji(text, scores, "🤢", "Disgust", 3);
        addEmoji(text, scores, "🤮", "Disgust", 4);

        addEmoji(text, scores, "😌", "Calmness", 3);
        addEmoji(text, scores, "😎", "Pride", 2);
        addEmoji(text, scores, "🤔", "Confusion", 2);
        addEmoji(text, scores, "😵", "Confusion", 3);
    }

    // =========================================================
    // ADD EMOJI
    // =========================================================

    private static void addEmoji(
            String text,
            Map<String, Double> scores,
            String emoji,
            String emotion,
            double points) {

        int count = 0;
        int position = 0;

        while ((position =
                text.indexOf(emoji, position)) != -1) {

            count++;

            position += emoji.length();
        }

        scores.put(
                emotion,
                scores.get(emotion) +
                        count * points
        );
    }

    // =========================================================
    // SENTIMENT SCORE
    // =========================================================

    private static double sentimentScore(
            String[] words) {

        double score = 50.0;

        for (int i = 0; i < words.length; i++) {

            Double value =
                    POSITIVE.get(words[i]);

            if (value == null) {

                value =
                        NEGATIVE.get(words[i]);
            }

            if (value != null) {

                double modifier =
                        intensityModifier(words, i);

                if (isNegated(words, i)) {

                    score -=
                            value * modifier;

                } else {

                    score +=
                            value * modifier;
                }
            }
        }

        return Math.max(
                0,
                Math.min(100, score)
        );
    }

    // =========================================================
    // DOMINANT EMOTION
    // =========================================================

    private static String findDominant(
            Map<String, Double> scores) {

        return scores.entrySet()
                .stream()
                .filter(e -> e.getValue() > 0)
                .max(
                        Map.Entry.comparingByValue()
                )
                .map(Map.Entry::getKey)
                .orElse("Neutral");
    }

    // =========================================================
    // INTENSITY
    // =========================================================

    private static double intensityScore(
            Map<String, Double> scores) {

        double total =
                scores.values()
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();

        return Math.min(
                total * 3.5,
                100.0
        );
    }

    // =========================================================
    // CONFIDENCE
    // =========================================================

    private static double emotionConfidence(
            Map<String, Double> scores) {

        double total =
                scores.values()
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();

        if (total <= 0) {
            return 0;
        }

        double highest =
                Collections.max(
                        scores.values()
                );

        return Math.min(
                highest / total * 100.0,
                100.0
        );
    }

    // =========================================================
    // MOOD CLASSIFICATION
    // =========================================================

    private static String classifyMood(
            double score) {

        if (score >= 85)
            return "Extremely Positive";

        if (score >= 70)
            return "Very Positive";

        if (score >= 60)
            return "Positive";

        if (score >= 45)
            return "Neutral";

        if (score >= 35)
            return "Slightly Negative";

        if (score >= 20)
            return "Negative";

        return "Very Negative";
    }

    // =========================================================
    // MATCHED KEYWORDS
    // =========================================================

    private static String matchedKeywords(
            String[] words) {

        Set<String> found =
                new LinkedHashSet<>();

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

        return found.isEmpty()
                ? "None detected"
                : found.toString();
    }

    // =========================================================
    // POSITIVE WORD COUNT
    // =========================================================

    private static int countPositiveWords(
            String[] words) {

        int count = 0;

        for (String word : words) {

            if (POSITIVE.containsKey(word)) {
                count++;
            }
        }

        return count;
    }

    // =========================================================
    // NEGATIVE WORD COUNT
    // =========================================================

    private static int countNegativeWords(
            String[] words) {

        int count = 0;

        for (String word : words) {

            if (NEGATIVE.containsKey(word)) {
                count++;
            }
        }

        return count;
    }

    // =========================================================
    // EMOTION BARS
    // =========================================================

    private static void showEmotionBars(
            Map<String, Double> scores) {

        double total =
                scores.values()
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();

        System.out.println();
        System.out.println(
                "----------- EMOTION BREAKDOWN -----------"
        );

        if (total <= 0) {

            System.out.println(
                    "No emotion signals detected."
            );

            return;
        }

        for (Map.Entry<String, Double> entry
                : scores.entrySet()) {

            double value =
                    entry.getValue();

            if (value <= 0) {
                continue;
            }

            double percent =
                    value / total * 100.0;

            int barLength =
                    Math.min(
                            30,
                            (int) Math.round(
                                    percent / 3.3
                            )
                    );

            System.out.printf(
                    Locale.ROOT,
                    "%-14s | %-30s %5.1f%%%n",
                    entry.getKey(),
                    "#".repeat(barLength),
                    percent
            );
        }
    }

    // =========================================================
    // TOP EMOTIONS
    // =========================================================

    private static void showTopEmotions(
            Map<String, Double> scores) {

        List<Map.Entry<String, Double>> list =
                new ArrayList<>(
                        scores.entrySet()
                );

        list.removeIf(
                entry -> entry.getValue() <= 0
        );

        list.sort(
                Map.Entry.<String, Double>
                        comparingByValue()
                        .reversed()
        );

        System.out.println();
        System.out.println(
                "----------- TOP EMOTIONS -----------"
        );

        int limit =
                Math.min(3, list.size());

        for (int i = 0; i < limit; i++) {

            Map.Entry<String, Double> entry =
                    list.get(i);

            System.out.printf(
                    Locale.ROOT,
                    "%d. %-15s %.2f points%n",
                    i + 1,
                    entry.getKey(),
                    entry.getValue()
            );
        }

        if (limit == 0) {

            System.out.println(
                    "No strong emotions detected."
            );
        }
    }

    // =========================================================
    // REFLECTION
    // =========================================================

    private static String reflection(
            String emotion) {

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

            case "Trust":
                return "Think about what or who makes you feel secure.";

            case "Anticipation":
                return "Focus on what you can prepare for rather than predicting everything.";

            case "Confusion":
                return "Break the situation into smaller questions.";

            case "Pride":
                return "Recognize the effort behind your achievement.";

            case "Calmness":
                return "Protect the conditions that are helping you stay calm.";

            case "Stress":
                return "Identify the biggest source of pressure and address one part at a time.";

            default:
                return "Check in with yourself without judging your feelings.";
        }
    }

    // =========================================================
    // SUGGESTION
    // =========================================================

    private static String suggestion(
            String emotion) {

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

            case "Trust":
                return "Maintain communication with people you trust.";

            case "Anticipation":
                return "Prepare one practical step for what is coming next.";

            case "Confusion":
                return "Write down what you know and what you still need to understand.";

            case "Pride":
                return "Use this achievement as evidence of your capability.";

            case "Calmness":
                return "Continue with activities that help you maintain balance.";

            case "Stress":
                return "Take a short break and divide the problem into smaller tasks.";

            default:
                return "Describe your feelings in more detail to get a clearer result.";
        }
    }

    // =========================================================
    // STRESS WARNING
    // =========================================================

    private static boolean detectStressWarning(
            String text) {

        String[] stressWords = {

                "stressed",
                "stress",
                "overwhelmed",
                "pressure",
                "panic",
                "anxious",
                "anxiety",
                "exhausted",
                "deadline",
                "tension",
                "can't handle",
                "too much"
        };

        int matches = 0;

        for (String word : stressWords) {

            if (text.contains(word)) {
                matches++;
            }
        }

        return matches >= 2;
    }

    // =========================================================
    // SAVE LOG
    // =========================================================

    private static void saveLog(
            String timestamp,
            String text,
            String emotion,
            double sentiment,
            double intensity,
            String mood)
            throws IOException {

        Path path =
                Paths.get(LOG_FILE);

        if (Files.notExists(path)) {

            Files.write(
                    path,
                    Collections.singletonList(
                            "Timestamp,Text,DominantEmotion,Sentiment,Intensity,Mood"
                    ),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE
            );
        }

        String row =
                csv(timestamp)
                        + ","
                        + csv(text)
                        + ","
                        + csv(emotion)
                        + ","
                        + sentiment
                        + ","
                        + intensity
                        + ","
                        + csv(mood);

        Files.write(
                path,
                Collections.singletonList(row),
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND
        );
    }

    // =========================================================
    // CSV ESCAPE
    // =========================================================

    private static String csv(
            String value) {

        if (value == null) {
            value = "";
        }

        return "\""
                + value.replace("\"", "\"\"")
                + "\"";
    }

    // =========================================================
    // HISTORY
    // =========================================================

    private static void showHistory() {

        Path path =
                Paths.get(LOG_FILE);

        if (Files.notExists(path)) {

            System.out.println(
                    "\nNo history available yet."
            );

            return;
        }

        try {

            List<String> lines =
                    Files.readAllLines(
                            path,
                            StandardCharsets.UTF_8
                    );

            System.out.println();
            System.out.println(
                    "----------- RECENT HISTORY -----------"
            );

            if (lines.size() <= 1) {

                System.out.println(
                        "No analyses recorded yet."
                );

                return;
            }

            int start =
                    Math.max(
                            1,
                            lines.size() - 10
                    );

            for (int i = start;
                 i < lines.size();
                 i++) {

                String[] columns =
                        parseCsvLine(
                                lines.get(i)
                        );

                if (columns.length >= 6) {

                    System.out.println();
                    System.out.println(
                            "Date      : " +
                                    columns[0]
                    );

                    System.out.println(
                            "Emotion   : " +
                                    columns[2]
                    );

                    System.out.println(
                            "Sentiment : " +
                                    columns[3]
                    );

                    System.out.println(
                            "Intensity : " +
                                    columns[4]
                    );

                    System.out.println(
                            "Mood      : " +
                                    columns[5]
                    );

                    System.out.println(
                            "Text      : " +
                                    columns[1]
                    );

                    System.out.println(
                            "--------------------------------------"
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Could not read history: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // STATISTICS
    // =========================================================

    private static void showStatistics() {

        Path path =
                Paths.get(LOG_FILE);

        if (Files.notExists(path)) {

            System.out.println(
                    "\nNo statistics available yet."
            );

            return;
        }

        try {

            List<String> lines =
                    Files.readAllLines(
                            path,
                            StandardCharsets.UTF_8
                    );

            Map<String, Integer> counts =
                    new TreeMap<>();

            int validRows = 0;

            double sentimentTotal = 0;
            double intensityTotal = 0;

            for (int i = 1;
                 i < lines.size();
                 i++) {

                String[] columns =
                        parseCsvLine(
                                lines.get(i)
                        );

                if (columns.length >= 6) {

                    counts.merge(
                            columns[2],
                            1,
                            Integer::sum
                    );

                    try {

                        sentimentTotal +=
                                Double.parseDouble(
                                        columns[3]
                                );

                        intensityTotal +=
                                Double.parseDouble(
                                        columns[4]
                                );

                    } catch (NumberFormatException ignored) {
                    }

                    validRows++;
                }
            }

            System.out.println();
            System.out.println(
                    "============================================================"
            );

            System.out.println(
                    "                    SESSION STATISTICS"
            );

            System.out.println(
                    "============================================================"
            );

            System.out.println(
                    "Total analyses : " +
                            validRows
            );

            if (validRows == 0) {

                System.out.println(
                        "No recorded analyses yet."
                );

                return;
            }

            double averageSentiment =
                    sentimentTotal / validRows;

            double averageIntensity =
                    intensityTotal / validRows;

            System.out.printf(
                    Locale.ROOT,
                    "Average sentiment : %.2f / 100%n",
                    averageSentiment
            );

            System.out.printf(
                    Locale.ROOT,
                    "Average intensity : %.2f%%%n",
                    averageIntensity
            );

            String mostFrequent =
                    counts.entrySet()
                            .stream()
                            .max(
                                    Map.Entry.comparingByValue()
                            )
                            .map(
                                    Map.Entry::getKey
                            )
                            .orElse("None");

            System.out.println(
                    "Most frequent emotion : " +
                            mostFrequent
            );

            System.out.println();
            System.out.println(
                    "Emotion frequency:"
            );

            counts.forEach(
                    (emotion, count) ->
                            System.out.printf(
                                    "%-16s : %d%n",
                                    emotion,
                                    count
                            )
            );

            System.out.println(
                    "============================================================"
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not read statistics: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // TREND
    // =========================================================

    private static void showTrend() {

        Path path =
                Paths.get(LOG_FILE);

        if (Files.notExists(path)) {

            System.out.println(
                    "\nNo trend data available."
            );

            return;
        }

        try {

            List<String> lines =
                    Files.readAllLines(
                            path,
                            StandardCharsets.UTF_8
                    );

            List<Double> sentiments =
                    new ArrayList<>();

            for (int i = 1;
                 i < lines.size();
                 i++) {

                String[] columns =
                        parseCsvLine(
                                lines.get(i)
                        );

                if (columns.length >= 6) {

                    try {

                        sentiments.add(
                                Double.parseDouble(
                                        columns[3]
                                )
                        );

                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            if (sentiments.size() < 2) {

                System.out.println(
                        "Need at least two analyses to calculate a trend."
                );

                return;
            }

            int recentCount =
                    Math.min(
                            5,
                            sentiments.size()
                    );

            double recentAverage = 0;

            for (
                    int i = sentiments.size() - recentCount;
                    i < sentiments.size();
                    i++
            ) {

                recentAverage +=
                        sentiments.get(i);
            }

            recentAverage /=
                    recentCount;

            double previousAverage = 0;

            int previousStart =
                    Math.max(
                            0,
                            sentiments.size()
                                    - recentCount * 2
                    );

            int previousEnd =
                    sentiments.size()
                            - recentCount;

            if (previousEnd > previousStart) {

                for (int i = previousStart;
                     i < previousEnd;
                     i++) {

                    previousAverage +=
                            sentiments.get(i);
                }

                previousAverage /=
                        (previousEnd -
                                previousStart);
            }

            System.out.println();
            System.out.println(
                    "----------- SENTIMENT TREND -----------"
            );

            System.out.printf(
                    Locale.ROOT,
                    "Recent average   : %.2f%n",
                    recentAverage
            );

            System.out.printf(
                    Locale.ROOT,
                    "Previous average : %.2f%n",
                    previousAverage
            );

            double difference =
                    recentAverage -
                            previousAverage;

            if (difference > 5) {

                System.out.println(
                        "Trend             : IMPROVING ↑"
                );

            } else if (difference < -5) {

                System.out.println(
                        "Trend             : DECLINING ↓"
                );

            } else {

                System.out.println(
                        "Trend             : STABLE →"
                );
            }

        } catch (IOException e) {

            System.out.println(
                    "Could not calculate trend: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // SEARCH HISTORY
    // =========================================================

    private static void searchHistory() {

        Path path =
                Paths.get(LOG_FILE);

        if (Files.notExists(path)) {

            System.out.println(
                    "No history available."
            );

            return;
        }

        System.out.print(
                "Enter keyword to search: "
        );

        String keyword =
                INPUT.nextLine()
                        .trim()
                        .toLowerCase(Locale.ROOT);

        if (keyword.isEmpty()) {

            System.out.println(
                    "Search keyword cannot be empty."
            );

            return;
        }

        try {

            List<String> lines =
                    Files.readAllLines(
                            path,
                            StandardCharsets.UTF_8
                    );

            int matches = 0;

            System.out.println();
            System.out.println(
                    "----------- SEARCH RESULTS -----------"
            );

            for (int i = 1;
                 i < lines.size();
                 i++) {

                if (lines.get(i)
                        .toLowerCase(Locale.ROOT)
                        .contains(keyword)) {

                    String[] columns =
                            parseCsvLine(
                                    lines.get(i)
                            );

                    if (columns.length >= 6) {

                        System.out.println();
                        System.out.println(
                                "Date    : " +
                                        columns[0]
                        );

                        System.out.println(
                                "Emotion : " +
                                        columns[2]
                        );

                        System.out.println(
                                "Mood    : " +
                                        columns[5]
                        );

                        System.out.println(
                                "Text    : " +
                                        columns[1]
                        );

                        matches++;
                    }
                }
            }

            System.out.println();
            System.out.println(
                    "Matches found: " + matches
            );

        } catch (IOException e) {

            System.out.println(
                    "Search failed: " +
                            e.getMessage()
            );
        }
    }

    // =========================================================
    // CLEAR HISTORY
    // =========================================================

    private static void clearHistory() {

        Path path =
                Paths.get(LOG_FILE);

        if (Files.notExists(path)) {

            System.out.println(
                    "History is already empty."
            );

            return;
        }

        System.out.print(
                "Are you sure you want to clear history? (yes/no): "
        );

        String confirmation =
                INPUT.nextLine()
                        .trim();

        if (!confirmation.equalsIgnoreCase("yes")) {

            System.out.println(
                    "Clear operation cancelled."
            );

            return;
        }

        try {

            Files.deleteIfExists(path);

            initializeLogFile();

            System.out.println(
                    "✓ History cleared successfully."
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not clear history: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // EXPORT REPORT
    // =========================================================

    private static void exportReport() {

        Path path =
                Paths.get(LOG_FILE);

        if (Files.notExists(path)) {

            System.out.println(
                    "No data available for report."
            );

            return;
        }

        try {

            List<String> lines =
                    Files.readAllLines(
                            path,
                            StandardCharsets.UTF_8
                    );

            int total = 0;

            Map<String, Integer> emotions =
                    new TreeMap<>();

            double sentimentTotal = 0;
            double intensityTotal = 0;

            for (int i = 1;
                 i < lines.size();
                 i++) {

                String[] columns =
                        parseCsvLine(
                                lines.get(i)
                        );

                if (columns.length >= 6) {

                    total++;

                    emotions.merge(
                            columns[2],
                            1,
                            Integer::sum
                    );

                    try {

                        sentimentTotal +=
                                Double.parseDouble(
                                        columns[3]
                                );

                        intensityTotal +=
                                Double.parseDouble(
                                        columns[4]
                                );

                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            StringBuilder report =
                    new StringBuilder();

            report.append(
                    "================================================\n"
            );

            report.append(
                    "          EMOTION INSIGHT ENGINE REPORT\n"
            );

            report.append(
                    "================================================\n\n"
            );

            report.append(
                    "Generated: "
            );

            report.append(
                    LocalDateTime.now()
                            .format(DATE_FORMAT)
            );

            report.append("\n\n");

            report.append(
                    "Total analyses: "
            );

            report.append(total);

            report.append("\n");

            if (total > 0) {

                report.append(
                        String.format(
                                Locale.ROOT,
                                "Average sentiment: %.2f / 100%n",
                                sentimentTotal / total
                        )
                );

                report.append(
                        String.format(
                                Locale.ROOT,
                                "Average intensity: %.2f%%%n",
                                intensityTotal / total
                        )
                );
            }

            report.append("\n");
            report.append(
                    "Emotion distribution:\n"
            );

            for (
                    Map.Entry<String, Integer> entry
                    : emotions.entrySet()
            ) {

                report.append(
                        String.format(
                                Locale.ROOT,
                                "%-18s : %d%n",
                                entry.getKey(),
                                entry.getValue()
                        )
                );
            }

            report.append("\n");
            report.append(
                    "================================================\n"
            );

            Files.writeString(
                    Paths.get(REPORT_FILE),
                    report.toString(),
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "✓ Report exported to " +
                            REPORT_FILE
            );

        } catch (IOException e) {

            System.out.println(
                    "Could not export report: "
                            + e.getMessage()
            );
        }
    }

    // =========================================================
    // HELP
    // =========================================================

    private static void showHelp() {

        System.out.println();
        System.out.println(
                "============================================================"
        );

        System.out.println(
                "                     HELP CENTER"
        );

        System.out.println(
                "============================================================"
        );

        System.out.println();
        System.out.println(
                "1. Analyze feelings"
        );

        System.out.println(
                "   Simply type something like:"
        );

        System.out.println(
                "   I am very happy today because I passed my exam."
        );

        System.out.println();

        System.out.println(
                "2. history"
        );

        System.out.println(
                "   Shows your recent emotion analyses."
        );

        System.out.println();

        System.out.println(
                "3. stats"
        );

        System.out.println(
                "   Shows total analyses and emotion statistics."
        );

        System.out.println();

        System.out.println(
                "4. trend"
        );

        System.out.println(
                "   Compares recent sentiment with previous sentiment."
        );

        System.out.println();

        System.out.println(
                "5. search"
        );

        System.out.println(
                "   Searches previous analyses."
        );

        System.out.println();

        System.out.println(
                "6. report"
        );

        System.out.println(
                "   Creates emotion_report.txt."
        );

        System.out.println();

        System.out.println(
                "7. clear"
        );

        System.out.println(
                "   Deletes previous analysis history."
        );

        System.out.println();

        System.out.println(
                "8. exit"
        );

        System.out.println(
                "   Closes the application."
        );

        System.out.println(
                "============================================================"
        );
    }

    // =========================================================
    // ABOUT
    // =========================================================

    private static void showAbout() {

        System.out.println();
        System.out.println(
                "============================================================"
        );

        System.out.println(
                "                EMOTION INSIGHT ENGINE"
        );

        System.out.println(
                "                       PRO " + VERSION
        );

        System.out.println(
                "============================================================"
        );

        System.out.println(
                "Language       : Java"
        );

        System.out.println(
                "Architecture   : Rule-based NLP"
        );

        System.out.println(
                "Storage        : CSV"
        );

        System.out.println(
                "External APIs  : None"
        );

        System.out.println(
                "Dependencies   : Java Standard Library"
        );

        System.out.println(
                "Purpose        : Educational emotion analysis"
        );

        System.out.println(
                "============================================================"
        );
    }

    // =========================================================
    // SESSION SUMMARY
    // =========================================================

    private static void showSessionSummary() {

        Path path =
                Paths.get(LOG_FILE);

        if (Files.notExists(path)) {
            return;
        }

        try {

            List<String> lines =
                    Files.readAllLines(
                            path,
                            StandardCharsets.UTF_8
                    );

            if (lines.size() <= 1) {
                return;
            }

            System.out.println();
            System.out.println(
                    "----------- SESSION SUMMARY -----------"
            );

            System.out.println(
                    "Saved analyses: " +
                            (lines.size() - 1)
            );

        } catch (IOException ignored) {
        }
    }

    // =========================================================
    // INITIALIZE LOG
    // =========================================================

    private static void initializeLogFile() {

        Path path =
                Paths.get(LOG_FILE);

        if (Files.exists(path)) {
            return;
        }

        try {

            Files.write(
                    path,
                    Collections.singletonList(
                            "Timestamp,Text,DominantEmotion,Sentiment,Intensity,Mood"
                    ),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE
            );

        } catch (IOException e) {

            System.out.println(
                    "Warning: Could not initialize log file."
            );
        }
    }

    // =========================================================
    // CSV PARSER
    // =========================================================

    private static String[] parseCsvLine(
            String line) {

        List<String> fields =
                new ArrayList<>();

        StringBuilder field =
                new StringBuilder();

        boolean quoted = false;

        for (int i = 0;
             i < line.length();
             i++) {

            char ch =
                    line.charAt(i);

            if (ch == '"') {

                if (quoted
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    field.append('"');

                    i++;

                } else {

                    quoted = !quoted;
                }

            } else if (
                    ch == ','
                            && !quoted
            ) {

                fields.add(
                        field.toString()
                );

                field.setLength(0);

            } else {

                field.append(ch);
            }
        }

        fields.add(
                field.toString()
        );

        return fields.toArray(
                new String[0]
        );
    }
}

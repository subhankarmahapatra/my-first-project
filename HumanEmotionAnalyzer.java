import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class EmotionInsightEngine {

    private static final Scanner INPUT = new Scanner(System.in);
    private static final String LOG_FILE = "emotion_log.csv";
    private static final String REPORT_FILE = "emotion_report.txt";
    private static final String VERSION = "4.0 PRO";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static final Map<String, Double> POSITIVE = new LinkedHashMap<>();
    private static final Map<String, Double> NEGATIVE = new LinkedHashMap<>();
    private static final Map<String, Map<String, Double>> EMOTIONS =
            new LinkedHashMap<>();

    private static final Map<String, Double> INTENSIFIERS =
            new HashMap<>();
    private static final Map<String, Double> DIMINISHERS =
            new HashMap<>();

    private static final Set<String> NEGATIONS = new HashSet<>(
            Arrays.asList(
                    "not", "no", "never", "without", "isn't",
                    "wasn't", "aren't", "don't", "didn't",
                    "can't", "cannot", "couldn't", "won't",
                    "wouldn't", "shouldn't", "hardly", "nothing",
                    "neither", "nor", "shan't"
            )
    );

    private static final Map<String, Integer> SESSION_EMOTIONS =
            new LinkedHashMap<>();

    private static int sessionAnalyses = 0;
    private static double sessionSentimentTotal = 0;
    private static double sessionIntensityTotal = 0;

    private static final class AnalysisResult {
        String timestamp;
        String text;
        String sentiment;
        String primaryEmotion;
        String mood;
        String suggestion;

        double sentimentScore;
        double intensity;
        double confidence;
        double stressIndicator;
        double anxietyIndicator;

        int wordCount;
        int characterCount;

        Map<String, Double> emotionScores = new LinkedHashMap<>();
        List<String> matchedKeywords = new ArrayList<>();
        List<String> flags = new ArrayList<>();
    }

    public static void main(String[] args) {
        initializeDictionaries();
        initializeLogFile();
        printBanner();

        while (true) {
            printMenu();
            System.out.print("\nEnter a command or describe your feelings:\n> ");

            String input;

            try {
                input = INPUT.nextLine().trim();
            } catch (NoSuchElementException e) {
                break;
            }

            if (input.equalsIgnoreCase("exit")
                    || input.equalsIgnoreCase("quit")) {
                showSessionSummary();
                System.out.println("\nSession ended. Take care!");
                break;
            }

            switch (input.toLowerCase(Locale.ROOT)) {
                case "help":
                    showHelp();
                    break;
                case "about":
                    showAbout();
                    break;
                case "history":
                    showHistory();
                    break;
                case "stats":
                    showStatistics();
                    break;
                case "trend":
                    showTrend();
                    break;
                case "search":
                    searchHistory();
                    break;
                case "report":
                    exportReport();
                    break;
                case "clear":
                    clearHistory();
                    break;
                case "session":
                    showSessionSummary();
                    break;
                default:
                    if (input.isEmpty()) {
                        System.out.println("Please enter some text.");
                    } else {
                        analyzeAndSave(input);
                    }
            }
        }

        INPUT.close();
    }

    // =========================================================
    // DICTIONARIES
    // =========================================================

    private static void initializeDictionaries() {
        addWords(POSITIVE, 4,
                "happy", "joy", "joyful", "excited", "amazing",
                "grateful", "wonderful", "great", "good", "delighted",
                "cheerful", "hopeful", "enjoy", "awesome", "fantastic",
                "smile", "laugh", "fun", "positive", "glad", "pleased",
                "love", "lovely", "success", "successful", "win",
                "winner", "best", "brilliant", "perfect", "peaceful",
                "confident", "proud", "relaxed", "thankful", "optimistic",
                "calm", "safe", "relief", "relieved", "beautiful",
                "inspired", "motivated", "comfortable", "content",
                "satisfied", "excellent", "kind", "blessed"
        );

        addWords(NEGATIVE, -4,
                "sad", "hurt", "pain", "angry", "lonely", "depressed",
                "bad", "terrible", "upset", "miserable", "stressed",
                "tired", "hopeless", "afraid", "worried", "anxious",
                "frustrated", "hate", "horrible", "awful", "failure",
                "fail", "problem", "cry", "crying", "broken",
                "disappointed", "disappointing", "exhausted", "nervous",
                "panic", "scared", "fear", "confused", "regret",
                "stress", "overwhelmed", "unhappy", "heartbroken",
                "helpless", "irritated", "furious", "annoyed",
                "disgusted", "guilty", "ashamed", "bored", "weak",
                "worthless", "difficult", "worst", "negative"
        );

        addWords(INTENSIFIERS, 1.5,
                "very", "really", "extremely", "incredibly",
                "absolutely", "totally", "completely", "deeply",
                "highly", "super", "truly", "immensely"
        );

        INTENSIFIERS.put("extremely", 2.0);
        INTENSIFIERS.put("incredibly", 1.8);
        INTENSIFIERS.put("absolutely", 1.7);

        addWords(DIMINISHERS, 0.6,
                "slightly", "somewhat", "little", "mildly",
                "kinda", "kindof", "barely", "bit"
        );

        DIMINISHERS.put("barely", 0.4);
        DIMINISHERS.put("slightly", 0.5);

        putEmotion("Happiness",
                "happy", "joy", "joyful", "excited", "delighted",
                "cheerful", "glad", "pleased", "awesome", "fantastic",
                "smile", "laugh", "fun", "amazing", "wonderful",
                "enjoy", "positive", "excellent");

        putEmotion("Sadness",
                "sad", "cry", "crying", "lonely", "depressed",
                "hurt", "pain", "upset", "miserable", "hopeless",
                "heartbroken", "grief", "broken", "disappointed",
                "sorrow", "unhappy", "tears", "alone", "helpless");

        putEmotion("Anger",
                "angry", "mad", "hate", "frustrated", "furious",
                "annoyed", "rage", "irritated", "enraged",
                "revenge", "outraged", "irritating");

        putEmotion("Fear",
                "scared", "fear", "worried", "anxious", "nervous",
                "terrified", "afraid", "panic", "insecure",
                "danger", "threat", "frightened", "worry");

        putEmotion("Love",
                "love", "lovely", "caring", "special", "affection",
                "adore", "romantic", "cherish", "beloved", "kiss",
                "relationship", "care", "darling", "dear");

        putEmotion("Surprise",
                "surprised", "shocked", "unexpected", "astonished",
                "wow", "unbelievable", "suddenly", "surprise", "amazed");

        putEmotion("Disgust",
                "disgusted", "gross", "awful", "revolting", "nasty",
                "repulsive", "disgusting", "dirty", "dislike", "sickening");

        putEmotion("Trust",
                "trust", "trusted", "reliable", "honest", "loyal",
                "faithful", "dependable", "secure", "believe", "respect");

        putEmotion("Anticipation",
                "expect", "expecting", "anticipate", "future",
                "waiting", "ready", "upcoming", "tomorrow",
                "plan", "planning", "excited");

        putEmotion("Confusion",
                "confused", "confusing", "uncertain", "unsure",
                "doubt", "doubtful", "lost", "unclear",
                "puzzled", "wondering");

        putEmotion("Pride",
                "proud", "achievement", "achieved", "success",
                "successful", "winner", "winning", "accomplished",
                "accomplishment", "progress");

        putEmotion("Calmness",
                "calm", "peaceful", "relaxed", "relax", "quiet",
                "comfortable", "safe", "serene", "peace",
                "relief", "relieved");

        putEmotion("Stress",
                "stress", "stressed", "pressure", "overwhelmed",
                "busy", "exhausted", "deadline", "tension",
                "burnout", "overworked", "workload");

        putEmotion("Gratitude",
                "grateful", "thankful", "thanks", "appreciate",
                "appreciated", "blessed", "gratitude");

        putEmotion("Disappointment",
                "disappointed", "disappointing", "failure", "failed",
                "regret", "letdown", "unfulfilled");

        putEmotion("Confidence",
                "confident", "capable", "brave", "courage",
                "determined", "strong", "prepared", "believe");
    }

    private static void addWords(
            Map<String, Double> dictionary,
            double weight,
            String... words) {

        for (String word : words) {
            dictionary.put(word, weight);
        }
    }

    private static void putEmotion(String name, String... words) {
        Map<String, Double> dictionary = new LinkedHashMap<>();

        for (String word : words) {
            dictionary.put(word, 1.0);
        }

        EMOTIONS.put(name, dictionary);
    }

    // =========================================================
    // BANNER AND MENU
    // =========================================================

    private static void printBanner() {
        System.out.println("\n======================================================");
        System.out.println("           EMOTION INSIGHT ENGINE PRO");
        System.out.println("                    VERSION " + VERSION);
        System.out.println("======================================================");
        System.out.println("Rule-based emotion and sentiment analysis");
        System.out.println("16 emotion categories | Sentiment | Mood | History");
        System.out.println("======================================================");
    }

    private static void printMenu() {
        System.out.println("\n-------------------- COMMANDS --------------------");
        System.out.println("Type any text to analyze it.");
        System.out.println("history  - View previous analyses");
        System.out.println("stats    - View statistics");
        System.out.println("trend    - View recent emotion trends");
        System.out.println("search   - Search saved history");
        System.out.println("report   - Export a text report");
        System.out.println("clear    - Clear saved history");
        System.out.println("session  - Current session summary");
        System.out.println("help     - Help");
        System.out.println("about    - About this program");
        System.out.println("exit     - Exit");
        System.out.println("--------------------------------------------------");
    }

    // =========================================================
    // ANALYSIS
    // =========================================================

    private static void analyzeAndSave(String text) {
        AnalysisResult result = analyzeText(text);
        displayResult(result);
        saveResult(result);
        updateSession(result);
    }

    private static AnalysisResult analyzeText(String text) {
        AnalysisResult result = new AnalysisResult();

        result.timestamp = LocalDateTime.now().format(DATE_FORMAT);
        result.text = text;
        result.characterCount = text.length();

        String cleaned = text.toLowerCase(Locale.ROOT);
        String[] tokens = cleaned.split("[^\\p{L}\\p{N}']+");

        List<String> words = new ArrayList<>();

        for (String token : tokens) {
            if (!token.isEmpty()) {
                words.add(token);
            }
        }

        result.wordCount = words.size();

        Map<String, Double> scores = new LinkedHashMap<>();

        for (String emotion : EMOTIONS.keySet()) {
            scores.put(emotion, 0.0);
        }

        double sentiment = 0.0;
        int matchedSentimentWords = 0;
        int negationWindow = 0;
        double modifier = 1.0;

        Map<String, Integer> frequencies = new HashMap<>();

        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);

            frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);

            if (NEGATIONS.contains(word)) {
                negationWindow = 3;
                continue;
            }

            if (INTENSIFIERS.containsKey(word)) {
                modifier = INTENSIFIERS.get(word);
                continue;
            }

            if (DIMINISHERS.containsKey(word)) {
                modifier = DIMINISHERS.get(word);
                continue;
            }

            boolean negated = negationWindow > 0;
            double wordModifier = modifier;

            if (word.endsWith("n't") && word.length() > 3) {
                negated = true;
            }

            Double positiveWeight = POSITIVE.get(word);
            Double negativeWeight = NEGATIVE.get(word);

            if (positiveWeight != null) {
                double value = positiveWeight * wordModifier;

                if (negated) {
                    sentiment -= Math.abs(value);
                    addEmotionScore(scores, "Happiness", -1.0);
                    result.matchedKeywords.add(word + " (negated)");
                } else {
                    sentiment += value;
                    addEmotionScore(scores, "Happiness", wordModifier);
                    result.matchedKeywords.add(word);
                }

                matchedSentimentWords++;
            }

            if (negativeWeight != null) {
                double value = negativeWeight * wordModifier;

                if (negated) {
                    sentiment += Math.abs(value) * 0.5;
                    result.matchedKeywords.add(word + " (negated)");
                } else {
                    sentiment += value;
                    result.matchedKeywords.add(word);
                }

                matchedSentimentWords++;
            }

            for (Map.Entry<String, Map<String, Double>> entry
                    : EMOTIONS.entrySet()) {

                Double weight = entry.getValue().get(word);

                if (weight != null) {
                    double score = weight * wordModifier;

                    if (negated) {
                        score *= -0.5;
                    }

                    addEmotionScore(scores, entry.getKey(), score);
                }
            }

            if (negationWindow > 0) {
                negationWindow--;
            }

            modifier = 1.0;
        }

        // Handle common phrases that consist of multiple words.
        String normalized = cleaned.replaceAll("[^\\p{L}\\p{N}' ]", " ")
                                   .replaceAll("\\s+", " ")
                                   .trim();

        if (normalized.contains("thank you")) {
            addEmotionScore(scores, "Gratitude", 3.0);
            sentiment += 3.0;
        }

        if (normalized.contains("looking forward")) {
            addEmotionScore(scores, "Anticipation", 2.0);
        }

        if (normalized.contains("fed up")) {
            addEmotionScore(scores, "Anger", 2.0);
            addEmotionScore(scores, "Stress", 1.0);
            sentiment -= 2.0;
        }

        if (normalized.contains("give up")) {
            addEmotionScore(scores, "Sadness", 2.0);
            sentiment -= 2.0;
        }

        // Repeated-word detection.
        for (Map.Entry<String, Integer> entry : frequencies.entrySet()) {
            if (entry.getValue() >= 3) {
                result.flags.add(
                        "Repeated word: " + entry.getKey()
                        + " (" + entry.getValue() + " times)"
                );
            }
        }

        // Punctuation and capitalization indicators.
        int exclamations = countChar(text, '!');
        int questions = countChar(text, '?');

        if (questions > 0) {
            result.flags.add("Question detected");
        }

        if (exclamations >= 2) {
            result.flags.add("Strong punctuation emphasis");
        }

        int letters = 0;
        int uppercase = 0;

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) {
                    uppercase++;
                }
            }
        }

        if (letters >= 4 && uppercase >= 4
                && uppercase >= letters * 0.65) {
            result.flags.add("High capitalization intensity");
        }

        if (text.contains("😂") || text.contains("😊")
                || text.contains("❤️") || text.contains("😍")
                || text.contains("😁") || text.contains("🥰")) {
            sentiment += 2.0;
            addEmotionScore(scores, "Happiness", 1.0);
            result.flags.add("Positive emoji detected");
        }

        if (text.contains("😢") || text.contains("😭")
                || text.contains("😔") || text.contains("💔")) {
            sentiment -= 2.0;
            addEmotionScore(scores, "Sadness", 1.0);
            result.flags.add("Sadness-related emoji detected");
        }

        if (text.contains("😡") || text.contains("🤬")) {
            sentiment -= 2.0;
            addEmotionScore(scores, "Anger", 1.0);
            result.flags.add("Anger-related emoji detected");
        }

        // A basic contradiction indicator, not a sarcasm detector.
        if ((normalized.contains("i am happy")
                || normalized.contains("i'm happy"))
                && (normalized.contains("i am sad")
                || normalized.contains("i'm sad"))) {
            result.flags.add("Multiple or conflicting emotions detected");
        }

        if (normalized.contains("yeah right")
                || normalized.contains("as if")
                || normalized.contains("just great")) {
            result.flags.add(
                    "Possible sarcasm: context is required to verify"
            );
        }

        result.sentimentScore = clamp(sentiment, -100, 100);

        if (result.sentimentScore > 1.0) {
            result.sentiment = "Positive";
        } else if (result.sentimentScore < -1.0) {
            result.sentiment = "Negative";
        } else {
            result.sentiment = "Neutral";
        }

        double intensity = Math.min(100,
                Math.abs(result.sentimentScore) * 5.0);

        intensity += Math.min(15, exclamations * 3);

        if (uppercase >= 4 && letters > 0
                && uppercase >= letters * 0.65) {
            intensity += 10;
        }

        result.intensity = clamp(intensity, 0, 100);

        double maxEmotion = 0.0;
        double totalPositiveEmotion = 0.0;

        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            double value = Math.max(0.0, entry.getValue());

            result.emotionScores.put(entry.getKey

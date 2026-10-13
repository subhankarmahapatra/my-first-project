import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Emotion Insight Engine PRO 5.0
 * Standalone rule-based sentiment and emotion analysis.
 *
 * Compile: javac EmotionInsightEngine.java
 * Run:     java EmotionInsightEngine
 *
 * Files created in the current directory:
 *   emotion_log.csv
 *   emotion_report.txt
 */
public class EmotionInsightEngine {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final Path LOG_FILE = Paths.get("emotion_log.csv");
    private static final Path REPORT_FILE = Paths.get("emotion_report.txt");
    private static final String VERSION = "5.0 PRO";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, Double> POSITIVE = new LinkedHashMap<>();
    private static final Map<String, Double> NEGATIVE = new LinkedHashMap<>();
    private static final Map<String, Set<String>> EMOTIONS = new LinkedHashMap<>();
    private static final Map<String, Double> INTENSIFIERS = new HashMap<>();
    private static final Map<String, Double> DIMINISHERS = new HashMap<>();
    private static final Set<String> NEGATIONS = new HashSet<>(Arrays.asList(
            "not", "no", "never", "without", "isn't", "wasn't", "aren't",
            "don't", "didn't", "can't", "cannot", "couldn't", "won't",
            "wouldn't", "shouldn't", "hardly", "neither", "nor", "shan't",
            "isnt", "wasnt", "arent", "dont", "didnt", "cant", "couldnt",
            "wont", "wouldnt", "shouldnt"
    ));
    private static final Map<String, Integer> SESSION_EMOTIONS = new LinkedHashMap<>();
    private static int sessionAnalyses;
    private static double sessionSentimentTotal;
    private static double sessionIntensityTotal;

    private static final class AnalysisResult {
        String timestamp, text, sentiment, primaryEmotion, mood, suggestion;
        double sentimentScore, intensity, confidence, stressIndicator, anxietyIndicator;
        int wordCount, characterCount;
        Map<String, Double> emotionScores = new LinkedHashMap<>();
        List<String> matchedKeywords = new ArrayList<>();
        List<String> flags = new ArrayList<>();
    }

    public static void main(String[] args) {
        initializeDictionaries();
        initializeLogFile();
        for (String emotion : EMOTIONS.keySet()) SESSION_EMOTIONS.put(emotion, 0);
        printBanner();

        while (true) {
            printMenu();
            System.out.print("\nEnter a command or describe your feelings:\n> ");
            String line;
            try {
                line = INPUT.nextLine().trim();
            } catch (NoSuchElementException e) {
                break;
            }
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                showSessionSummary();
                System.out.println("\nSession ended. Take care!");
                break;
            }
            if (line.isEmpty()) {
                System.out.println("Please enter some text or a command.");
                continue;
            }
            switch (line.toLowerCase(Locale.ROOT)) {
                case "help": showHelp(); break;
                case "about": showAbout(); break;
                case "history": showHistory(); break;
                case "stats": showStatistics(); break;
                case "trend": showTrend(); break;
                case "search": searchHistory(); break;
                case "report": exportReport(); break;
                case "clear": clearHistory(); break;
                case "session": showSessionSummary(); break;
                default: analyzeAndSave(line);
            }
        }
        INPUT.close();
    }

    private static void initializeDictionaries() {
        addWords(POSITIVE, 4.0,
                "happy", "joy", "joyful", "excited", "amazing", "grateful",
                "wonderful", "great", "good", "delighted", "cheerful", "hopeful",
                "enjoy", "awesome", "fantastic", "smile", "laugh", "fun", "positive",
                "glad", "pleased", "love", "lovely", "success", "successful", "win",
                "winner", "best", "brilliant", "perfect", "peaceful", "confident",
                "proud", "relaxed", "thankful", "optimistic", "calm", "safe", "relief",
                "relieved", "beautiful", "inspired", "motivated", "comfortable",
                "content", "satisfied", "excellent", "kind", "blessed", "capable",
                "delicious", "helpful", "hope", "celebrate", "celebration", "friendly",
                "appreciate", "appreciated", "thank", "thanks", "peace", "serene",
                "secure", "trust", "honest", "loyal", "brave", "strong", "determined"
        );
        addWords(NEGATIVE, -4.0,
                "sad", "hurt", "pain", "angry", "lonely", "depressed", "bad",
                "terrible", "upset", "miserable", "stressed", "tired", "hopeless",
                "afraid", "worried", "anxious", "frustrated", "hate", "horrible",
                "awful", "failure", "fail", "failed", "problem", "cry", "crying",
                "broken", "disappointed", "disappointing", "exhausted", "nervous",
                "panic", "scared", "fear", "confused", "regret", "stress", "overwhelmed",
                "unhappy", "heartbroken", "helpless", "irritated", "furious", "annoyed",
                "disgusted", "guilty", "ashamed", "bored", "weak", "worthless",
                "difficult", "worst", "negative", "pressure", "burnout", "overworked",
                "unfair", "hate", "dislike", "unsafe", "insecure", "grief", "sorrow",
                "worry", "rage", "enraged", "terrified", "frightened", "nasty",
                "revolting", "repulsive", "sickening", "letdown", "unfulfilled"
        );

        addWords(INTENSIFIERS, 1.5,
                "very", "really", "extremely", "incredibly", "absolutely", "totally",
                "completely", "deeply", "highly", "super", "truly", "immensely",
                "so", "too", "utterly"
        );
        INTENSIFIERS.put("extremely", 2.0);
        INTENSIFIERS.put("incredibly", 1.8);
        INTENSIFIERS.put("absolutely", 1.7);
        INTENSIFIERS.put("utterly", 1.8);
        addWords(DIMINISHERS, 0.6,
                "slightly", "somewhat", "little", "mildly", "kinda", "kindof", "barely", "bit",
                "almost", "fairly", "rather"
        );
        DIMINISHERS.put("barely", 0.4);
        DIMINISHERS.put("slightly", 0.5);

        putEmotion("Happiness", "happy", "joy", "joyful", "excited", "delighted", "cheerful",
                "glad", "pleased", "awesome", "fantastic", "smile", "laugh", "fun", "amazing",
                "wonderful", "enjoy", "positive", "excellent", "celebrate", "celebration");
        putEmotion("Sadness", "sad", "cry", "crying", "lonely", "depressed", "hurt", "pain",
                "upset", "miserable", "hopeless", "heartbroken", "grief", "broken",
                "disappointed", "sorrow", "unhappy", "tears", "alone", "helpless");
        putEmotion("Anger", "angry", "mad", "hate", "frustrated", "furious", "annoyed",
                "rage", "irritated", "enraged", "revenge", "outraged", "irritating");
        putEmotion("Fear", "scared", "fear", "worried", "anxious", "nervous", "terrified",
                "afraid", "panic", "insecure", "danger", "threat", "frightened", "worry");
        putEmotion("Love", "love", "lovely", "caring", "special", "affection", "adore",
                "romantic", "cherish", "beloved", "kiss", "relationship", "care", "darling", "dear");
        putEmotion("Surprise", "surprised", "shocked", "unexpected", "astonished", "wow",
                "unbelievable", "suddenly", "surprise", "amazed");
        putEmotion("Disgust", "disgusted", "gross", "awful", "revolting", "nasty",
                "repulsive", "disgusting", "dirty", "dislike", "sickening");
        putEmotion("Trust", "trust", "trusted", "reliable", "honest", "loyal", "faithful",
                "dependable", "secure", "believe", "respect");
        putEmotion("Anticipation", "expect", "expecting", "anticipate", "future", "waiting",
                "ready", "upcoming", "tomorrow", "plan", "planning", "looking");
        putEmotion("Confusion", "confused", "confusing", "uncertain", "unsure", "doubt",
                "doubtful", "lost", "unclear", "puzzled", "wondering");
        putEmotion("Pride", "proud", "achievement", "achieved", "success", "successful",
                "winner", "winning", "accomplished", "accomplishment", "progress");
        putEmotion("Calmness", "calm", "peaceful", "relaxed", "relax", "quiet",
                "comfortable", "safe", "serene", "peace", "relief", "relieved");
        putEmotion("Stress", "stress", "stressed", "pressure", "overwhelmed", "busy",
                "exhausted", "deadline", "tension", "burnout", "overworked", "workload");
        putEmotion("Gratitude", "grateful", "thankful", "thanks", "appreciate",
                "appreciated", "blessed", "gratitude", "thank");
        putEmotion("Disappointment", "disappointed", "disappointing", "failure", "failed",
                "regret", "letdown", "unfulfilled");
        putEmotion("Confidence", "confident", "capable", "brave", "courage", "determined",
                "strong", "prepared", "believe");
        putEmotion("Curiosity", "curious", "question", "wonder", "explore", "learn",
                "discover", "interesting", "why", "how");
    }

    private static void addWords(Map<String, Double> dictionary, double weight, String... words) {
        for (String word : words) dictionary.put(word, weight);
    }

    private static void putEmotion(String name, String... words) {
        EMOTIONS.put(name, new LinkedHashSet<>(Arrays.asList(words)));
    }

    private static void printBanner() {
        System.out.println("\n======================================================");
        System.out.println("           EMOTION INSIGHT ENGINE PRO");
        System.out.println("                    VERSION " + VERSION);
        System.out.println("======================================================");
        System.out.println("Rule-based sentiment and emotion analysis");
        System.out.println(EMOTIONS.size() + " emotion categories | Sentiment | Mood | History");
        System.out.println("Note: this is an educational text analyzer, not a diagnosis.");
        System.out.println("======================================================");
    }

    private static void printMenu() {
        System.out.println("\n-------------------- COMMANDS --------------------");
        System.out.println("Type any text to analyze it.");
        System.out.println("history  - View saved analyses");
        System.out.println("stats    - View overall statistics");
        System.out.println("trend    - View recent emotion trends");
        System.out.println("search   - Search saved history");
        System.out.println("report   - Export a text report");
        System.out.println("clear    - Clear saved history");
        System.out.println("session  - Current session summary");
        System.out.println("help     - Help | about - About | exit - Quit");
        System.out.println("--------------------------------------------------");
    }

    private static AnalysisResult analyzeText(String text) {
        AnalysisResult r = new AnalysisResult();
        r.timestamp = LocalDateTime.now().format(DATE_FORMAT);
        r.text = text;
        r.characterCount = text.length();

        String cleaned = text.toLowerCase(Locale.ROOT);
        List<String> words = tokenize(cleaned);
        r.wordCount = words.size();

        Map<String, Double> scores = new LinkedHashMap<>();
        for (String emotion : EMOTIONS.keySet()) scores.put(emotion, 0.0);

        double sentiment = 0.0;
        int matchedSentimentWords = 0;
        int negationWindow = 0;
        double modifier = 1.0;
        Map<String, Integer> frequencies = new LinkedHashMap<>();

        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);

            if (NEGATIONS.contains(word)) {
                negationWindow = 3;
                modifier = 1.0;
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

            boolean negated = negationWindow > 0 || word.endsWith("n't");
            double valueModifier = modifier;
            Double pos = POSITIVE.get(word);
            Double neg = NEGATIVE.get(word);

            if (pos != null) {
                double value = pos * valueModifier;
                sentiment += negated ? -Math.abs(value) : value;
                matchedSentimentWords++;
                r.matchedKeywords.add(word + (negated ? " (negated)" : ""));
                if (!negated) addEmotionScore(scores, "Happiness", valueModifier);
            }
            if (neg != null) {
                double value = Math.abs(neg * valueModifier);
                sentiment += negated ? value * 0.5 : -value;
                matchedSentimentWords++;
                r.matchedKeywords.add(word + (negated ? " (negated)" : ""));
            }

            for (Map.Entry<String, Set<String>> entry : EMOTIONS.entrySet()) {
                if (entry.getValue().contains(word)) {
                    double score = valueModifier;
                    if (negated) score *= -0.5;
                    addEmotionScore(scores, entry.getKey(), score);
                }
            }
            if (negationWindow > 0) negationWindow--;
            modifier = 1.0;
        }

        String normalized = cleaned.replaceAll("[^\\p{L}\\p{N}' ]", " ")
                .replaceAll("\\s+", " ").trim();

        if (normalized.contains("thank you")) {
            addEmotionScore(scores, "Gratitude", 3.0);
            sentiment += 3.0;
        }
        if (normalized.contains("looking forward")) addEmotionScore(scores, "Anticipation", 2.0);
        if (normalized.contains("fed up")) {
            addEmotionScore(scores, "Anger", 2.0);
            addEmotionScore(scores, "Stress", 1.0);
            sentiment -= 2.0;
        }
        if (normalized.contains("give up")) {
            addEmotionScore(scores, "Sadness", 2.0);
            sentiment -= 2.0;
        }
        if (normalized.contains("on cloud nine")) {
            addEmotionScore(scores, "Happiness", 3.0);
            sentiment += 3.0;
        }
        if (normalized.contains("couldn't be happier") || normalized.contains("could not be happier")) {
            addEmotionScore(scores, "Happiness", 3.0);
            sentiment += 3.0;
        }

        for (Map.Entry<String, Integer> e : frequencies.entrySet()) {
            if (e.getValue() >= 3) r.flags.add("Repeated word: " + e.getKey() + " (" + e.getValue() + " times)");
        }

        int exclamations = countChar(text, '!');
        int questions = countChar(text, '?');
        if (questions > 0) r.flags.add("Question detected");
        if (exclamations >= 2) r.flags.add("Strong punctuation emphasis");

        int letters = 0, uppercase = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) uppercase++;
            }
        }
        boolean allCaps = letters >= 4 && uppercase >= 4 && uppercase >= letters * 0.65;
        if (allCaps) r.flags.add("High capitalization intensity");

        if (containsAny(text, "😂", "😊", "❤️", "😍", "😁", "🥰", "😄", "🙂")) {
            sentiment += 2.0;
            addEmotionScore(scores, "Happiness", 1.0);
            r.flags.add("Positive emoji detected");
        }
        if (containsAny(text, "😢", "😭", "😔", "💔", "🙁", "☹️")) {
            sentiment -= 2.0;
            addEmotionScore(scores, "Sadness", 1.0);
            r.flags.add("Sadness-related emoji detected");
        }
        if (containsAny(text, "😡", "🤬", "😠")) {
            sentiment -= 2.0;
            addEmotionScore(scores, "Anger", 1.0);
            r.flags.add("Anger-related emoji detected");
        }
        if (containsAny(text, "😨", "😰", "😱")) {
            addEmotionScore(scores, "Fear", 1.0);
            r.flags.add("Fear-related emoji detected");
        }

        if ((normalized.contains("i am happy") || normalized.contains("i'm happy"))
                && (normalized.contains("i am sad") || normalized.contains("i'm sad"))) {
            r.flags.add("Multiple or conflicting emotions detected");
        }
        if (containsAny(normalized, "yeah right", "as if", "just great", "oh wonderful")) {
            r.flags.add("Possible sarcasm: context is required to verify");
        }

        r.sentimentScore = clamp(sentiment, -100.0, 100.0);
        if (r.sentimentScore > 1.0) r.sentiment = "Positive";
        else if (r.sentimentScore < -1.0) r.sentiment = "Negative";
        else r.sentiment = "Neutral";

        double intensity = Math.min(100.0, Math.abs(r.sentimentScore) * 5.0);
        intensity += Math.min(15.0, exclamations * 3.0);
        if (allCaps) intensity += 10.0;
        r.intensity = clamp(intensity, 0.0, 100.0);

        double maxEmotion = 0.0;
        double totalPositiveEmotion = 0.0;
        for (Map.Entry<String, Double> e : scores.entrySet()) {
            double value = Math.max(0.0, e.getValue());
            r.emotionScores.put(e.getKey(), round2(value));
            totalPositiveEmotion += value;
            maxEmotion = Math.max(maxEmotion, value);
        }
        r.primaryEmotion = "Mixed / Undetected";
        if (maxEmotion > 0.0) {
            for (Map.Entry<String, Double> e : r.emotionScores.entrySet()) {
                if (e.getValue() == maxEmotion) {
                    r.primaryEmotion = e.getKey();
                    break;
                }
            }
        }

        r.confidence = words.isEmpty() ? 0.0 :
                clamp((matchedSentimentWords * 15.0 + maxEmotion * 5.0), 0.0, 95.0);
        r.stressIndicator = indicator(r.emotionScores, "Stress", "Anger", "Fear");
        r.anxietyIndicator = indicator(r.emotionScores, "Fear", "Stress", "Confusion");
        r.mood = determineMood(r.sentiment, r.primaryEmotion, r.intensity);
        r.suggestion = makeSuggestion(r.primaryEmotion, r.sentiment);
        r.flags.add("Rule-based estimate; context and sarcasm may affect accuracy.");
        return r;
    }

    private static List<String> tokenize(String text) {
        List<String> words = new ArrayList<>();
        Matcher m = Pattern.compile("[\\p{L}\\p{N}]+(?:'[\\p{L}]+)?").matcher(text);
        while (m.find()) words.add(m.group());
        return words;
    }

    private static void addEmotionScore(Map<String, Double> scores, String emotion, double value) {
        if (scores.containsKey(emotion)) scores.put(emotion, scores.get(emotion) + value);
    }

    private static double indicator(Map<String, Double> scores, String... names) {
        double total = 0.0;
        for (String name : names) total += scores.getOrDefault(name, 0.0);
        return clamp(total * 12.0, 0.0, 100.0);
    }

    private static String determineMood(String sentiment, String emotion, double intensity) {
        if ("Positive".equals(sentiment)) return intensity >= 60 ? "Very positive" : "Positive / hopeful";
        if ("Negative".equal

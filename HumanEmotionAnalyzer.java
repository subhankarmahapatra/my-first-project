import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

/**
 * ============================================================
 *              EMOTION INSIGHT ENGINE PRO 3.0
 * ============================================================
 *
 * Rule-based Emotion + Sentiment Analysis System
 *
 * Features:
 *
 *  1.  13+ emotion categories
 *  2.  Sentiment score
 *  3.  Positive / Negative / Neutral classification
 *  4.  Emotion confidence
 *  5.  Emotion intensity
 *  6.  Negation detection
 *  7.  Intensifiers
 *  8.  Diminishers
 *  9.  Emoji detection
 * 10.  Keyword detection
 * 11.  Top-3 emotion ranking
 * 12.  Multi-emotion detection
 * 13.  Mood classification
 * 14.  Personalized suggestions
 * 15.  Reflection questions
 * 16.  Stress indicator
 * 17.  Anxiety indicator
 * 18.  Question detection
 * 19.  Repeated-word detection
 * 20.  Capital-letter intensity detection
 * 21.  Punctuation intensity
 * 22.  Basic contradiction detection
 * 23.  Basic sarcasm indicator
 * 24.  Word statistics
 * 25.  CSV history
 * 26.  History viewer
 * 27.  Search history
 * 28.  Statistics
 * 29.  Emotion distribution
 * 30.  Average sentiment
 * 31.  Average intensity
 * 32.  Most frequent emotion
 * 33.  Trend analysis
 * 34.  Session statistics
 * 35.  Text report export
 * 36.  Clear history
 * 37.  Help
 * 38.  About
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

    private static final String VERSION = "3.0 PRO";

    // Session statistics
    private static int sessionAnalyses = 0;
    private static double sessionSentimentTotal = 0.0;
    private static double sessionIntensityTotal = 0.0;

    private static final Map<String, Integer> SESSION_EMOTIONS =
            new LinkedHashMap<>();

    // =========================================================
    // NEGATIONS
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
            "shouldn't",
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
                    Map.entry("truly", 1.4),
                    Map.entry("extremely", 2.0)
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
                    Map.entry("barely", 0.4),
                    Map.entry("a_little", 0.5)
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
                    Map.entry("optimistic", 6.0),
                    Map.entry("peace", 5.0),
                    Map.entry("relief", 5.0),
                    Map.entry("relieved", 5.0)
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
                    Map.entry("regret", -5.0),
                    Map.entry("stress", -6.0),
                    Map.entry("overwhelmed", -7.0)
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

            String input;

            try {
                input = INPUT.nextLine().trim();
            } catch (NoSuchElementException e) {
                break;
            }

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

            if (input.equalsIgnoreCase("session")) {
                showSessionSummary();
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
        System.out.println(" Emotions : 13+");
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
        System.out.println(" session       -> Session statistics");
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
                "fun:2",
                "positive:2",
                "glad:3",
                "pleased:3"
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
                "disappointing:3",
                "sorrow:4",
                "unhappy:3",
                "tears:3",
                "alone:2"
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
                "revenge:3",
                "outraged:4",
                "irritating:2"
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
                "threat:3",
                "frightened:4",
                "worry:3"
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
                "care:2",
                "darling:3",
                "dear:2"
        ));

        result.put("Surprise", weights(
                "surprised:3",
                "shocked:3",
                "unexpected:2",
                "astonished:4",
                "wow:2",
                "unbelievable:3",
                "suddenly:2",
                "unexpectedly:2",
                "surprise:3",
                "amazed:3"
        ));

        result.put("Disgust", weights(
                "disgusted:4",
                "gross:3",
                "awful:2",
                "revolting:4",
                "nasty:3",
                "repulsive:4",
                "disgusting:4",
                "dirty:2",
                "dislike:2",
                "sickening:4"
        ));

        result.put("Trust", weights(
                "trust:4",
                "trusted:4",
                "reliable:3",
                "honest:3",
                "loyal:3",
                "faithful:3",
                "dependable:3",
                "secure:2",
                "believe:2",
                "respect:2"
        ));

        result.put("Anticipation", weights(
                "expect:2",
                "expecting:3",
                "anticipate:3",
                "future:2",
                "waiting:2",
                "ready:2",
                "soon:2",
                "upcoming:2",
                "tomorrow:2",
                "plan:2",
                "planning:2"
        ));

        result.put("Confusion", weights(
                "confused:4",
                "confusing:4",
                "uncertain:3",
                "unsure:3",
                "doubt:3",
                "doubtful:3",
                "lost:3",
                "unclear:3",
                "puzzled:3",
                "wondering:2"
        ));

        result.put("Pride", weights(
                "proud:4",
                "achievement:4",
                "achieved:4",
                "success:3",
                "successful:3",
                "winner:4",
                "winning:4",
                "accomplished:4",
                "accomplishment:4",
                "progress:3"
        ));

        result.put("Calmness", weights(
                "calm:4",
                "peaceful:4",
                "relaxed:3",
                "relax:3",
                "quiet:2",
                "comfortable:3",
                "safe:3",
                "serene:4",
                "peace:4",
                "relief:3",
                "relieved:3"
        ));

        result.put("Stress", weights(
                "stress:4",
                "stressed:4",
                "pressure:3",
                "overwhelmed:4",
                "busy:2",
                "exhausted:3",
                "deadline:3",
                "tension:3",
                "burnout:4",
                "overworked:4",
                "workload:3"
        ));

        result.put("Gratitude", weights(
                "grateful:4",
                "thankful:4",
                "thanks:3",
                "appreciate:3",
                "appreciated:3",
                "blessed:3",
                "gratitude:4"
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
                            parts[0].toLowerCase(Locale.ROOT),
                            Double.parseDouble(parts[1])
      

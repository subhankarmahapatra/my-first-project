import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

/**
 * Emotion Insight Engine PRO 7.0 (7000-Series Edition)
 * Standalone, rule-based sentiment, cognitive pattern & emotion analyzer. No external libraries.
 *
 * Compile: javac EmotionInsightEngine.java
 * Run:     java EmotionInsightEngine
 * Files:   emotion_log.csv, emotion_report.txt, emotion_dashboard.html, custom_lexicon.csv
 * Note:    Educational heuristic estimates only; not a clinical or diagnostic tool.
 */
public class EmotionInsightEngine {
    private static final Scanner IN = new Scanner(System.in);
    private static final Path LOG = Paths.get("emotion_log.csv");
    private static final Path REPORT = Paths.get("emotion_report.txt");
    private static final Path DASHBOARD = Paths.get("emotion_dashboard.html");
    private static final Path CUSTOM_LEX = Paths.get("custom_lexicon.csv");
    private static final String VERSION = "7.0 (7000-SERIES ULTIMATE)";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static final LinkedHashMap<String, Double> SENTIMENT = new LinkedHashMap<>();
    private static final LinkedHashMap<String, LinkedHashSet<String>> EMOTIONS = new LinkedHashMap<>();
    private static final Map<String, Double> BOOST = new HashMap<>(), DAMPEN = new HashMap<>();
    private static final Map<String, String[]> COGNITIVE_PATTERNS = new LinkedHashMap<>();

    private static final Set<String> NEGATIONS = new HashSet<>(Arrays.asList(
        "not","no","never","without","isn't","wasn't","aren't","don't","didn't","can't","cannot",
        "couldn't","won't","wouldn't","shouldn't","isnt","wasnt","arent","dont","didnt","cant",
        "couldnt","wont","wouldnt","shouldnt","hardly","neither","nor","n't","nahi","na","nuhe","nuhen"));

    private static final Set<String> FIRST_PERSON = new HashSet<>(Arrays.asList("i","me","my","mine","myself","mu","mote","mora"));
    private static final Set<String> COLLECTIVE_PERSON = new HashSet<>(Arrays.asList("we","us","our","ours","ourselves","ame","amara"));
    private static final Set<String> SECOND_PERSON = new HashSet<>(Arrays.asList("you","your","yours","yourself","tu","tume","apana","tote"));

    private static final Pattern TOKEN = Pattern.compile("[\\p{L}\\p{N}]+(?:['’][\\p{L}]+)?");
    private static int sessionCount;
    private static double sessionTotal;

    private static final class Result {
        String timestamp, text, sentiment, primary, mood, suggestion, toneStyle, pronounFocus, arcSummary;
        double score, intensity, confidence, stress, anxiety;
        int words, chars;
        LinkedHashMap<String, Double> emotions = new LinkedHashMap<>();
        List<String> keywords = new ArrayList<>(), flags = new ArrayList<>();
        List<String> dyads = new ArrayList<>(), cognitiveDistortions = new ArrayList<>();
        List<Double> clauseScores = new ArrayList<>();
    }

    public static void main(String[] args) {
        setup();
        loadCustomLexicon();
        ensureLog();
        banner();
        while (true) {
            menu();
            System.out.print("\nEnter text or command > ");
            String line;
            try {
                line = IN.nextLine().trim();
            } catch (NoSuchElementException e) {
                break;
            }
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                sessionSummary();
                System.out.println("Goodbye! Stay mindful and take care.");
                break;
            }
            if (line.isEmpty()) {
                System.out.println("Please enter text or a valid command.");
                continue;
            }
            switch (line.toLowerCase(Locale.ROOT)) {
                case "help": help(); break;
                case "about": about(); break;
                case "history": history(); break;
                case "stats": stats(); break;
                case "trend": trend(); break;
                case "search": search(); break;
                case "compare": compareTexts(); break;
                case "train": trainWord(); break;
                case "batch": batchAnalyze(); break;
                case "dashboard": exportDashboard(); break;
                case "breathe": breathingExercise(); break;
                case "report": report(); break;
                case "clear": clearHistory(); break;
                case "session": sessionSummary(); break;
                default: analyzeAndSave(line);
            }
        }
    }

    private static void setup() {
        // Core English + Built-in Odia & Hinglish Positive Words
        addSent(3, "happy joy joyful excited amazing grateful wonderful great good delighted cheerful hopeful enjoy awesome fantastic smile laugh fun positive glad pleased love lovely success successful win winner best brilliant perfect peaceful confident proud relaxed thankful optimistic calm safe relief relieved beautiful inspired motivated comfortable content satisfied excellent kind blessed capable delicious helpful hope celebrate celebration friendly appreciate appreciated thank thanks peace serene secure trust honest loyal brave strong determined thrilled khusi bhal bhala sundar mast badhiya bindaas shanti ananda dhanyabad jaya");

        // Core English + Built-in Odia & Hinglish Negative Words
        addSent(-3, "sad hurt pain angry lonely depressed bad terrible upset miserable stressed tired hopeless afraid worried anxious frustrated hate horrible awful failure fail failed problem cry crying broken disappointed disappointing exhausted nervous panic scared fear confused regret stress overwhelmed unhappy heartbroken helpless irritated furious annoyed disgusted guilty ashamed bored weak worthless difficult worst negative pressure burnout overworked unfair dislike unsafe insecure grief sorrow worry rage enraged terrified frightened nasty revolting repulsive sickening letdown unfulfilled dukha kasta raga bhaya bekar faltu kharap chinta dar tension");

        putEmotion("Happiness", "happy joy joyful excited delighted cheerful glad pleased awesome fantastic smile laugh fun amazing wonderful enjoy positive excellent celebrate celebration thrilled khusi ananda mast badhiya bindaas");
        putEmotion("Sadness", "sad cry crying lonely depressed hurt pain upset miserable hopeless heartbroken grief broken disappointed sorrow unhappy tears alone helpless dukha kasta kandiba");
        putEmotion("Anger", "angry mad hate frustrated furious annoyed rage irritated enraged revenge outraged irritating raga chidi");
        putEmotion("Fear", "scared fear worried anxious nervous terrified afraid panic danger threat frightened worry bhaya dar chinta");
        putEmotion("Love", "love lovely caring special affection adore romantic cherish beloved kiss relationship care darling dear bhala-paiba prema");
        putEmotion("Surprise", "surprised shocked unexpected astonished wow unbelievable suddenly surprise amazed aachambita");
        putEmotion("Disgust", "disgusted gross awful revolting nasty repulsive disgusting dirty dislike sickening bekar faltu ghrina");
        putEmotion("Trust", "trust trusted reliable honest loyal faithful dependable secure believe respect biswasa bharasa");
        putEmotion("Anticipation", "expect expecting anticipate future waiting ready upcoming tomorrow plan planning forward aasha");
        putEmotion("Confusion", "confused confusing uncertain unsure doubt doubtful lost unclear puzzled wondering dwidha");
        putEmotion("Pride", "proud achievement achieved success successful winner winning accomplished accomplishment progress garba jaya");
        putEmotion("Calmness", "calm peaceful relaxed relax quiet comfortable safe serene peace relief relieved shanti niramaya");
        putEmotion("Stress", "stress stressed pressure overwhelmed busy exhausted deadline tension burnout overworked workload chap");
        putEmotion("Gratitude", "grateful thankful thanks appreciate appreciated blessed gratitude thank dhanyabad kritagya");
        putEmotion("Disappointment", "disappointed disappointing failure failed regret letdown unfulfilled nirasha");
        putEmotion("Confidence", "confident capable brave courage determined strong prepared believe sahasa");
        putEmotion("Curiosity", "curious question wonder explore learn discover interesting why how jigyasa");
        putEmotion("Jealousy", "jealous envy envious possessive irsha");
        putEmotion("Shame", "ashamed embarrassed humiliation humiliated shame lajja");
        putEmotion("Boredom", "bored boring dull uninterested tedious birakta");
        putEmotion("Hope", "hope hopeful optimistic future possible possibility aasha");
        putEmotion("Loneliness", "lonely alone isolated abandoned disconnected eka ekaki");

        String[] boosts = {"very","really","extremely","incredibly","absolutely","totally","completely","deeply","highly","super","truly","immensely","so","too","utterly","bahut","bhari","pura"};
        for (String w : boosts) BOOST.put(w, 1.5);
        BOOST.put("extremely", 2.0); BOOST.put("incredibly", 1.8); BOOST.put("absolutely", 1.7); BOOST.put("utterly", 1.8); BOOST.put("bahut", 1.7);

        for (String w : Arrays.asList("slightly","somewhat","little","mildly","kinda","kindof","barely","bit","almost","fairly","rather","tike","alpa")) {
            DAMPEN.put(w, 0.6);
        }
        DAMPEN.put("barely", 0.4); DAMPEN.put("slightly", 0.5); DAMPEN.put("tike", 0.5);

        // CBT Cognitive Distortion Triggers
        COGNITIVE_PATTERNS.put("All-or-Nothing Thinking", new String[]{"always", "never", "everyone", "nobody", "nothing", "everything", "completely ruined", "total failure"});
        COGNITIVE_PATTERNS.put("Catastrophizing", new String[]{"worst ever", "disaster", "end of the world", "unbearable", "ruined my life", "can't survive", "terrible fate"});
        COGNITIVE_PATTERNS.put("Should / Must Statements", new String[]{"should have", "must always", "ought to", "have to be", "supposed to"});
        COGNITIVE_PATTERNS.put("Self-Labeling / Blame", new String[]{"i am a failure", "i'm a loser", "i am worthless", "all my fault", "i am stupid", "i'm broken"});
        COGNITIVE_PATTERNS.put("Fortune Telling (Negative Prediction)", new String[]{"will never work", "going to fail", "no point trying", "nothing will change", "bound to fail"});
    }

    private static void addSent(double weight, String words) {
        for (String w : words.split("\\s+")) if (!w.isEmpty()) SENTIMENT.put(w, weight);
    }

    private static void putEmotion(String name, String words) {
        EMOTIONS.put(name, new LinkedHashSet<>(Arrays.asList(words.split("\\s+"))));
    }

    private static void banner() {
        System.out.println("\n======================================================================");
        System.out.println("        EMOTION INSIGHT ENGINE PRO " + VERSION);
        System.out.println("======================================================================");
        System.out.println("Features: " + EMOTIONS.size() + " Emotions | Plutchik Dyads | CBT Distortions | Odia/Hinglish");
        System.out.println("          Emotional Arc | Tone Profiler | HTML Dashboard | Custom Trainer");
        System.out.println("Note:     Rule-based educational tool; results can miss context/sarcasm.");
    }

    private static void menu() {
        System.out.println("\n[COMMANDS] history | stats | trend | search | compare | train | batch | dashboard | breathe | report | clear | session | help | about | exit");
    }

    private static void help() {
        System.out.println("\n--- HELP & COMMAND GUIDE ---");
        System.out.println("Type any sentence (English, Roman Odia, or Hinglish) to analyze it immediately.");
        System.out.println("  compare   - Compare two sentences side-by-side (e.g., Draft 1 vs Draft 2)");
        System.out.println("  train     - Teach the engine a new custom word with sentiment & emotion");
        System.out.println("  dashboard - Generate an interactive HTML visual report (emotion_dashboard.html)");
        System.out.println("  batch     - Analyze all lines inside a text file");
        System.out.println("  breathe   - Start a guided 4-7-8 calming breathing exercise in the console");
        System.out.println("  history   - View recent saved analyses");
        System.out.println("  stats     - View overall statistics, averages, and emotion distribution");
        System.out.println("  trend     - View ASCII timeline trend of recent entries");
        System.out.println("  search    - Search saved history by keyword");
        System.out.println("  report    - Export raw CSV log to emotion_report.txt");
        System.out.println("  clear     - Delete saved history and reset session");
        System.out.println("  session   - View current session summary");
    }

    private static void about() {
        System.out.println("\nEmotion Insight Engine " + VERSION);
        System.out.println("Uses weighted lexicons, negation windows, intensity boosters/dampeners,");
        System.out.println("Plutchik emotion dyads, CBT cognitive distortion heuristics, clause-level");
        System.out.println("emotional trajectory tracking, and pronoun-based tone profiling.");
    }

    private static Result analyze(String text) {
        Result r = new Result();
        r.timestamp = LocalDateTime.now().format(TS);
        r.text = text;
        r.chars = text.length();

        String lower = text.toLowerCase(Locale.ROOT).replace('’', '\'');
        List<String> words = tokens(lower);
        r.words = words.size();
        for (String e : EMOTIONS.keySet()) r.emotions.put(e, 0.0);

        Map<String, Integer> freq = new LinkedHashMap<>();
        double score = 0;
        int matched = 0, negWindow = 0;
        double modifier = 1.0;
        int firstP = 0, collP = 0, secondP = 0, longWords = 0;

        for (int i = 0; i < words.size(); i++) {
            String w = words.get(i);
            freq.put(w, freq.getOrDefault(w, 0) + 1);
            if (w.length() >= 7) longWords++;
            if (FIRST_PERSON.contains(w)) firstP++;
            if (COLLECTIVE_PERSON.contains(w)) collP++;
            if (SECOND_PERSON.contains(w)) secondP++;

            if (NEGATIONS.contains(w) || w.endsWith("n't")) {
                negWindow = 3;
                modifier = 1.0;
                continue;
            }
            if (BOOST.containsKey(w)) {
                modifier = BOOST.get(w);
                continue;
            }
            if (DAMPEN.containsKey(w)) {
                modifier = DAMPEN.get(w);
                continue;
            }

            boolean negated = negWindow > 0;
            Double s = SENTIMENT.get(w);
            if (s != null) {
                score += negated ? -s * modifier : s * modifier;
                matched++;
                r.keywords.add(w + (negated ? " [negated]" : "") + (modifier != 1.0 ? String.format(" [x%.1f]", modifier) : ""));
            }
            for (Map.Entry<String, LinkedHashSet<String>> e : EMOTIONS.entrySet()) {
                if (e.getValue().contains(w)) {
                    double add = modifier * (negated ? -0.65 : 1.0);
                    r.emotions.put(e.getKey(), r.emotions.get(e.getKey()) + add);
                }
            }
            if (negWindow > 0) negWindow--;
            modifier = 1.0;
        }

        String norm = lower.replaceAll("[^\\p{L}\\p{N}' ]", " ").replaceAll("\\s+", " ").trim();
        if (norm.contains("thank you") || norm.contains("bahut dhanyabad")) { bump(r, "Gratitude", 2.5); score += 2.5; }
        if (norm.contains("looking forward")) { bump(r, "Anticipation", 2.0); score += 1.5; }
        if (norm.contains("fed up")) { bump(r, "Anger", 2.0); bump(r, "Stress", 1.0); score -= 2.5; }
        if (norm.contains("give up")) { bump(r, "Sadness", 2.0); score -= 2.5; }
        if (norm.contains("on cloud nine")) { bump(r, "Happiness", 3.0); score += 3.5; }
        if (norm.contains("couldn't be happier") || norm.contains("could not be happier")) { bump(r, "Happiness", 3.5); score += 4.0; }
        if (norm.contains("bhal laguchi") || norm.contains("bhala laguchi")) { bump(r, "Happiness", 2.5); score += 3.0; }
        if (norm.contains("bhal laguni") || norm.contains("bhala laguni")) { bump(r, "Sadness", 2.5); score -= 3.0; }

        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            if (e.getValue() >= 3) r.flags.add("Repeated word: '" + e.getKey() + "' x" + e.getValue());
        }

        int bangs = count(text, '!'), questions = count(text, '?');
        if (questions > 0) r.flags.add("Question punctuation detected (" + questions + ")");
        if (bangs >= 2) r.flags.add("Strong exclamation emphasis (" + bangs + ")");

        int letters = 0, caps = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
                if (Character.isUpperCase(c)) caps++;
            }
        }
        boolean allCaps = letters >= 4 && caps >= 4 && caps >= letters * 0.65;
        if (allCaps) r.flags.add("High capitalization intensity");

        // Emoji analysis
        if (any(text, "😂","😊","❤️","😍","😁","🥰","😄","🙂","😃","🎉","✨")) {
            score += 2; bump(r, "Happiness", 1.5); r.flags.add("Positive emoji clue");
        }
        if (any(text, "😢","😭","😔","💔","🙁","☹️","🥺")) {
            score -= 2; bump(r, "Sadness", 1.5); r.flags.add("Sadness-related emoji clue");
        }
        if (any(text, "😡","🤬","😠","💢")) {
            score -= 2; bump(r, "Anger", 1.5); r.flags.add("Anger-related emoji clue");
        }
        if (any(text, "😨","😰","😱","😟")) {
            score -= 1.5; bump(r, "Fear", 1.5); r.flags.add("Fear-related emoji clue");
        }

        if (any(norm, "yeah right", "as if", "just great", "oh wonderful", "what a joke")) {
            r.flags.add("Possible sarcasm: literal interpretation uncertain");
        }
        if (norm.contains(" but ") || norm.contains(" however ") || norm.contains(" kintu ") || norm.contains(" hele ")) {
            r.flags.add("Contrast connector detected; emotion shifts mid-text");
        }

        // Normalize emotion scores
        double max = 0;
        for (String e : r.emotions.keySet()) {
            double v = Math.max(0, r.emotions.get(e));
            r.emotions.put(e, round(v));
            max = Math.max(max, v);
        }

        r.score = clamp(score * 3.2, -100, 100);
        r.sentiment = r.score > 2 ? "Positive" : r.score < -2 ? "Negative" : "Neutral";
        r.intensity = clamp(Math.abs(r.score) * 0.85 + Math.min(20, bangs * 4) + (allCaps ? 12 : 0), 0, 100);

        r.primary = "Mixed / Undetected";
        if (max > 0) {
            for (Map.Entry<String, Double> e : r.emotions.entrySet()) {
                if (e.getValue() == max) {
                    r.primary = e.getKey();
                    break;
                }
            }
        }

        r.confidence = words.isEmpty() ? 0 : clamp((matched * 14.0 + max * 5.0), 0, 98);
        r.stress = indicator(r, "Stress", "Anger", "Fear");
        r.anxiety = indicator(r, "Fear", "Stress", "Confusion");
        r.mood = mood(r.sentiment, r.primary, r.intensity);

        // NEW FEATURE 1: Plutchik Emotion Dyads
        detectDyads(r);

        // NEW FEATURE 2: Cognitive Distortion Detector
        detectCognitiveDistortions(norm, r);

        // NEW FEATURE 3: Emotional Arc / Trajectory
        computeEmotionalArc(text, r);

        // NEW FEATURE 4: Tone & Pronoun Profiler
        r.pronounFocus = determinePronounF
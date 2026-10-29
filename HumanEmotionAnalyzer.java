import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;



public class EmotionInsightEngine {
    private static final Scanner IN = new Scanner(System.in);
    private static final Path LOG = Paths.get("emotion_log.csv");
    private static final Path CUSTOM_LEXICON = Paths.get("custom_lexicon.csv");
    private static final Path DASHBOARD = Paths.get("emotion_dashboard.html");
    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, Double> SENTIMENT = new LinkedHashMap<>();
    private static final Map<String, String> EMOTION_BY_WORD = new HashMap<>();
    private static final Set<String> NEGATIONS = new HashSet<>(Arrays.asList(
            "not", "no", "never", "without", "isn't", "wasn't", "aren't",
            "didn't", "can't", "cannot", "couldn't", "won't", "wouldn't", "shouldn't",
            "isnt", "wasnt", "arent", "dont", "didnt", "cant", "couldnt", "wont",
            "wouldnt", "shouldnt"));
    private static final Map<String, Double> BOOSTERS = new HashMap<>();
    private static final Map<String, Double> DAMPENERS = new HashMap<>();
    private static final Pattern TOKEN = Pattern.compile("[\\p{L}\\p{N}]+(?:['’][\\p{L}]+)?");

    private static class Result {
        String timestamp;
        String text;
        String sentiment;
        String primaryEmotion;
        List<String> matchedWords = new ArrayList<>();
        Map<String, Double> emotions = new LinkedHashMap<>();
    }

    public static void main(String[] args) {
        setupLexicon();
        loadCustomLexicon();
        ensureLog();

        System.out.println("\n==============================================");
        System.out.println("       EMOTION INSIGHT ENGINE 8.0");
        System.out.println("==============================================");
        System.out.println("Rule-based text analysis | English, Roman Odia and Hinglish");
        System.out.println("Type a sentence to analyze it, or enter a command.");
        printHelp();

        while (true) {
            System.out.print("\n> ");
            String input;
            try {
                input = IN.nextLine().trim();
            } catch (NoSuchElementException ex) {
                break;
            }
            if (input.isEmpty()) continue;

            switch (input.toLowerCase(Locale.ROOT)) {
                case "exit":
                case "quit":
                    System.out.println("Goodbye!");
                    return;
                case "help": printHelp(); break;
                case "history": showHistory(); break;
                case "today": showToday(); break;
                case "stats": showStats(); break;
                case "search": searchHistory(); break;
                case "compare": compareTexts(); break;
                case "train": trainWord(); break;
                case "batch": batchAnalyze(); break;
                case "export": exportDashboard(); break;
                case "clear": clearHistory(); break;
                default: analyzeAndSave(input);
            }
        }
    }

    private static void setupLexicon() {
        addWords(3, "happy joy joyful excited amazing grateful wonderful great good delighted cheerful hopeful enjoy awesome fantastic smile laugh fun positive glad pleased love lovely success successful win winner best brilliant perfect peaceful confident proud relaxed thankful optimistic calm safe relief relieved beautiful inspired motivated comfortable content satisfied excellent kind blessed capable helpful hope celebrate friendly appreciate thanks peace serene secure trust honest loyal brave strong determined thrilled khusi bhala bhal sundar mast badhiya shanti ananda dhanyabad");
        addWords(-3, "sad hurt pain angry lonely depressed bad terrible upset miserable stressed tired hopeless afraid worried anxious frustrated hate horrible awful failure fail failed problem cry crying broken disappointed exhausted nervous panic scared fear confused regret stress overwhelmed unhappy heartbroken helpless irritated furious annoyed guilty ashamed bored weak difficult worst pressure burnout overworked unfair dislike unsafe insecure grief sorrow worry rage terrified dukha kasta raga bhaya bekar kharap chinta dar tension");
        addEmotion("Happiness", "happy joy joyful excited amazing wonderful delighted cheerful glad pleased awesome fantastic smile laugh fun positive celebrate thrilled khusi ananda mast");
        addEmotion("Sadness", "sad cry crying lonely depressed hurt pain upset miserable hopeless heartbroken grief broken disappointed sorrow unhappy helpless dukha kasta");
        addEmotion("Anger", "angry hate frustrated furious annoyed rage irritated raga");
        addEmotion("Fear", "scared fear worried anxious nervous terrified afraid panic bhaya dar chinta");
        addEmotion("Love", "love lovely caring special affection adore romantic cherish beloved relationship care prema");
        addEmotion("Trust", "trust trusted reliable honest loyal faithful dependable believe respect biswasa");
        addEmotion("Stress", "stress stressed pressure overwhelmed busy exhausted deadline tension burnout workload");
        addEmotion("Gratitude", "grateful thankful thanks appreciate gratitude dhanyabad");
        addEmotion("Confidence", "confident capable brave courage determined strong prepared sahasa");
        addEmotion("Hope", "hope hopeful optimistic future possible possibility aasha");
        addEmotion("Confusion", "confused confusing uncertain unsure doubt doubtful lost unclear puzzled");
        addEmotion("Curiosity", "curious question wonder explore learn discover interesting why how jigyasa");
        addEmotion("Disappointment", "disappointed failure regret letdown unfulfilled nirasha");
        addEmotion("Calmness", "calm peaceful relaxed quiet comfortable safe serene peace relief shanti");
        addEmotion("Surprise", "surprised shocked unexpected astonished wow unbelievable surprise");
        addEmotion("Boredom", "bored boring dull uninterested tedious birakta");
        addEmotion("Pride", "proud achievement achieved winner winning accomplished progress garba");
        addEmotion("Jealousy", "jealous envy envious irsha");
        addEmotion("Loneliness", "lonely alone isolated abandoned disconnected ekaki");
        addEmotion("Disgust", "disgusted gross revolting nasty repulsive disgusting dirty dislike ghrina");

        for (String w : Arrays.asList("very", "really", "extremely", "incredibly", "absolutely", "deeply", "so", "too", "bahut")) {
            BOOSTERS.put(w, 1.5);
        }
        BOOSTERS.put("extremely", 2.0);
        BOOSTERS.put("incredibly", 1.8);
        BOOSTERS.put("bahut", 1.7);
        for (String w : Arrays.asList("slightly", "somewhat", "little", "mildly", "barely", "bit", "tike", "alpa")) {
            DAMPENERS.put(w, 0.6);
        }
        DAMPENERS.put("barely", 0.4);
    }

    private static void addWords(double weight, String words) {
        for (String word : words.split("\\s+")) SENTIMENT.put(word, weight);
    }

    private static void addEmotion(String emotion, String words) {
        for (String word : words.split("\\s+")) EMOTION_BY_WORD.put(word, emotion);
    }

    private static void printHelp() {
        System.out.println("\nCommands:");
        System.out.println("  history  - show recent saved analyses");
        System.out.println("  today    - show analyses saved today");
        System.out.println("  stats    - show sentiment totals and average score");
        System.out.println("  search   - find saved entries by keyword");
        System.out.println("  compare  - compare two sentences");
        System.out.println("  train    - add a custom word to the lexicon");
        System.out.println("  batch    - analyze each non-empty line in a text file");
        System.out.println("  export   - create a simple HTML dashboard");
        System.out.println("  clear    - clear saved history after confirmation");
        System.out.println("  help     - show this guide");
        System.out.println("  exit     - quit");
        System.out.println("Note: results are heuristic estimates and can miss context, sarcasm, or mixed meanings.");
    }

    private static Result analyze(String text) {
        Result r = new Result();
        r.timestamp = LocalDateTime.now().format(TIME);
        r.text = text;
        List<String> words = tokenize(text.toLowerCase(Locale.ROOT).replace('’', '\''));
        r.words = words.size();
        Map<String, Double> totals = new LinkedHashMap<>();
        for (String emotion : new LinkedHashSet<>(EMOTION_BY_WORD.values())) totals.put(emotion, 0.0);

        double score = 0;
        int negationWindow = 0;
        double modifier = 1.0;
        for (String word : words) {
            if (NEGATIONS.contains(word) || word.endsWith("n't")) {
                negationWindow = 3;
                modifier = 1.0;
                continue;
            }
            if (BOOSTERS.containsKey(word)) {
                modifier = BOOSTERS.get(word);
                continue;
            }
            if (DAMPENERS.containsKey(word)) {
                modifier = DAMPENERS.get(word);
                continue;
            }

            boolean negated = negationWindow > 0;
            Double weight = SENTIMENT.get(word);
            if (weight != null) {
                score += (negated ? -weight : weight) * modifier;
                r.matchedWords.add(word + (negated ? " [negated]" : ""));
            }
            String emotion = EMOTION_BY_WORD.get(word);
            if (emotion != null) {
                double old = totals.getOrDefault(emotion, 0.0);
                totals.put(emotion, old + (negated ? -0.65 : 1.0) * modifier);
            }
            if (negationWindow > 0) negationWindow--;
            modifier = 1.0;
        }

        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}' ]", " ").replaceAll("\\s+", " ").trim();
        if (normalized.contains("thank you")) { totals.merge("Gratitude", 2.0, Double::sum); score += 2.0; }
        if (normalized.contains("looking forward")) { totals.merge("Hope", 1.0, Double::sum); score += 1.0; }
        if (normalized.contains("on cloud nine")) { totals.merge("Happiness", 2.0, Double::sum); score += 3.0; }

        r.score = Math.max(-100, Math.min(100, score * 4.0));
        r.sentiment = r.score > 2 ? "Positive" : r.score < -2 ? "Negative" : "Neutral";
        r.emotions = totals.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
        r.primaryEmotion = "Undetected";
        for (Map.Entry<String, Double> e : r.emotions.entrySet()) {
            if (e.getValue() > 0) { r.primaryEmotion = e.getKey(); break; }
        }
        return r;
    }

    private static List<String> tokenize(String text) {
        List<String> words = new ArrayList<>();
        Matcher m = TOKEN.matcher(text);
        while (m.find()) words.add(m.group().replace('’', '\''));
        return words;
    }

    private static void analyzeAndSave(String text) {
        Result r = analyze(text);
        printResult(r);
        append(r);
    }

    private static void printResult(Result r) {
        System.out.println("\n--- Analysis ---");
        System.out.println("Time:            " + r.timestamp);
        System.out.println("Sentiment:       " + r.sentiment + " (score " + round(r.score) + " / -100 to +100)");
        System.out.println("Primary emotion: " + r.primaryEmotion);
        System.out.println("Word count:      " + r.words);
        System.out.println("Matched words:   " + (r.matchedWords.isEmpty() ? "none" : String.join(", ", r.matchedWords)));
        System.out.println("Top emotions:");
        int shown = 0;
        for (Map.Entry<String, Double> e : r.emotions.entrySet()) {
            if (e.getValue() <= 0) continue;
            System.out.printf("  %-16s %.1f%n", e.getKey(), e.getValue());
            if (++shown == 5) break;
        }
        System.out.println("Insight: " + insight(r));
        System.out.println("Reminder: this is a text-based estimate, not a diagnosis.");
    }

    private static String insight(Result r) {
        if (r.sentiment.equals("Positive")) return "Your text contains more positive than negative sentiment clues.";
        if (r.sentiment.equals("Negative")) return "Your text contains more negative than positive sentiment clues. Consider the context before drawing conclusions.";
        return "The text appears neutral or has too few clear sentiment clues for a strong result.";
    }

    private static void append(Result r) {
        try {
            boolean newFile = !Files.exists(LOG) || Files.size(LOG) == 0;
            try (BufferedWriter out = Files.newBufferedWriter(LOG, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                if (newFile) out.write("timestamp,sentiment,score,primary_emotion,word_count,text\n");
                out.write(csv(r.timestamp) + "," + csv(r.sentiment) + "," + round(r.score) + ","
                        + csv(r.primaryEmotion) + "," + r.words + "," + csv(r.text) + "\n");
            }
        } catch (IOException ex) {
            System.out.println("Could not save history: " + ex.getMessage());
        }
    }

    private static String csv(String value) {
        return "\"" + value.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
    }

    private static List<String[]> readHistory() {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(LOG)) return rows;
        try {
            List<String> lines = Files.readAllLines(LOG, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                List<String> fields = parseCsvLine(lines.get(i));
                if (fields.size() >= 6) rows.add(fields.toArray(new String[0]));
            }
        } catch (IOException ex) {
            System.out.println("Could not read history: " + ex.getMessage());
        }
        return rows;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"'); i++;
                } else quoted = !quoted;
            } else if (c == ',' && !quoted) {
                fields.add(field.toString()); field.setLength(0);
            } else field.append(c);
        }
        fields.add(field.toString());
        return fields;
    }

    private static void showHistory() {
        List<String[]> rows = readHistory();
        if (rows.isEmpty()) { System.out.println("No saved analyses yet."); return; }
        int start = Math.max(0, rows.size() - 10);
        System.out.println("\nRecent analyses (latest 10):");
        for (int i = start; i < rows.size(); i++) {
            String[] r = rows.get(i);
            System.out.printf("%s | %-8s | %6s | %-14s | %s%n",
                    r[0], r[1], r[2], r[3], shorten(r[5], 60));
        }
    }

    private static void showToday() {
        String today = LocalDate.now().toString();
        List<String[]> rows = readHistory();
        int found = 0;

        System.out.println("\\nAnalyses saved today (" + today + "):");
        for (String[] row : rows) {
            if (row.length >= 6 && row[0].startsWith(today)) {
                System.out.printf("%s | %-8s | %6s | %-14s | %s%n",
                        row[0], row[1], row[2], row[3], shorten(row[5], 60));
                found++;
            }
        }

        if (found == 0) {
            System.out.println("No analyses saved today.");
        } else {
            System.out.println("Today's total: " + found);
        }
    }

    private static void showStats() {
        List<String[]> rows = readHistory();
        if (rows.isEmpty()) { System.out.println("No saved analyses yet."); return; }
        int positive = 0, negative = 0, neutral = 0;
        double sum = 0;
        for (String[] row : rows) {
            try {
                double score = Double.parseDouble(row[2]);
                sum += score;
                if (score > 2) positive++; else if (score < -2) negative++; else neutral++;
            } catch (NumberFormatException ignored) { }
        }
        System.out.println("\n--- History statistics ---");
        System.out.println("Total entries: " + rows.size());
        System.out.println("Positive: " + positive + " | Negative: " + negative + " | Neutral: " + neutral);
        System.out.println("Average sentiment score: " + round(sum / rows.size()));
        System.out.println("This summarizes text entries, not a person's mental-health status.");
    }

    private static void searchHistory() {
        System.out.print("Keyword to search > ");
        String term = IN.nextLine().trim().toLowerCase(Locale.ROOT);
        if (term.isEmpty()) return;
        int found = 0;
        for (String[] row : readHistory()) {
            if (row[5].toLowerCase(Locale.ROOT).contains(term)) {
                System.out.printf("%s | %-8s | %6s | %s%n", row[0], row[1], row[2], row[5]);
                found++;
            }
        }
        System.out.println("Matches: " + found);
    }

    private static void compareTexts() {
        System.out.print("Text A > ");
        String aText = IN.nextLine().trim();
        System.out.print("Text B > ");
        String bText = IN.nextLine().trim();
        if (aText.isEmpty() || bText.isEmpty()) { System.out.println("Enter both texts."); return; }
        Result a = analyze(aText), b = analyze(bText);
        System.out.println("\nComparison");
        System.out.printf("%-20s | %-18s | %-18s%n", "Metric", "Text A", "Text B");
        System.out.println("----------------------------------------------------------");
        System.out.printf("%-20s | %-18s | %-18s%n", "Sentiment", a.sentiment, b.sentiment);
        System.out.printf("%-20s | %-18.1f | %-18.1f%n", "Score", a.score, b.score);
        System.out.printf("%-20s | %-18s | %-18s%n", "Primary emotion", a.primaryEmotion, b.primaryEmotion);
        System.out.printf("%-20s | %-18d | %-18d%n", "Word count", a.words, b.words);
        System.out.printf("Score change (B - A): %.1f points%n", b.score - a.score);
    }

    private static void trainWord() {
        System.out.print("New single word (no spaces) > ");
        String word = IN.nextLine().trim().toLowerCase(Locale.ROOT);
        if (word.isEmpty() || word.contains(" ") || !word.matches("[\\p{L}\\p{N}'’-]+")) {
            System.out.println("Enter one valid word only."); return;
        }
        System.out.print("Sentiment weight (-5 to +5) > ");
        double weight;
        try {
            weight = Double.parseDouble(IN.nextLine().trim());
            if (!Double.isFinite(weight) || weight < -5 || weight > 5) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            System.out.println("Enter a number from -5 to +5."); return;
        }
        System.out.println("Emotion options: Happiness, Sadness, Anger, Fear, Love, Trust, Stress, Gratitude, Confidence, Hope, Confusion, Curiosity, Disappointment, Calmness, Surprise, Boredom, Pride, Jealousy, Loneliness, Disgust");
        System.out.print("Emotion (or Enter to skip) > ");
        String emotion = IN.nextLine().trim();
        if (!emotion.isEmpty() && !Arrays.asList("Happiness", "Sadness", "Anger", "Fear", "Love", "Trust", "Stress", "Gratitude", "Confidence", "Hope", "Confusion", "Curiosity", "Disappointment", "Calmness", "Surprise", "Boredom", "Pride", "Jealousy", "Loneliness", "Disgust").contains(emotion)) {
            System.out.println("Unknown emotion; word saved for sentiment only.");
            emotion = "";
        }
        SENTIMENT.put(word, weight);
        if (!emotion.isEmpty()) EMOTION_BY_WORD.put(word, emotion);
        try {
            Files.write(CUSTOM_LEXICON, Collections.singletonList(word + "," + weight + "," + emotion),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            System.out.println("Saved custom word: " + word);
        } catch (IOException ex) {
            System.out.println("Word added for this session but could not be saved: " + ex.getMessage());
        }
    }

    private static void loadCustomLexicon() {
        if (!Files.exists(CUSTOM_LEXICON)) return;
        try {
            for (String line : Files.readAllLines(CUSTOM_LEXICON, StandardCharsets.UTF_8)) {
                String[] p = line.split(",", -1);
                if (p.length < 2) continue;
                try {
                    String word = p[0].trim().toLowerCase(Locale.ROOT);
                    double weight = Double.parseDouble(p[1].trim());
                    if (word.isEmpty() || !Double.isFinite(weight) || weight < -5 || weight > 5) continue;
                    SENTIMENT.put(word, weight);
                    if (p.length >= 3 && !p[2].trim().isEmpty()) EMOTION_BY_WORD.put(word, p[2].trim());
                } catch (NumberFormatException ignored) { }
            }
        } catch (IOException ex) {
            System.out.println("Could not load custom lexicon: " + ex.getMessage());
        }
    }

    private static void batchAnalyze() {
        System.out.print("Path to UTF-8 text file > ");
        String path = IN.nextLine().trim();
        if (path.isEmpty()) return;
        Path file = Paths.get(path);
        if (!Files.isRegularFile(file)) { System.out.println("File not found."); return; }
        try {
            int count = 0;
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty()) continue;
                Result r = analyze(line.trim());
                append(r);
                System.out.printf("%-8s %6.1f %-14s %s%n", r.sentiment, r.score, r.primaryEmotion, shorten(r.text, 70));
                count++;
            }
            System.out.println("Batch complete. Saved " + count + " analyses.");
        } catch (IOException ex) {
            System.out.println("Could not read file: " + ex.getMessage());
        }
    }

    private static void exportDashboard() {
        List<String[]> rows = readHistory();
        if (rows.isEmpty()) { System.out.println("No history to export."); return; }
        int pos = 0, neg = 0, neu = 0;
        double total = 0;
        StringBuilder table = new StringBuilder();
        for (String[] r : rows) {
            try {
                double score = Double.parseDouble(r[2]);
                total += score;
                if (score > 2) pos++; else if (score < -2) neg++; else neu++;
                table.append("<tr><td>").append(html(r[0])).append("</td><td>")
                     .append(html(r[1])).append("</td><td>").append(score).append("</td><td>")
                     .append(html(r[3])).append("</td><td>").append(html(r[5])).append("</td></tr>");
            } catch (NumberFormatException ignored) { }
        }
        String page = "<!doctype html><html lang='en'><head><meta charset='utf-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'><title>Emotion Insight Dashboard</title>"
                + "<style>body{font-family:Arial,sans-serif;max-width:1100px;margin:30px auto;padding:0 16px;background:#f4f6fb;color:#172033}"
                + ".cards{display:flex;gap:12px;flex-wrap:wrap}.card{background:white;padding:18px;border-radius:10px;flex:1;min-width:150px;box-shadow:0 2px 8px #0001}"
                + "table{width:100%;border-collapse:collapse;background:white;margin-top:20px}th,td{padding:10px;text-align:left;border-bottom:1px solid #ddd;overflow-wrap:anywhere}"
                + "th{background:#e8edf7} .note{color:#586174;font-size:.9rem}</style></head><body>"
                + "<h1>Emotion Insight Engine 8.0</h1><p class='note'>Rule-based educational estimates; not a clinical assessment.</p><div class='cards'>"
                + card("Total entries", rows.size()) + card("Positive", pos) + card("Negative", neg) + card("Neutral", neu)
                + card("Average score", round(total / Math.max(1, rows.size()))) + "</div>"
                + "<table><thead><tr><th>Timestamp</th><th>Sentiment</th><th>Score</th><th>Primary emotion</th><th>Text</th></tr></thead><tbody>"
                + table + "</tbody></table></body></html>";
        try {
            Files.write(DASHBOARD, page.getBytes(StandardCharsets.UTF_8));
            System.out.println("Dashboard exported to: " + DASHBOARD.toAbsolutePath());
        } catch (IOException ex) {
            System.out.println("Could not export dashboard: " + ex.getMessage());
        }
    }

    private static String card(String title, Object value) {
        return "<div class='card'><div>" + html(title) + "</div><h2>" + html(String.valueOf(value)) + "</h2></div>";
    }

    private static void clearHistory() {
        System.out.print("Delete all saved analysis history? Type YES to confirm > ");
        if (!IN.nextLine().trim().equals("YES")) { System.out.println("Cancelled."); return; }
        try {
            Files.deleteIfExists(LOG);
            System.out.println("History deleted. Custom lexicon was kept.");
        } catch (IOException ex) {
            System.out.println("Could not delete history: " + ex.getMessage());
        }
    }

    private static String html(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String shorten(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static double round(double n) {
        return Math.round(n * 10.0) / 10.0;
    }

    private static void ensureLog() {
        if (Files.exists(LOG)) return;
        try {
            Files.write(LOG, Collections.singletonList("timestamp,sentiment,score,primary_emotion,word_count,text"),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            System.out.println("History file will be created when possible: " + ex.getMessage());
        }
    }
}

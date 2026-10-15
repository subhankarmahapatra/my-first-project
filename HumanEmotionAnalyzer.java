import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;

/**
 * Emotion Insight Engine PRO 6.0 - Ultimate Edition
 * Standalone, rule-based sentiment/emotion analyzer. No external libraries.
 * Compile: javac EmotionInsightEngine.java
 * Run:     java EmotionInsightEngine
 * Files: emotion_log.csv, emotion_report.txt
 * Note: educational estimates only; not a clinical or diagnostic tool.
 */
public class EmotionInsightEngine {
    private static final Scanner IN = new Scanner(System.in);
    private static final Path LOG = Paths.get("emotion_log.csv");
    private static final Path REPORT = Paths.get("emotion_report.txt");
    private static final String VERSION = "6.0 ULTIMATE";
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final LinkedHashMap<String, Double> SENTIMENT = new LinkedHashMap<>();
    private static final LinkedHashMap<String, LinkedHashSet<String>> EMOTIONS = new LinkedHashMap<>();
    private static final Map<String, Double> BOOST = new HashMap<>(), DAMPEN = new HashMap<>();
    private static final Set<String> NEGATIONS = new HashSet<>(Arrays.asList(
        "not","no","never","without","isn't","wasn't","aren't","don't","didn't","can't","cannot",
        "couldn't","won't","wouldn't","shouldn't","isnt","wasnt","arent","dont","didnt","cant",
        "couldnt","wont","wouldnt","shouldnt","hardly","neither","nor","n't"));
    private static final Pattern TOKEN = Pattern.compile("[\\p{L}\\p{N}]+(?:['’][\\p{L}]+)?");
    private static int sessionCount;
    private static double sessionTotal;

    private static final class Result {
        String timestamp, text, sentiment, primary, mood, suggestion;
        double score, intensity, confidence, stress, anxiety;
        int words, chars;
        LinkedHashMap<String, Double> emotions = new LinkedHashMap<>();
        List<String> keywords = new ArrayList<>(), flags = new ArrayList<>();
    }

    public static void main(String[] args) {
        setup(); ensureLog();
        banner();
        while (true) {
            menu(); System.out.print("\nEnter text or command > ");
            String line;
            try { line = IN.nextLine().trim(); } catch (NoSuchElementException e) { break; }
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                sessionSummary(); System.out.println("Goodbye. Take care!"); break;
            }
            if (line.isEmpty()) { System.out.println("Please enter text or a command."); continue; }
            switch (line.toLowerCase(Locale.ROOT)) {
                case "help": help(); break;
                case "about": about(); break;
                case "history": history(); break;
                case "stats": stats(); break;
                case "trend": trend(); break;
                case "search": search(); break;
                case "report": report(); break;
                case "clear": clearHistory(); break;
                case "session": sessionSummary(); break;
                default: analyzeAndSave(line);
            }
        }
    }

    private static void setup() {
        addSent(3, "happy joy joyful excited amazing grateful wonderful great good delighted cheerful hopeful enjoy awesome fantastic smile laugh fun positive glad pleased love lovely success successful win winner best brilliant perfect peaceful confident proud relaxed thankful optimistic calm safe relief relieved beautiful inspired motivated comfortable content satisfied excellent kind blessed capable delicious helpful hope celebrate celebration friendly appreciate appreciated thank thanks peace serene secure trust honest loyal brave strong determined thrilled satisfied wonderful");
        addSent(-3, "sad hurt pain angry lonely depressed bad terrible upset miserable stressed tired hopeless afraid worried anxious frustrated hate horrible awful failure fail failed problem cry crying broken disappointed disappointing exhausted nervous panic scared fear confused regret stress overwhelmed unhappy heartbroken helpless irritated furious annoyed disgusted guilty ashamed bored weak worthless difficult worst negative pressure burnout overworked unfair dislike unsafe insecure grief sorrow worry rage enraged terrified frightened nasty revolting repulsive sickening letdown unfulfilled miserable");
        putEmotion("Happiness", "happy joy joyful excited delighted cheerful glad pleased awesome fantastic smile laugh fun amazing wonderful enjoy positive excellent celebrate celebration thrilled");
        putEmotion("Sadness", "sad cry crying lonely depressed hurt pain upset miserable hopeless heartbroken grief broken disappointed sorrow unhappy tears alone helpless");
        putEmotion("Anger", "angry mad hate frustrated furious annoyed rage irritated enraged revenge outraged irritating");
        putEmotion("Fear", "scared fear worried anxious nervous terrified afraid panic danger threat frightened worry");
        putEmotion("Love", "love lovely caring special affection adore romantic cherish beloved kiss relationship care darling dear");
        putEmotion("Surprise", "surprised shocked unexpected astonished wow unbelievable suddenly surprise amazed");
        putEmotion("Disgust", "disgusted gross awful revolting nasty repulsive disgusting dirty dislike sickening");
        putEmotion("Trust", "trust trusted reliable honest loyal faithful dependable secure believe respect");
        putEmotion("Anticipation", "expect expecting anticipate future waiting ready upcoming tomorrow plan planning forward");
        putEmotion("Confusion", "confused confusing uncertain unsure doubt doubtful lost unclear puzzled wondering");
        putEmotion("Pride", "proud achievement achieved success successful winner winning accomplished accomplishment progress");
        putEmotion("Calmness", "calm peaceful relaxed relax quiet comfortable safe serene peace relief relieved");
        putEmotion("Stress", "stress stressed pressure overwhelmed busy exhausted deadline tension burnout overworked workload");
        putEmotion("Gratitude", "grateful thankful thanks appreciate appreciated blessed gratitude thank");
        putEmotion("Disappointment", "disappointed disappointing failure failed regret letdown unfulfilled");
        putEmotion("Confidence", "confident capable brave courage determined strong prepared believe");
        putEmotion("Curiosity", "curious question wonder explore learn discover interesting why how");
        putEmotion("Jealousy", "jealous envy envious possessive");
        putEmotion("Shame", "ashamed embarrassed humiliation humiliated shame");
        putEmotion("Boredom", "bored boring dull uninterested tedious");
        putEmotion("Hope", "hope hopeful optimistic future possible possibility");
        putEmotion("Loneliness", "lonely alone isolated abandoned disconnected");
        String[] boosts = {"very","really","extremely","incredibly","absolutely","totally","completely","deeply","highly","super","truly","immensely","so","too","utterly"};
        for (String w : boosts) BOOST.put(w, 1.5);
        BOOST.put("extremely", 2.0); BOOST.put("incredibly", 1.8); BOOST.put("absolutely", 1.7); BOOST.put("utterly", 1.8);
        for (String w : Arrays.asList("slightly","somewhat","little","mildly","kinda","kindof","barely","bit","almost","fairly","rather")) DAMPEN.put(w, .6);
        DAMPEN.put("barely", .4); DAMPEN.put("slightly", .5);
    }
    private static void addSent(double weight, String words) { for (String w : words.split(" ")) SENTIMENT.put(w, weight); }
    private static void putEmotion(String name, String words) { EMOTIONS.put(name, new LinkedHashSet<>(Arrays.asList(words.split(" ")))); }
    private static void banner() {
        System.out.println("\n============================================================");
        System.out.println("          EMOTION INSIGHT ENGINE PRO " + VERSION);
        System.out.println("============================================================");
        System.out.println(EMOTIONS.size() + " emotion categories | sentiment | history | analytics");
        System.out.println("Rule-based educational tool; results can miss context and sarcasm.");
    }
    private static void menu() {
        System.out.println("\n[TEXT] Analyze | history | stats | trend | search | report | clear | session | help | about | exit");
    }
    private static void help() {
        System.out.println("Type a sentence to analyze it. Commands: history, stats, trend, search, report, clear, session, about, help, exit.");
        System.out.println("Examples: I feel very happy today! | I am not happy | I feel worried about tomorrow.");
    }
    private static void about() { System.out.println("Emotion Insight Engine " + VERSION + " uses word dictionaries, modifiers, negation, phrases, punctuation, and emoji clues. It is not genuine AI or a mental-health diagnosis."); }

    private static Result analyze(String text) {
        Result r = new Result(); r.timestamp = LocalDateTime.now().format(TS); r.text = text; r.chars = text.length();
        String lower = text.toLowerCase(Locale.ROOT).replace('’', '\'');
        List<String> words = tokens(lower); r.words = words.size();
        for (String e : EMOTIONS.keySet()) r.emotions.put(e, 0.0);
        Map<String,Integer> freq = new LinkedHashMap<>(); double score = 0; int matched = 0, negWindow = 0; double modifier = 1;
        for (int i=0; i<words.size(); i++) {
            String w = words.get(i); freq.put(w, freq.getOrDefault(w,0)+1);
            if (NEGATIONS.contains(w) || w.endsWith("n't")) { negWindow = 3; modifier = 1; continue; }
            if (BOOST.containsKey(w)) { modifier = BOOST.get(w); continue; }
            if (DAMPEN.containsKey(w)) { modifier = DAMPEN.get(w); continue; }
            boolean negated = negWindow > 0;
            Double s = SENTIMENT.get(w);
            if (s != null) {
                score += negated ? -s * modifier : s * modifier; matched++;
                r.keywords.add(w + (negated ? " [negated]" : ""));
            }
            for (Map.Entry<String,LinkedHashSet<String>> e : EMOTIONS.entrySet()) {
                if (e.getValue().contains(w)) {
                    double add = modifier * (negated ? -.65 : 1.0);
                    r.emotions.put(e.getKey(), r.emotions.get(e.getKey()) + add);
                }
            }
            if (negWindow > 0) negWindow--; modifier = 1;
        }
        String norm = lower.replaceAll("[^\\p{L}\\p{N}' ]", " ").replaceAll("\\s+", " ").trim();
        if (norm.contains("thank you")) { bump(r,"Gratitude",2.5); score += 2; }
        if (norm.contains("looking forward")) bump(r,"Anticipation",2);
        if (norm.contains("fed up")) { bump(r,"Anger",2); bump(r,"Stress",1); score -= 2; }
        if (norm.contains("give up")) { bump(r,"Sadness",2); score -= 2; }
        if (norm.contains("on cloud nine")) { bump(r,"Happiness",3); score += 3; }
        if (norm.contains("couldn't be happier") || norm.contains("could not be happier")) { bump(r,"Happiness",3); score += 3; }
        for (Map.Entry<String,Integer> e : freq.entrySet()) if (e.getValue() >= 3) r.flags.add("Repeated word: " + e.getKey() + " x" + e.getValue());
        int bangs = count(text,'!'), questions = count(text,'?');
        if (questions > 0) r.flags.add("Question punctuation detected");
        if (bangs >= 2) r.flags.add("Strong exclamation emphasis");
        int letters=0, caps=0; for(char c:text.toCharArray()) if(Character.isLetter(c)){letters++; if(Character.isUpperCase(c))caps++;}
        boolean allCaps = letters >= 4 && caps >= 4 && caps >= letters*.65;
        if(allCaps) r.flags.add("High capitalization intensity");
        if(any(text,"😂","😊","❤️","😍","😁","🥰","😄","🙂","😃")){score+=2;bump(r,"Happiness",1.5);r.flags.add("Positive emoji clue");}
        if(any(text,"😢","😭","😔","💔","🙁","☹️")){score-=2;bump(r,"Sadness",1.5);r.flags.add("Sadness-related emoji clue");}
        if(any(text,"😡","🤬","😠")){score-=2;bump(r,"Anger",1.5);r.flags.add("Anger-related emoji clue");}
        if(any(text,"😨","😰","😱")){bump(r,"Fear",1.5);r.flags.add("Fear-related emoji clue");}
        if(any(norm,"yeah right","as if","just great","oh wonderful")) r.flags.add("Possible sarcasm: interpretation uncertain");
        if(norm.contains("but")) r.flags.add("Contrast word detected; emotions may change within the sentence");
        r.score = clamp(score,-100,100);
        r.sentiment = r.score > 1 ? "Positive" : r.score < -1 ? "Negative" : "Neutral";
        r.intensity = clamp(Math.abs(r.score)*5 + Math.min(15,bangs*3) + (allCaps?10:0),0,100);
        double max=0, sum=0;
        for(String e:r.emotions.keySet()){double v=Math.max(0,r.emotions.get(e)); r.emotions.put(e,round(v));sum+=v;max=Math.max(max,v);}
        r.primary="Mixed / Undetected";
        if(max>0){for(Map.Entry<String,Double> e:r.emotions.entrySet()) if(e.getValue()==max){r.primary=e.getKey();break;}}
        r.confidence = words.isEmpty()?0:clamp((matched*13.0 + max*4.0),0,95);
        r.stress = indicator(r,"Stress","Anger","Fear"); r.anxiety=indicator(r,"Fear","Stress","Confusion");
        r.mood = mood(r.sentiment,r.primary,r.intensity); r.suggestion=suggestion(r.sentiment,r.primary);
        if(words.isEmpty()) r.flags.add("No recognizable words found");
        r.flags.add("Heuristic estimate; not a clinical assessment."); return r;
    }
    private static List<String> tokens(String s){List<String> a=new ArrayList<>();Matcher m=TOKEN.matcher(s);while(m.find())a.add(m.group().replace('’','\''));return a;}
    private static void bump(Result r,String e,double n){if(r.emotions.containsKey(e))r.emotions.put(e,r.emotions.get(e)+n);}
    private static double indicator(Result r,String... names){double n=0;for(String s:names)n+=Math.max(0,r.emotions.getOrDefault(s,0.0));return clamp(n*10,0,100);}
    private static String mood(String s,String e,double i){if(s.equals("Positive"))return i>=60?"Strongly positive":"Positive / hopeful";if(s.equals("Negative"))return i>=60?"Strong negative signals":"Low mood signals";return e.equals("Mixed / Undetected")?"Neutral or unclear":"Mixed / emotionally nuanced";}
    private static String suggestion(String s,String e){
        switch(e){case "Stress":case "Fear":case "Anger":return "Pause for a moment, slow your breathing, and identify one manageable next step.";
        case "Sadness":case "Loneliness":return "Consider talking with someone you trust or doing one small supportive activity.";
        case "Confusion":return "Write down what is unclear and separate facts from assumptions.";
        case "Happiness":case "Gratitude":case "Hope":return "Notice what contributed to this feeling and consider recording it for later.";
        case "Confidence":case "Pride":return "Recognize the progress you made and choose a realistic next goal.";
        default:return s.equals("Negative")?"Take a short break and consider what support or practical action would help.":"Reflect on the situation and the specific feeling behind your words.";}
    }
    private static void analyzeAndSave(String text){Result r=analyze(text);printResult(r);append(r);sessionCount++;sessionTotal+=r.score;}
    private static void printResult(Result r){
        System.out.println("\n================ ANALYSIS RESULT ================");
        System.out.println("Time: "+LocalDateTime.parse(r.timestamp,TS).format(DISPLAY));
        System.out.println("Sentiment: "+r.sentiment+" | Score: "+round(r.score)+" (-100 to +100)");
        System.out.println("Primary emotion: "+r.primary+" | Mood: "+r.mood);
        System.out.println("Intensity: "+round(r.intensity)+"/100 | Heuristic confidence: "+round(r.confidence)+"/100");
        System.out.println("Stress-related clues: "+round(r.stress)+"/100 | Anxiety-related clues: "+round(r.anxiety)+"/100");
        System.out.println("Words: "+r.words+" | Characters: "+r.chars);
        System.out.println("Top emotions:");
        r.emotions.entrySet().stream().filter(e->e.getValue()>0).sorted((a,b)->Double.compare(b.getValue(),a.getValue())).limit(5)
            .forEach(e->System.out.println("  - "+e.getKey()+": "+round(e.getValue())));
        if(r.keywords.isEmpty())System.out.println("Matched sentiment words: none");else System.out.println("Matched words: "+String.join(", ",r.keywords));
        if(!r.flags.isEmpty()){System.out.println("Notes:");for(String f:r.flags)System.out.println("  - "+f);}
        System.out.println("Suggestion: "+r.suggestion);System.out.println("=================================================");
    }
    private static void ensureLog(){if(!Files.exists(LOG))try{Files.write(LOG,Collections.singleton("timestamp,sentiment,score,primary,mood,intensity,confidence,stress,anxiety,words,characters,text,keywords,flags,suggestion"),StandardCharsets.UTF_8,StandardOpenOption.CREATE);}catch(IOException e){System.out.println("Log initialization warning: "+e.getMessage());}}
    private static void append(Result r){String row=csv(r.timestamp)+","+csv(r.sentiment)+","+r.score+","+csv(r.primary)+","+csv(r.mood)+","+r.intensity+","+r.confidence+","+r.stress+","+r.anxiety+","+r.words+","+r.chars+","+csv(r.text)+","+csv(String.join("; ",r.keywords))+","+csv(String.join("; ",r.flags))+","+csv(r.suggestion);try{Files.write(LOG,Collections.singleton(row),StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(IOException e){System.out.println("Could not save history: "+e.getMessage());}}
    private static String csv(String s){return "\""+s.replace("\"","\"\"")+"\"";}
    private static List<String> readRows(){try{if(!Files.exists(LOG))return new ArrayList<>();return Files.readAllLines(LOG,StandardCharsets.UTF_8);}catch(IOException e){System.out.println("Read error: "+e.getMessage());return new ArrayList<>();}}
    private static void history(){List<String> rows=readRows();if(rows.size()<=1){System.out.println("No saved analyses yet.");return;}int start=Math.max(1,rows.size()-11);for(int i=start;i<rows.size();i++)System.out.println(rows.get(i));}
    
    private static void stats(){
        List<String> rows=readRows();
        if(rows.size()<=1){System.out.println("No data yet.");return;}
        int pos=0,neg=0,neu=0;double total=0;Map<String,Integer> counts=new TreeMap<>();
        for(int i=1;i<rows.size();i++){
            String[] c=parseCsv(rows.get(i));
            if(c.length<4)continue;
            try{
                double sc=Double.parseDouble(c[2]);
                total+=sc;
                if(sc>1)pos++;else if(sc< -1)neg++;else neu++;
                counts.put(c[3],counts.getOrDefault(c[3],0)+1);
            }catch(Exception ignored){}
        }
        int n=pos+neg+neu;
        System.out.println("Saved analyses: "+n+" | Positive: "+pos+" | Negative: "+neg+" | Neutral: "+neu);
        if(n>0)System.out.println("Average sentiment score: "+round(total/n));
        System.out.println("Primary emotion counts: "+counts);
    }
    
    private static void trend(){
        List<String> rows=readRows();
        if(rows.size()<=1){System.out.println("No data yet.");return;}
        int start=Math.max(1,rows.size()-8);
        System.out.println("Recent sentiment trend (latest 7 records):");
        for(int i=start;i<rows.size();i++){
            String[] c=parseCsv(rows.get(i));
            if(c.length<3)continue;
            try{
                double v=Double.parseDouble(c[2]);
                System.out.printf("%s | %6.1f | %s%n",c[0],v,bar(v));
            }catch(Exception ignored){}
        }
    }
    
    private static String bar(double v){int n=(int)Math.round(Math.abs(v)/5);char ch=v>1?'+':v< -1?'-':'=';StringBuilder b=new StringBuilder();for(int i=0;i<n;i++)b.append(ch);return b.toString();}
    
    private static void search() {
        System.out.print("Search text > ");
        String q = IN.nextLine().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return;
        int found = 0;
        List<String> rows = readRows();
        for (int i = 1; i < rows.size(); i++) {
            if (rows.get(i).toLowerCase(Locale.ROOT).contains(q)) {
                System.out.println(rows.get(i));
                found++;
            }
        }
        System.out.println("Found " + found + " matching records.");
    }

    private static void report() {
        try {
            List<String> rows = readRows();
            Files.write(REPORT, rows, StandardCharsets.UTF_8);
            System.out.println("Data exported successfully to " + REPORT.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Failed to write report: " + e.getMessage());
        }
    }

    private static void clearHistory() {
        try {
            Files.deleteIfExists(LOG);
            sessionCount = 0;
            sessionTotal = 0.0;
            System.out.println("Analysis history cleared.");
            ensureLog();
        } catch (IOException e) {
            System.out.println("Failed to clear history: " + e.getMessage());
        }
    }

    private static void sessionSummary() {
        System.out.println("\n=== Session Summary ===");
        System.out.println("Analyses performed this session: " + sessionCount);
        if (sessionCount > 0) {
            System.out.println("Average sentiment score: " + round(sessionTotal / sessionCount));
        }
        System.out.println("=======================");
    }

    private static int count(String s, char target) {
        int count = 0;
        for (char c : s.toCharArray()) if (c == target) count++;
        return count;
    }

    private static boolean any(String text, String... targets) {
        for (String target : targets) if (text.contains(target)) return true;
        return false;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static String[] parseCsv(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentToken = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(currentToken.toString());
                currentToken.setLength(0);
            } else {
                currentToken.append(c);
            }
        }
        tokens.add(currentToken.toString());
        return tokens.toArray(new String[0]);
    }
}

import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class HumanEmotionAnalyzer {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🤖 HUMAN EMOTION ANALYZER — REAL HUMAN MODE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        System.out.print("🧠 Tell me honestly… what's going on in your heart right now:\n> ");

        String input = sc.nextLine().toLowerCase().trim();

        if (input.isEmpty()) {
            System.out.println("⚠️ Please enter something so I can analyze it.");
            sc.close();
            return;
        }

        System.out.println("\n⏳ Reading your emotional signals...");
        delay(900);

        Map<String, Double> emotionScores = analyzeEmotion(input);

        String primaryEmotion = getPrimaryEmotion(emotionScores);

        double sentimentScore = calculateSentiment(input);

        double intensity = calculateIntensity(emotionScores);

        String moodLevel = getMoodLevel(sentimentScore);

        String reflection =
                generateReflection(primaryEmotion, sentimentScore, input);

        String recommendation =
                generateRecommendation(primaryEmotion);

        String detectedWords =
                detectImportantWords(input);

        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("💡 EMOTIONAL ANALYSIS RESULT");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        System.out.printf("• Primary Emotion     : %s%n", primaryEmotion);
        System.out.printf("• Sentiment Score     : %.1f / 100%n", sentimentScore);
        System.out.printf("• Emotional Intensity : %.1f%%%n", intensity);
        System.out.printf("• Mood Level          : %s%n", moodLevel);

        System.out.println("\n🔎 Words I noticed:");
        System.out.println("• " + detectedWords);

        System.out.println("\n💬 My response:");
        System.out.println(reflection);

        System.out.println("\n🧭 Suggested action:");
        System.out.println(recommendation);

        System.out.println("\n📊 Emotional Weights:");

        emotionScores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry ->
                        System.out.printf(
                                "• %-10s : %.2f%n",
                                entry.getKey(),
                                entry.getValue()
                        )
                );

        System.out.println("\n🧠 Emotional Signature:");

        displayEmotionBar(emotionScores);

        System.out.println("\n🕒 Analysis Time: " +
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                ));

        System.out.println("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("❤️ Remember: emotions are signals, not definitions.");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        sc.close();
    }


    // ============================================================
    // EMOTION ANALYSIS
    // ============================================================

    public static Map<String, Double> analyzeEmotion(String text) {

        Map<String, Double> scores = new LinkedHashMap<>();

        scores.put("Happiness", weightedCount(text, Map.ofEntries(

                Map.entry("happy", 2.0),
                Map.entry("joy", 3.0),
                Map.entry("joyful", 3.0),
                Map.entry("overjoyed", 3.5),
                Map.entry("thrilled", 3.0),
                Map.entry("excited", 2.5),
                Map.entry("amazing", 2.5),
                Map.entry("beautiful", 2.0),
                Map.entry("perfect", 2.0),
                Map.entry("smiling", 2.0),
                Map.entry("grateful", 2.2),
                Map.entry("blessed", 2.5),
                Map.entry("fantastic", 2.3),
                Map.entry("positive", 1.8),
                Map.entry("peaceful", 2.0),
                Map.entry("proud", 2.5),
                Map.entry("glowing", 2.5),
                Map.entry("wonderful", 2.5),
                Map.entry("love", 2.5),
                Map.entry("ecstatic", 3.5),
                Map.entry("loveable", 4.2),
                Map.entry("on top of the world", 4.0)
                Map.entry("on love". 4.1)
        )));


        scores.put("Sadness", weightedCount(text, Map.ofEntries(

                Map.entry("sad", 2.0),
                Map.entry("down", 1.5),
                Map.entry("upset", 1.8),
                Map.entry("cry", 2.0),
                Map.entry("crying", 2.5),
                Map.entry("hurt", 2.2),
                Map.entry("pain", 2.3),
                Map.entry("lonely", 2.5),
                Map.entry("alone", 2.0),
                Map.entry("empty", 2.0),
                Map.entry("broken", 2.5),
                Map.entry("depressed", 3.0),
                Map.entry("lost", 2.0),
                Map.entry("miss", 1.8)
        )));


        scores.put("Anger", weightedCount(text, Map.ofEntries(

                Map.entry("angry", 2.2),
                Map.entry("mad", 1.8),
                Map.entry("furious", 2.8),
                Map.entry("rage", 2.5),
                Map.entry("hate", 2.5),
                Map.entry("annoyed", 1.8),
                Map.entry("frustrated", 2.2),
                Map.entry("irritated", 2.0),
                Map.entry("disappointed", 1.5)
        )));


        scores.put("Fear", weightedCount(text, Map.ofEntries(

                Map.entry("scared", 2.3),
                Map.entry("fear", 2.0),
                Map.entry("afraid", 2.2),
                Map.entry("nervous", 2.0),
                Map.entry("worried", 2.2),
                Map.entry("anxious", 2.5),
                Map.entry("panic", 3.0),
                Map.entry("uncertain", 1.8)
        )));


        scores.put("Love", weightedCount(text, Map.ofEntries(

                Map.entry("love", 3.0),
                Map.entry("loving", 3.0),
                Map.entry("romantic", 2.5),
                Map.entry("care", 1.8),
                Map.entry("caring", 2.0),
                Map.entry("dear", 1.5),
                Map.entry("special", 2.0),
                Map.entry("beautiful", 1.5)
        )));


        scores.put("Surprise", weightedCount(text, Map.ofEntries(

                Map.entry("wow", 2.5),
                Map.entry("unexpected", 2.5),
                Map.entry("surprised", 3.0),
                Map.entry("shocked", 3.0),
                Map.entry("unbelievable", 2.5)
        )));


        // Negation correction
        applyNegationCorrection(text, scores);

        return scores;
    }


    // ============================================================
    // WEIGHTED WORD COUNTER
    // ============================================================

    public static double weightedCount(
            String text,
            Map<String, Double> wordWeights) {

        double score = 0.0;

        for (Map.Entry<String, Double> entry : wordWeights.entrySet()) {

            if (text.contains(entry.getKey())) {

                score += entry.getValue();
            }
        }

        return score;
    }


    // ============================================================
    // NEGATION DETECTION
    // ============================================================

    public static void applyNegationCorrection(
            String text,
            Map<String, Double> scores) {

        String[] negativeWords = {
                "not",
                "never",
                "don't",
                "dont",
                "isn't",
                "isnt",
                "wasn't",
                "wasnt"
        };

        boolean negationFound = false;

        for (String word : negativeWords) {

            if (text.contains(word)) {
                negationFound = true;
                break;
            }
        }

        if (negationFound) {

            scores.put(
                    "Happiness",
                    scores.get("Happiness") * 0.5
            );

            scores.put(
                    "Love",
                    scores.get("Love") * 0.7
            );
        }
    }


    // ============================================================
    // PRIMARY EMOTION
    // ============================================================

    public static String getPrimaryEmotion(
            Map<String, Double> scores) {

        return scores.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Neutral");
    }


    // ============================================================
    // SENTIMENT SCORE
    // ============================================================

    public static double calculateSentiment(String text) {

        double score = 50;

        String[] positiveWords = {
                "happy",
                "joy",
                "amazing",
                "beautiful",
                "perfect",
                "excited",
                "love",
                "grateful",
                "wonderful",
                "proud",
                "fantastic"
        };

        String[] negativeWords = {
                "sad",
                "hurt",
                "pain",
                "angry",
                "hate",
                "lonely",
                "broken",
                "scared",
                "fear",
                "worried",
                "depressed"
        };


        for (String word : positiveWords) {

            if (text.contains(word)) {
                score += 7;
            }
        }


        for (String word : negativeWords) {

            if (text.contains(word)) {
                score -= 7;
            }
        }


        if (score > 100)
            score = 100;

        if (score < 0)
            score = 0;

        return score;
    }


    // ============================================================
    // EMOTIONAL INTENSITY
    // ============================================================

    public static double calculateIntensity(
            Map<String, Double> scores) {

        double total = 0;

        for (double value : scores.values()) {
            total += value;
        }

        double intensity = Math.min(total * 8, 100);

        return intensity;
    }


    // ============================================================
    // MOOD LEVEL
    // ============================================================

    public static String getMoodLevel(double sentiment) {

        if (sentiment >= 80)
            return "🌟 Extremely Positive";

        if (sentiment >= 65)
            return "😊 Positive";

        if (sentiment >= 45)
            return "😐 Balanced / Neutral";

        if (sentiment >= 25)
            return "😔 Negative";

        return "🌧️ Very Negative";
    }


    // ============================================================
    // REFLECTION ENGINE
    // ============================================================

    public static String generateReflection(
            String emotion,
            double sentiment,
            String text) {

        switch (emotion) {

            case "Happiness":

                return "😊 Your words carry a positive and uplifting energy. "
                        + "There seems to be something making you genuinely happy.";

            case "Sadness":

                return "💙 Your words suggest that something may be weighing heavily "
                        + "on you. Sometimes expressing what we feel is the first step "
                        + "toward understanding it.";

            case "Anger":

                return "🔥 I can sense frustration or anger in your words. "
                        + "Something seems to have crossed an important boundary for you.";

            case "Fear":

                return "🌧️ Your message contains signs of worry or uncertainty. "
                        + "It may help to separate what you can control from what you cannot.";

            case "Love":

                return "❤️ Your words contain strong signs of affection and emotional connection.";

            case "Surprise":

                return "😲 Your message suggests that something unexpected "
                        + "has strongly affected you.";

            default:

                return "😐 Your emotional signals seem mixed. "
                        + "There may be more than one feeling present.";
        }
    }


    // ============================================================
    // RECOMMENDATION ENGINE
    // ============================================================

    public static String generateRecommendation(
            String emotion) {

        switch (emotion) {

            case "Happiness":
                return "✨ Enjoy the moment. Write down what made you feel this way.";

            case "Sadness":
                return "💙 Give yourself some space and consider talking to someone you trust.";

            case "Anger":
                return "🧘 Take a short pause before reacting. A few calm minutes can help.";

            case "Fear":
                return "🌿 Focus on the next small thing you can actually control.";

            case "Love":
                return "❤️ If appropriate, express appreciation to the person who matters to you.";

            case "Surprise":
                return "🔎 Take a moment to understand what happened before reacting.";

            default:
                return "📝 Keep expressing your thoughts. More context can reveal more patterns.";
        }
    }


    // ============================================================
    // IMPORTANT WORD DETECTOR
    // ============================================================

    public static String detectImportantWords(String text) {

        String[] importantWords = {

                "happy", "sad", "love", "angry",
                "excited", "hurt", "pain",
                "scared", "worried", "beautiful",
                "amazing", "proud", "lonely",
                "grateful", "special"
        };

        List<String> found = new ArrayList<>();

        for (String word : importantWords) {

            if (text.contains(word)) {
                found.add(word);
            }
        }

        if (found.isEmpty()) {
            return "No strong emotional keywords detected.";
        }

        return String.join(", ", found);
    }


    // ============================================================
    // EMOTION BAR
    // ============================================================

    public static void displayEmotionBar(
            Map<String, Double> scores) {

        for (Map.Entry<String, Double> entry : scores.entrySet()) {

            int bars = (int) Math.min(entry.getValue() * 2, 20);

            System.out.printf(
                    "%-10s | %s %.2f%n",
                    entry.getKey(),
                    "█".repeat(bars),
                    entry.getValue()
            );
        }
    }


    // ============================================================
    // DELAY
    // ============================================================

    public static void delay(int ms) {

        try {

            Thread.sleep(ms);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }
}

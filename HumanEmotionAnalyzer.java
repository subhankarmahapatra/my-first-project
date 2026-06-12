import java.util.*;

public class HumanEmotionAnalyzer {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🤖 HUMAN EMOTION ANALYZER — REAL HUMAN MODE");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.print("🧠 Tell me honestly… what’s going on in your heart right now: ");

        String input = sc.nextLine().toLowerCase();

        Map<String, Double> emotionScores = analyzeEmotion(input);
        String primaryEmotion = getPrimaryEmotion(emotionScores);
        double sentimentScore = calculateSentiment(input);
        String reflection = generateReflection(primaryEmotion, sentimentScore, input);

        System.out.println("\n⏳ Let me sit with your words for a moment...");
        delay(1100);

        System.out.println("\n💡 What I feel from your message:");
        System.out.printf("• Primary Emotion : %s%n", primaryEmotion);
        System.out.printf("• Sentiment Score : %.1f%n", sentimentScore);

        System.out.println("\n💬 My honest response:");
        System.out.println(reflection);

        System.out.println("\n📊 Emotional weights I sensed:");
        emotionScores.forEach((k, v) -> System.out.printf("• %-10s : %.2f%n", k, v));

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        sc.close();
    }

    // EMOTION ANALYSIS WITH REAL HUMAN VOCABULARY
    public static Map<String, Double> analyzeEmotion(String text) {
        Map<String, Double> scores = new HashMap<>();

        // HAPPINESS — now includes REAL human expressions
        scores.put("Happiness", weightedCount(text, Map.ofEntries(
                Map.entry("happy", 2.0),
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
                Map.entry("light", 1.5),
                Map.entry("proud", 2.5),
                Map.entry("glowing", 2.5),
                Map.entry("wonderful", 2.5),
                Map.entry("joy", 3.0),
                Map.entry("love", 2.5),
                Map.entry("ecstatic", 3.5),
                Map.entry("on top of the world", 4.0)
        )));

        // SADNESS
        scores.put("Sadness", weightedCount(text, Map.ofEntries(
                Map.entry("sad", 2.0),
                Map.entry("down", 1.5),
                Map.entry("upset", 1.8),
                Map.entry("cry", 2.0),
                Map.entry("hurt", 2.2),
                Map.entry("pain", 2.3),
                Map.entry("lonely", 2.5),
                Map.entry("empty", 2.0),
                Map.entry("broken", 2.5),
                Map.entry("depressed", 3.0)
        )));

        // ANGER
        scores.put("Anger", weightedCount(text, Map.ofEntries(
                Map.entry("angry", 2.2),
                Map.entry("mad", 1.8),
                Map.entry("furious", 2.8),
                Map.entry("rage", 2.5),
                Map.entry("hate", 2.5),
                Map.entry("annoyed", 1.8),
                Map.entry("frustrated", 2.2)
        )));

        // FEAR
        scores.put("Fear", weightedCount(text, Map.ofEntries(
                Map.entry("scared", 2.3),
                Map.entry("fear", 2.0),
                Map.entry("afraid", 2.2),
                Map.entry("nervous", 2.0),
                Map.entry("worried", 2.2),
                Map.entry("anxious", 2.5)
        )));

        return scores;
    }

    // Weighted count
    public static double weightedCount(String text, Map<String, Double> wordWeights) {
        double score = 0.0;
        for (Map.Entry<String, Double> entry : wordWeights.entrySet()) {
            if (text.contains(entry.getKey())) {
                score += entry.getValue();
            }
        }
        return score;
    }

    public static String getPrimaryEmotion(Map<String, Double> scores) {
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get().getKey();
    }

    public static double calculateSentiment(String text) {
        if (text.contains("overjoyed")) return 90;
        if (text.contains("thrilled")) return 85;
        if (text.contains("amazing")) return 70;
        if (text.contains("proud")) return 75;
        if (text.contains("beautiful")) return 60;
        return 0;
    }

    public static String generateReflection(String emotion, double sentiment, String text) {

        if (emotion.equals("Happiness") && sentiment > 50) {
            return "😊 Your message doesn’t just sound happy — it feels deeply joyful. "
                 + "‘Overjoyed’ is a powerful word, and I can feel that excitement in your tone. "
                 + "Whatever you achieved, you truly deserve this happiness.";
        }

        if (emotion.equals("Happiness")) {
            return "🙂 I can sense real positivity in your words. "
                 + "There’s a warm, uplifting energy in what you said.";
        }

        return "😐 Your message feels mixed, but I’m here to understand you more.";
    }

    public static void delay(int ms) {
        try { Thread.sleep(ms); } catch (Exception e) {}
    }
}

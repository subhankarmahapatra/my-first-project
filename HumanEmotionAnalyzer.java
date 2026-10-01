import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class HumanEmotionAnalyzer {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter your feelings:\n> ");
        String input = sc.nextLine().toLowerCase().trim();

        if (input.isEmpty()) {
            System.out.println("Please enter something so I can analyze it.");
            sc.close();
            return;
        }

        Map<String, Double> emotionScores = analyzeEmotion(input);
        String primaryEmotion = getPrimaryEmotion(emotionScores);
        double sentimentScore = calculateSentiment(input);
        double intensity = calculateIntensity(emotionScores);
        String moodLevel = getMoodLevel(sentimentScore);
        String reflection = generateReflection(primaryEmotion);
        String recommendation = generateRecommendation(primaryEmotion);
        String detectedWords = detectImportantWords(input);

        System.out.println("\n--- Emotional Analysis ---");
        System.out.printf("Primary Emotion     : %s%n", primaryEmotion);
        System.out.printf("Sentiment Score     : %.1f / 100%n", sentimentScore);
        System.out.printf("Emotional Intensity : %.1f%%%n", intensity);
        System.out.printf("Mood Level          : %s%n", moodLevel);

        System.out.println("\nKeywords detected: " + detectedWords);
        System.out.println("Reflection: " + reflection);
        System.out.println("Recommendation: " + recommendation);

        System.out.println("\nEmotion Weights:");
        emotionScores.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry ->
                        System.out.printf("• %-10s : %.2f%n", entry.getKey(), entry.getValue())
                );

        System.out.println("\nAnalysis Time: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));

        sc.close();
    }

    // Emotion analysis
    public static Map<String, Double> analyzeEmotion(String text) {
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("Happiness", weightedCount(text, Map.of("happy", 2.0, "joy", 3.0, "excited", 2.5, "love", 2.5)));
        scores.put("Sadness", weightedCount(text, Map.of("sad", 2.0, "cry", 2.0, "lonely", 2.5, "depressed", 3.0)));
        scores.put("Anger", weightedCount(text, Map.of("angry", 2.2, "mad", 1.8, "hate", 2.5, "frustrated", 2.2)));
        scores.put("Fear", weightedCount(text, Map.of("scared", 2.3, "fear", 2.0, "worried", 2.2, "anxious", 2.5)));
        scores.put("Love", weightedCount(text, Map.of("love", 3.0, "caring", 2.0, "special", 2.0)));
        return scores;
    }

    public static double weightedCount(String text, Map<String, Double> wordWeights) {
        return wordWeights.entrySet().stream()
                .filter(entry -> text.contains(entry.getKey()))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }

    public static String getPrimaryEmotion(Map<String, Double> scores) {
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Neutral");
    }

    public static double calculateSentiment(String text) {
        double score = 50;
        String[] positiveWords = {"happy", "joy", "amazing", "love", "grateful"};
        String[] negativeWords = {"sad", "hurt", "pain", "angry", "lonely", "depressed"};

        for (String word : positiveWords) if (text.contains(word)) score += 7;
        for (String word : negativeWords) if (text.contains(word)) score -= 7;

        return Math.max(0, Math.min(100, score));
    }

    public static double calculateIntensity(Map<String, Double> scores) {
        double total = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        return Math.min(total * 8, 100);
    }

    public static String getMoodLevel(double sentiment) {
        if (sentiment >= 80) return "Extremely Positive";
        if (sentiment >= 65) return "Positive";
        if (sentiment >= 45) return "Neutral";
        if (sentiment >= 25) return "Negative";
        return "Very Negative";
    }

    public static String generateReflection(String emotion) {
        switch (emotion) {
            case "Happiness": return "Your words carry uplifting energy.";
            case "Sadness": return "Something may be weighing on you.";
            case "Anger": return "I sense frustration or anger.";
            case "Fear": return "Your words show worry or uncertainty.";
            case "Love": return "Strong signs of affection detected.";
            default: return "Mixed emotional signals present.";
        }
    }

    public static String generateRecommendation(String emotion) {
        switch (emotion) {
            case "Happiness": return "Enjoy the moment and note what caused it.";
            case "Sadness": return "Talk to someone you trust.";
            case "Anger": return "Pause before reacting.";
            case "Fear": return "Focus on what you can control.";
            case "Love": return "Express appreciation to someone important.";
            default: return "Keep sharing your thoughts.";
        }
    }

    public static String detectImportantWords(String text) {
        String[] importantWords = {"happy", "sad", "love", "angry", "excited", "hurt", "pain", "scared", "worried"};
        List<String> found = new ArrayList<>();
        for (String word : importantWords) if (text.contains(word)) found.add(word);
        return found.isEmpty() ? "No strong emotional keywords detected." : String.join(", ", found);
    }
}

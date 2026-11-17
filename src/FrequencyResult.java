import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the comprehensive results of a frequency analysis on a given text.
 * Stores raw counts, calculated percentages, total letters, and descending frequency rankings.
 */
public class FrequencyResult {
    private final String originalText;
    private final int[] counts;             // Index 0 = 'A', ..., Index 25 = 'Z'
    private final double[] percentages;     // Index 0 = 'A', ..., Index 25 = 'Z'
    private final int totalLetters;
    private final List<LetterFrequency> ranking;

    public FrequencyResult(String originalText, int[] counts, double[] percentages, int totalLetters, List<LetterFrequency> ranking) {
        this.originalText = (originalText != null) ? originalText : "";
        this.counts = new int[26];
        if (counts != null) {
            System.arraycopy(counts, 0, this.counts, 0, Math.min(counts.length, 26));
        }
        this.percentages = new double[26];
        if (percentages != null) {
            System.arraycopy(percentages, 0, this.percentages, 0, Math.min(percentages.length, 26));
        }
        this.totalLetters = totalLetters;
        this.ranking = (ranking != null) ? Collections.unmodifiableList(new ArrayList<>(ranking)) : Collections.emptyList();
    }

    public String getOriginalText() {
        return originalText;
    }

    /**
     * Returns the frequency count for a specific letter (case-insensitive).
     */
    public int getCount(char letter) {
        char upper = Character.toUpperCase(letter);
        if (upper >= 'A' && upper <= 'Z') {
            return counts[upper - 'A'];
        }
        return 0;
    }

    /**
     * Returns the percentage for a specific letter (case-insensitive).
     */
    public double getPercentage(char letter) {
        char upper = Character.toUpperCase(letter);
        if (upper >= 'A' && upper <= 'Z') {
            return percentages[upper - 'A'];
        }
        return 0.0;
    }

    public int[] getCounts() {
        return counts.clone();
    }

    public double[] getPercentages() {
        return percentages.clone();
    }

    public int getTotalLetters() {
        return totalLetters;
    }

    /**
     * Returns the ranked list of letters that occurred in the text,
     * sorted descending by count, with alphabetical tie-breaking.
     */
    public List<LetterFrequency> getRanking() {
        return ranking;
    }

    /**
     * Returns an ordered list of characters that occurred in the text, from highest to lowest frequency.
     */
    public List<Character> getRankedLetters() {
        List<Character> letters = new ArrayList<>();
        for (LetterFrequency lf : ranking) {
            letters.add(lf.getLetter());
        }
        return letters;
    }
}

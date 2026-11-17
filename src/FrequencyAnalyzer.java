import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core engine for performing frequency analysis on ciphertext or plaintext strings.
 * Handles character normalization, filtering out non-alphabetic symbols,
 * counting occurrences of English letters A-Z, computing percentages, and ranking.
 */
public class FrequencyAnalyzer {

    /**
     * Analyzes the provided text:
     * - Counts occurrences of each letter (A-Z).
     * - Calculates total alphabetic characters.
     * - Computes percentage for each letter: (count / total) * 100, guarding against division by zero.
     * - Ranks observed letters in descending order of frequency, breaking ties alphabetically.
     *
     * @param text The input string to analyze
     * @return A FrequencyResult containing the complete statistical breakdown
     */
    public FrequencyResult analyze(String text) {
        int[] counts = new int[26];
        double[] percentages = new double[26];
        int totalLetters = 0;

        if (text != null && !text.isEmpty()) {
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (Character.isLetter(c)) {
                    char upper = Character.toUpperCase(c);
                    if (upper >= 'A' && upper <= 'Z') {
                        counts[upper - 'A']++;
                        totalLetters++;
                    }
                }
            }
        }

        List<LetterFrequency> rankingList = new ArrayList<>();

        if (totalLetters > 0) {
            for (int i = 0; i < 26; i++) {
                char letter = (char) ('A' + i);
                double pct = ((double) counts[i] / totalLetters) * 100.0;
                percentages[i] = pct;

                if (counts[i] > 0) {
                    rankingList.add(new LetterFrequency(letter, counts[i], pct));
                }
            }
            // Sort using natural ordering: descending by count, ascending alphabetically for ties
            Collections.sort(rankingList);
        }

        return new FrequencyResult(text, counts, percentages, totalLetters, rankingList);
    }
}

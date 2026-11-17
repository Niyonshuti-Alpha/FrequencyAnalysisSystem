/**
 * Core engine for performing frequency analysis on ciphertext or plaintext strings.
 * Handles character normalization, filtering out non-alphabetic symbols,
 * and counting occurrences of English letters A-Z.
 */
public class FrequencyAnalyzer {

    /**
     * Analyzes the provided text by counting occurrences of each letter (A-Z)
     * and calculating the total number of alphabetic characters.
     * Non-alphabetic characters (digits, spaces, punctuation) are ignored.
     * Uppercase and lowercase letters are treated identically.
     *
     * @param text The input string to analyze
     * @return A FrequencyResult containing the counts and total
     */
    public FrequencyResult analyze(String text) {
        int[] counts = new int[26];
        int totalLetters = 0;

        if (text == null || text.isEmpty()) {
            return new FrequencyResult(text, counts, 0);
        }

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Check if the character is an English letter (A-Z, a-z)
            if (Character.isLetter(c)) {
                char upper = Character.toUpperCase(c);
                if (upper >= 'A' && upper <= 'Z') {
                    counts[upper - 'A']++;
                    totalLetters++;
                }
            }
        }

        return new FrequencyResult(text, counts, totalLetters);
    }
}

/**
 * Represents the results of a frequency analysis on a given text.
 * Stores raw letter counts (A-Z) and the total number of alphabetic characters.
 */
public class FrequencyResult {
    private final String originalText;
    private final int[] counts; // Index 0 = 'A', ..., Index 25 = 'Z'
    private final int totalLetters;

    public FrequencyResult(String originalText, int[] counts, int totalLetters) {
        this.originalText = (originalText != null) ? originalText : "";
        this.counts = new int[26];
        if (counts != null) {
            System.arraycopy(counts, 0, this.counts, 0, Math.min(counts.length, 26));
        }
        this.totalLetters = totalLetters;
    }

    public String getOriginalText() {
        return originalText;
    }

    /**
     * Returns the frequency count for a specific letter (case-insensitive).
     *
     * @param letter The character to check (A-Z or a-z)
     * @return Occurrence count, or 0 if non-alphabetic
     */
    public int getCount(char letter) {
        char upper = Character.toUpperCase(letter);
        if (upper >= 'A' && upper <= 'Z') {
            return counts[upper - 'A'];
        }
        return 0;
    }

    /**
     * Returns a copy of the raw counts array for letters A-Z.
     */
    public int[] getCounts() {
        return counts.clone();
    }

    public int getTotalLetters() {
        return totalLetters;
    }
}

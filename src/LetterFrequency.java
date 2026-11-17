/**
 * Represents the frequency statistics for a single letter.
 * Implements Comparable to support natural ordering:
 * 1. Primary order: Descending by count (most frequent first).
 * 2. Secondary order (tie-breaking rule): Ascending alphabetical order (A before B).
 */
public class LetterFrequency implements Comparable<LetterFrequency> {
    private final char letter;
    private final int count;
    private final double percentage;

    public LetterFrequency(char letter, int count, double percentage) {
        this.letter = Character.toUpperCase(letter);
        this.count = count;
        this.percentage = percentage;
    }

    public char getLetter() {
        return letter;
    }

    public int getCount() {
        return count;
    }

    public double getPercentage() {
        return percentage;
    }

    @Override
    public int compareTo(LetterFrequency other) {
        // Primary sort: descending order of frequency count
        int countComparison = Integer.compare(other.count, this.count);
        if (countComparison != 0) {
            return countComparison;
        }
        // Secondary sort: alphabetical ascending (tie-breaking rule)
        return Character.compare(this.letter, other.letter);
    }

    @Override
    public String toString() {
        return String.format("%c: %d (%.2f%%)", letter, count, percentage);
    }
}

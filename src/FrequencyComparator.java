import java.util.Collections;
import java.util.List;

/**
 * Provides standard English language letter frequency reference data
 * and performs comparative cryptanalytic analysis against observed ciphertext frequencies.
 */
public class FrequencyComparator {

    // Reference letter frequencies in standard English text (percentages)
    private static final double[] ENGLISH_FREQUENCIES = {
        8.2,  // A
        1.5,  // B
        2.8,  // C
        4.3,  // D
        12.7, // E
        2.2,  // F
        2.0,  // G
        6.1,  // H
        7.0,  // I
        0.2,  // J
        0.8,  // K
        4.0,  // L
        2.4,  // M
        6.7,  // N
        7.5,  // O
        1.9,  // P
        0.1,  // Q
        6.0,  // R
        6.3,  // S
        9.1,  // T
        2.8,  // U
        1.0,  // V
        2.4,  // W
        0.2,  // X
        2.0,  // Y
        0.1   // Z
    };

    // Canonical English frequency rank order: E T A O I N S H R D L C U M W F G Y P B V K J X Q Z
    public static final char[] ENGLISH_RANKED_LETTERS = {
        'E', 'T', 'A', 'O', 'I', 'N', 'S', 'H', 'R', 'D',
        'L', 'C', 'U', 'M', 'W', 'F', 'G', 'Y', 'P', 'B',
        'V', 'K', 'J', 'X', 'Q', 'Z'
    };

    /**
     * Returns the standard English frequency percentage for a given letter.
     */
    public static double getEnglishFrequency(char letter) {
        char upper = Character.toUpperCase(letter);
        if (upper >= 'A' && upper <= 'Z') {
            return ENGLISH_FREQUENCIES[upper - 'A'];
        }
        return 0.0;
    }

    /**
     * Generates a detailed comparative report between observed ciphertext frequencies
     * and standard English language expectations.
     */
    public static String generateComparisonReport(FrequencyResult result) {
        if (result == null || result.getTotalLetters() == 0) {
            return "No ciphertext letters available to compare with English frequencies.\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("               ENGLISH LETTER FREQUENCY COMPARISON                      \n");
        sb.append("========================================================================\n");
        sb.append("Total Alphabetic Characters Analyzed: ").append(result.getTotalLetters()).append("\n\n");

        // Section 1: Side-by-Side A-Z Comparison Table
        sb.append("--- Side-by-Side Frequency Table (A-Z) ---\n");
        sb.append(String.format("%-8s %-12s %-16s %-16s %-12s\n",
                "Letter", "Count", "Observed (%)", "English Ref (%)", "Difference"));
        sb.append("------------------------------------------------------------------------\n");

        for (char ch = 'A'; ch <= 'Z'; ch++) {
            int count = result.getCount(ch);
            double observedPct = result.getPercentage(ch);
            double englishPct = getEnglishFrequency(ch);
            double diff = observedPct - englishPct;

            String diffStr = String.format("%+6.2f%%", diff);
            sb.append(String.format("  %-6c %-12d %-16s %-16s %-12s\n",
                    ch, count,
                    String.format("%6.2f%%", observedPct),
                    String.format("%6.2f%%", englishPct),
                    diffStr));
        }

        // Section 2: Rank-to-Rank Pairing
        sb.append("\n--- Rank-to-Rank Pairing (Observed vs. Standard English) ---\n");
        sb.append("Hypothesizes potential substitutions by pairing the most frequent ciphertext\n");
        sb.append("letters with the most frequent letters in standard English:\n\n");
        sb.append(String.format("%-6s %-22s %-22s %-18s\n",
                "Rank", "Observed Letter (Pct)", "English Letter (Ref Pct)", "Hypothesized Pair"));
        sb.append("------------------------------------------------------------------------\n");

        List<LetterFrequency> ranking = result.getRanking();
        int pairings = Math.min(ranking.size(), ENGLISH_RANKED_LETTERS.length);

        for (int i = 0; i < pairings; i++) {
            LetterFrequency observed = ranking.get(i);
            char engLetter = ENGLISH_RANKED_LETTERS[i];
            double engPct = getEnglishFrequency(engLetter);

            String obsStr = String.format("%c (%6.2f%%)", observed.getLetter(), observed.getPercentage());
            String engStr = String.format("%c (%6.2f%%)", engLetter, engPct);
            String pairStr = String.format("%c -> %c", observed.getLetter(), engLetter);

            sb.append(String.format(" #%-5d %-22s %-22s %-18s\n", (i + 1), obsStr, engStr, pairStr));
        }

        // Section 3: Educational Cryptographic Commentary
        sb.append("\n------------------------------------------------------------------------\n");
        sb.append("                       CRYPTANALYTIC ADVISORY                           \n");
        sb.append("------------------------------------------------------------------------\n");
        sb.append("[!] IMPORTANT STATISTICAL CONCEPTS:\n");
        sb.append(" 1. Statistical Tendency vs. Rule: English frequency tables represent averages\n");
        sb.append("    over hundreds of thousands of words. They are NOT guaranteed substitution rules.\n");
        sb.append(" 2. Sample Size Variance: In short ciphertexts (like single sentences or words),\n");
        sb.append("    natural variance is extremely high. The most common ciphertext letter is NOT\n");
        sb.append("    always 'E' (e.g., in \"A DARK KNIGHT\", 'A' and 'K' outnumber 'E').\n");
        sb.append(" 3. Frequency Flattening: Advanced ciphers (e.g., polyalphabetic Vigenere or\n");
        sb.append("    modern AES) deliberately flatten or randomize letter distributions, making\n");
        sb.append("    simple frequency analysis ineffective.\n");
        sb.append("========================================================================\n");

        return sb.toString();
    }
}

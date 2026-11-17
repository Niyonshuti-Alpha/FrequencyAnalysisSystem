import java.util.List;

/**
 * Handles cryptanalytic decryption assistance focused strictly on
 * Automatic Caesar Cipher Analysis.
 *
 * Concepts:
 * In a Caesar cipher, every letter of the alphabet is rotated by a single
 * constant shift k: C = (P + k) mod 26.
 * Because all letters shift together, finding the shift for just ONE letter
 * breaks the entire cipher.
 *
 * Algorithm:
 * 1. Identify the most frequent letter in the ciphertext.
 * 2. Hypothesize that this letter represents plaintext 'E' (the most common English letter).
 * 3. Compute shift = (position of top letter - position of E) mod 26.
 *    Decryption shifts each letter backward by this amount:
 *    P = (C - shift) mod 26.
 * 4. If multiple letters share the top frequency count, tie-breaking selects
 *    the alphabetically first letter, and an ambiguity warning is flagged.
 */
public class DecryptionAssistant {

    /**
     * Container holding the results of an automatic Caesar decryption attempt.
     */
    public static class CaesarResult {
        private final int shift;                     // The shift value (0 to 25)
        private final char topCipherLetter;          // Observed most frequent ciphertext letter (e.g. 'K')
        private final char assumedPlainLetter;       // Hypothesized plaintext letter (always 'E')
        private final String candidatePlaintext;     // Decrypted result preserving casing and non-letters
        private final boolean isTieAmbiguous;        // True if multiple letters tied for the highest count
        private final String reasoning;              // Pedagogical explanation of the deduction

        public CaesarResult(int shift, char topCipherLetter, char assumedPlainLetter,
                            String candidatePlaintext, boolean isTieAmbiguous, String reasoning) {
            this.shift = shift;
            this.topCipherLetter = topCipherLetter;
            this.assumedPlainLetter = assumedPlainLetter;
            this.candidatePlaintext = candidatePlaintext;
            this.isTieAmbiguous = isTieAmbiguous;
            this.reasoning = reasoning;
        }

        public int getShift() {
            return shift;
        }

        public char getTopCipherLetter() {
            return topCipherLetter;
        }

        public char getAssumedPlainLetter() {
            return assumedPlainLetter;
        }

        public String getCandidatePlaintext() {
            return candidatePlaintext;
        }

        public boolean isTieAmbiguous() {
            return isTieAmbiguous;
        }

        public String getReasoning() {
            return reasoning;
        }
    }

    /**
     * Automatically attempts to solve a Caesar cipher by identifying the most frequent
     * letter in the ciphertext and assuming it represents the most common English letter ('E').
     *
     * @param result The frequency analysis result of the ciphertext
     * @return CaesarResult containing shift, candidate text, reasoning, and tie flag; or null if no letters exist
     */
    public static CaesarResult solveCaesarAssumingE(FrequencyResult result) {
        if (result == null || result.getRanking().isEmpty()) {
            return null;
        }

        List<LetterFrequency> ranking = result.getRanking();
        LetterFrequency top = ranking.get(0);
        char topCipherLetter = top.getLetter();
        char assumedPlainLetter = 'E';

        // Check if there is an ambiguous tie for rank #1
        boolean isTieAmbiguous = false;
        if (ranking.size() > 1 && ranking.get(1).getCount() == top.getCount()) {
            isTieAmbiguous = true;
        }

        // Letter positions: A=0, ..., E=4, ..., K=10, ..., Z=25
        int cipherPos = topCipherLetter - 'A';
        int plainPos = assumedPlainLetter - 'A';

        // Shift formula from requirements:
        // shift = (position of top letter - position of E) mod 26
        // Example: K(10) - E(4) = 6. Decrypt by shifting back 6 positions.
        int shift = Math.floorMod(cipherPos - plainPos, 26);

        // Decrypt text by shifting backward by 'shift'
        String decrypted = applyCaesarShift(result.getOriginalText(), -shift);

        StringBuilder reasoningSb = new StringBuilder();
        reasoningSb.append(String.format("Most frequent ciphertext letter: '%c' (count: %d, %.2f%%).\n",
                topCipherLetter, top.getCount(), top.getPercentage()));
        reasoningSb.append("Hypothesis: In standard English, 'E' is the most common letter (12.7%).\n");
        reasoningSb.append(String.format("Deduction: Position of '%c' is %d; position of 'E' is %d.\n",
                topCipherLetter, cipherPos, plainPos));
        reasoningSb.append(String.format("           Shift = (%d - %d) mod 26 = %d.\n",
                cipherPos, plainPos, shift));
        reasoningSb.append(String.format("           Decrypting by shifting each letter backward by %d positions.\n", shift));

        if (isTieAmbiguous) {
            reasoningSb.append("\n[!] AMBIGUITY NOTICE:\n");
            reasoningSb.append("    Multiple letters tied for the highest frequency count.\n");
            reasoningSb.append(String.format("    '%c' was chosen based on the tie-breaking rule (alphabetically first).\n", topCipherLetter));
            reasoningSb.append("    The Caesar hypothesis may need manual consideration of the other tied letters.");
        }

        return new CaesarResult(shift, topCipherLetter, assumedPlainLetter, decrypted, isTieAmbiguous, reasoningSb.toString());
    }

    /**
     * Shifts letters in the text by a given amount.
     * Preserves case, spaces, digits, and punctuation.
     *
     * @param text The text to shift
     * @param shift Number of positions to shift (positive or negative)
     * @return The shifted text
     */
    public static String applyCaesarShift(String text, int shift) {
        if (text == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                int shifted = Math.floorMod((c - 'A') + shift, 26);
                sb.append((char) ('A' + shifted));
            } else if (c >= 'a' && c <= 'z') {
                int shifted = Math.floorMod((c - 'a') + shift, 26);
                sb.append((char) ('a' + shifted));
            } else {
                // Digits, spaces, punctuation preserved
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

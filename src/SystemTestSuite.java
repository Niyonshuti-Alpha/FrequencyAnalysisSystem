import java.util.List;
import java.util.Map;

/**
 * Automated verification test suite for the Frequency Analysis System.
 * Tests all required cases from Section 9 programmatically:
 * - HELLO WORLD (standard, lowercase, mixed case, punctuation, numbers)
 * - Empty string and whitespace-only
 * - Single letter 'A'
 * - Canonical Caesar test case 'KBKXEUTK' (frequencies, ranking, Caesar deduction)
 * - Percentage summation check (~100%)
 * - Ciphertext immutability under decryption operations
 */
public class SystemTestSuite {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   FREQUENCY ANALYSIS SYSTEM - SYSTEM TEST SUITE ");
        System.out.println("=================================================\n");

        FrequencyAnalyzer analyzer = new FrequencyAnalyzer();
        int testsPassed = 0;
        int totalTests = 0;

        // Test 1: Standard "HELLO WORLD"
        totalTests++;
        FrequencyResult r1 = analyzer.analyze("HELLO WORLD");
        if (verifyHelloWorldResult(r1, "Standard 'HELLO WORLD'")) {
            testsPassed++;
        }

        // Test 2: Lowercase "hello world"
        totalTests++;
        FrequencyResult r2 = analyzer.analyze("hello world");
        if (verifyHelloWorldResult(r2, "Lowercase 'hello world'")) {
            testsPassed++;
        }

        // Test 3: Mixed case "HeLLo WoRLd"
        totalTests++;
        FrequencyResult r3 = analyzer.analyze("HeLLo WoRLd");
        if (verifyHelloWorldResult(r3, "Mixed case 'HeLLo WoRLd'")) {
            testsPassed++;
        }

        // Test 4: Punctuation "Hello, World!"
        totalTests++;
        FrequencyResult r4 = analyzer.analyze("Hello, World!");
        if (verifyHelloWorldResult(r4, "Punctuation 'Hello, World!'")) {
            testsPassed++;
        }

        // Test 5: Numbers "HELLO 123 WORLD"
        totalTests++;
        FrequencyResult r5 = analyzer.analyze("HELLO 123 WORLD");
        if (verifyHelloWorldResult(r5, "Numbers 'HELLO 123 WORLD'")) {
            testsPassed++;
        }

        // Test 6: Empty string
        totalTests++;
        FrequencyResult rEmpty = analyzer.analyze("");
        if (rEmpty.getTotalLetters() == 0 && rEmpty.getRanking().isEmpty()) {
            System.out.println("[PASS] Empty string: Total 0, ranking empty, no crash.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] Empty string handling failed.");
        }

        // Test 7: Whitespace only
        totalTests++;
        FrequencyResult rSpaces = analyzer.analyze("    ");
        if (rSpaces.getTotalLetters() == 0 && rSpaces.getRanking().isEmpty()) {
            System.out.println("[PASS] Whitespace-only string: Total 0, ranking empty, no crash.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] Whitespace string handling failed.");
        }

        // Test 8: Single letter "A"
        totalTests++;
        FrequencyResult rSingle = analyzer.analyze("A");
        if (rSingle.getTotalLetters() == 1 && rSingle.getCount('A') == 1 && Math.abs(rSingle.getPercentage('A') - 100.0) < 0.001) {
            System.out.println("[PASS] Single letter 'A': Total 1, count 1, percentage 100.00%.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] Single letter 'A' failed.");
        }

        // Test 9: Canonical "KBKXEUTK"
        totalTests++;
        FrequencyResult rKbk = analyzer.analyze("KBKXEUTK");
        boolean kbkOk = (rKbk.getTotalLetters() == 8)
                && (rKbk.getCount('K') == 3 && Math.abs(rKbk.getPercentage('K') - 37.50) < 0.01)
                && (rKbk.getCount('B') == 1 && Math.abs(rKbk.getPercentage('B') - 12.50) < 0.01)
                && (rKbk.getCount('E') == 1 && Math.abs(rKbk.getPercentage('E') - 12.50) < 0.01)
                && (rKbk.getCount('T') == 1 && Math.abs(rKbk.getPercentage('T') - 12.50) < 0.01)
                && (rKbk.getCount('U') == 1 && Math.abs(rKbk.getPercentage('U') - 12.50) < 0.01)
                && (rKbk.getCount('X') == 1 && Math.abs(rKbk.getPercentage('X') - 12.50) < 0.01);

        // Check ranking: K, B, E, T, U, X
        List<Character> ranked = rKbk.getRankedLetters();
        boolean rankingOk = ranked.size() == 6
                && ranked.get(0) == 'K'
                && ranked.get(1) == 'B'
                && ranked.get(2) == 'E'
                && ranked.get(3) == 'T'
                && ranked.get(4) == 'U'
                && ranked.get(5) == 'X';

        if (kbkOk && rankingOk) {
            System.out.println("[PASS] 'KBKXEUTK': Counts, percentages, and tie-broken ranking (K, B, E, T, U, X) verified.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] 'KBKXEUTK' frequency or ranking mismatch.");
        }

        // Test 10: Caesar Helper on "KBKXEUTK"
        totalTests++;
        DecryptionAssistant.CaesarResult caesarRes = DecryptionAssistant.solveCaesarAssumingE(rKbk);
        if (caesarRes != null && caesarRes.getShift() == -6 && "EVERYONE".equals(caesarRes.getCandidatePlaintext())) {
            System.out.println("[PASS] Caesar Helper on 'KBKXEUTK': Suggests shift -6, candidate plaintext 'EVERYONE'.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] Caesar Helper failed on 'KBKXEUTK'.");
        }

        // Test 11: Percentage sum equals ~100%
        totalTests++;
        double sum = 0.0;
        for (LetterFrequency lf : rKbk.getRanking()) {
            sum += lf.getPercentage();
        }
        if (Math.abs(sum - 100.0) < 0.001) {
            System.out.printf("[PASS] Percentage sum verification: %.2f%% == 100.00%%\n", sum);
            testsPassed++;
        } else {
            System.out.printf("[FAIL] Percentage sum verification failed: %.2f%%\n", sum);
        }

        // Test 12: Ciphertext immutability under decryption & substitution
        totalTests++;
        String originalBefore = rKbk.getOriginalText();
        DecryptionAssistant assistant = new DecryptionAssistant();
        assistant.setMapping('K', 'E');
        assistant.setMapping('B', 'V');
        String masked = assistant.applySubstitution(originalBefore, true);
        String unmasked = assistant.applySubstitution(originalBefore, false);
        String caesarAttempt = DecryptionAssistant.applyCaesarShift(originalBefore, -6);
        String originalAfter = rKbk.getOriginalText();

        if (originalBefore.equals(originalAfter) && "KBKXEUTK".equals(originalAfter)) {
            System.out.println("[PASS] Ciphertext immutability: Original text remained unchanged through all operations.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] Ciphertext was modified!");
        }

        System.out.println("\n-------------------------------------------------");
        System.out.printf("Test Suite Completed: %d / %d Tests Passed.\n", testsPassed, totalTests);
        System.out.println("=================================================");
    }

    private static boolean verifyHelloWorldResult(FrequencyResult r, String label) {
        if (r.getTotalLetters() != 10) {
            System.out.println("[FAIL] " + label + ": Total letters was " + r.getTotalLetters() + ", expected 10.");
            return false;
        }
        if (r.getCount('L') != 3 || Math.abs(r.getPercentage('L') - 30.00) > 0.01) {
            System.out.println("[FAIL] " + label + ": L count/pct mismatch.");
            return false;
        }
        if (r.getCount('O') != 2 || Math.abs(r.getPercentage('O') - 20.00) > 0.01) {
            System.out.println("[FAIL] " + label + ": O count/pct mismatch.");
            return false;
        }
        char[] singleLetters = {'D', 'E', 'H', 'R', 'W'};
        for (char ch : singleLetters) {
            if (r.getCount(ch) != 1 || Math.abs(r.getPercentage(ch) - 10.00) > 0.01) {
                System.out.println("[FAIL] " + label + ": Letter " + ch + " mismatch.");
                return false;
            }
        }
        System.out.println("[PASS] " + label + ": Total 10; L: 3 (30.00%); O: 2 (20.00%); D, E, H, R, W: 1 (10.00%).");
        return true;
    }
}

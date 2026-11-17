import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Automated verification test suite for the Frequency Analysis System.
 * Tests:
 * 1. Standard "HELLO WORLD" and case/punctuation/digit invariance
 * 2. Empty string and whitespace-only handling
 * 3. Single letter 'A'
 * 4. Canonical Caesar test case 'KBKXEUTK' (frequencies, ranking, Caesar deduction)
 * 5. Ambiguous tie detection for Caesar cryptanalysis
 * 6. Percentage summation check (~100%)
 * 7. Ciphertext immutability under decryption operations
 * 8. HTML escaping / XSS protection in WebPageRenderer
 * 9. Web server GET (200), POST analysis (200), 404 (unknown URL), 405 (unsupported method)
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

        // Test 10: Caesar Helper on "KBKXEUTK" (shift formula: (10 - 4) mod 26 = 6, decrypt back 6)
        totalTests++;
        DecryptionAssistant.CaesarResult caesarRes = DecryptionAssistant.solveCaesarAssumingE(rKbk);
        if (caesarRes != null && caesarRes.getShift() == 6 && "EVERYONE".equals(caesarRes.getCandidatePlaintext()) && !caesarRes.isTieAmbiguous()) {
            System.out.println("[PASS] Caesar Helper on 'KBKXEUTK': Shift 6, candidate plaintext 'EVERYONE', no tie ambiguity.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] Caesar Helper failed on 'KBKXEUTK'.");
        }

        // Test 11: Caesar Ambiguity Tie Detection (e.g. "AB" where A:1 and B:1 tie for rank 1)
        totalTests++;
        FrequencyResult rTie = analyzer.analyze("AB");
        DecryptionAssistant.CaesarResult tieRes = DecryptionAssistant.solveCaesarAssumingE(rTie);
        if (tieRes != null && tieRes.isTieAmbiguous() && tieRes.getTopCipherLetter() == 'A') {
            System.out.println("[PASS] Caesar Ambiguity Tie Detection: Identified tied top count and flagged ambiguity.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] Caesar Ambiguity Tie Detection failed.");
        }

        // Test 12: Percentage sum equals ~100%
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

        // Test 13: Ciphertext immutability under decryption operations
        totalTests++;
        String originalBefore = rKbk.getOriginalText();
        String caesarAttempt = DecryptionAssistant.applyCaesarShift(originalBefore, -6);
        String originalAfter = rKbk.getOriginalText();

        if (originalBefore.equals(originalAfter) && "KBKXEUTK".equals(originalAfter)) {
            System.out.println("[PASS] Ciphertext immutability: Original text remained unchanged through decryption.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] Ciphertext was modified!");
        }

        // Test 14: HTML escaping / XSS protection
        totalTests++;
        String dangerousInput = "<script>alert('xss');</script> & \"hello\" 'world'";
        String escaped = WebPageRenderer.escapeHtml(dangerousInput);
        if (!escaped.contains("<script>") && escaped.contains("&lt;script&gt;") && escaped.contains("&amp;") && escaped.contains("&quot;") && escaped.contains("&#39;")) {
            System.out.println("[PASS] HTML Escaping: Sanitized dangerous script tags and quotes safely.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] HTML Escaping failed.");
        }

        // Test 15: Embedded Web Server Endpoints (GET, POST, 404, 405)
        totalTests++;
        boolean webTestsOk = testWebServerEndpoints();
        if (webTestsOk) {
            System.out.println("[PASS] Web Server Endpoints: GET (200), POST (200 with results), 404 Not Found, 405 Method Not Allowed.");
            testsPassed++;
        } else {
            System.out.println("[FAIL] Web Server Endpoints failed.");
        }

        System.out.println("\n-------------------------------------------------");
        System.out.printf("Test Suite Completed: %d / %d Tests Passed.\n", testsPassed, totalTests);
        System.out.println("=================================================");
    }

    private static boolean testWebServerEndpoints() {
        int testPort = 8899;
        FrequencyHttpServer testServer = null;
        try {
            testServer = new FrequencyHttpServer(testPort);
            testServer.start();

            // 1. Test GET "/"
            URI uriRoot = URI.create("http://localhost:" + testPort + "/");
            HttpURLConnection connGet = (HttpURLConnection) uriRoot.toURL().openConnection();
            connGet.setRequestMethod("GET");
            int getCode = connGet.getResponseCode();
            if (getCode != 200) return false;

            // 2. Test POST "/" with KBKXEUTK
            HttpURLConnection connPost = (HttpURLConnection) uriRoot.toURL().openConnection();
            connPost.setRequestMethod("POST");
            connPost.setDoOutput(true);
            connPost.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            try (OutputStream os = connPost.getOutputStream()) {
                os.write("ciphertext=KBKXEUTK".getBytes(StandardCharsets.UTF_8));
            }
            int postCode = connPost.getResponseCode();
            if (postCode != 200) return false;
            String postBody;
            try (InputStream is = connPost.getInputStream()) {
                postBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
            if (!postBody.contains("EVERYONE") || !postBody.contains("KBKXEUTK")) {
                return false;
            }

            // 3. Test 404 on unknown URL "/unknown-page"
            URI uriUnknown = URI.create("http://localhost:" + testPort + "/unknown-page");
            HttpURLConnection conn404 = (HttpURLConnection) uriUnknown.toURL().openConnection();
            conn404.setRequestMethod("GET");
            int code404 = conn404.getResponseCode();
            if (code404 != 404) return false;

            // 4. Test 405 on unsupported method (e.g. DELETE "/")
            HttpURLConnection conn405 = (HttpURLConnection) uriRoot.toURL().openConnection();
            conn405.setRequestMethod("DELETE");
            int code405 = conn405.getResponseCode();
            if (code405 != 405) return false;

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (testServer != null) {
                testServer.stop(0);
            }
        }
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

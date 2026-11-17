import java.util.List;

/**
 * Generates responsive, self-contained HTML for the Frequency Analysis System web interface.
 * Renders all five application sections in strict order:
 * 1. Enter ciphertext
 * 2. Analyse letter frequencies
 * 3. View frequency ranking
 * 4. Compare with English frequencies
 * 5. Decryption assistance (Automatic Caesar analysis)
 *
 * Uses the exact same domain classes (FrequencyResult, FrequencyComparator, DecryptionAssistant)
 * without duplicating logic.
 */
public class WebPageRenderer {

    /**
     * Sanitizes user input to prevent Cross-Site Scripting (XSS) and HTML injection.
     * Replaces &, <, >, ", and ' with safe HTML entities.
     *
     * @param text Raw user string
     * @return HTML-safe string
     */
    public static String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&':  sb.append("&amp;"); break;
                case '<':  sb.append("&lt;"); break;
                case '>':  sb.append("&gt;"); break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default:   sb.append(c); break;
            }
        }
        return sb.toString();
    }

    /**
     * Renders the complete single-page application.
     *
     * @param rawCiphertext The ciphertext entered by the user (if any)
     * @param result The analysis result (null if no analysis has been run)
     * @param errorMessage Optional error message (e.g. text too long)
     * @return Complete HTML page as a string
     */
    public static String renderPage(String rawCiphertext, FrequencyResult result, String errorMessage) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n");
        html.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>Frequency Analysis System</title>\n");
        html.append("<style>\n");
        html.append(getCss());
        html.append("</style>\n</head>\n<body>\n");

        html.append("<div class=\"container\">\n");
        html.append("<header>\n");
        html.append("  <h1>Frequency Analysis System</h1>\n");
        html.append("  <p class=\"subtitle\">Classical Cryptanalysis &amp; Caesar Cipher Frequency Assistant</p>\n");
        html.append("</header>\n");

        if (errorMessage != null && !errorMessage.isEmpty()) {
            html.append("<div class=\"alert error\">").append(escapeHtml(errorMessage)).append("</div>\n");
        }

        // Section 1: Enter ciphertext
        html.append("<section class=\"card\" id=\"section-1\">\n");
        html.append("  <h2>1. Enter Ciphertext</h2>\n");
        html.append("  <form method=\"POST\" action=\"/\">\n");
        html.append("    <label for=\"ciphertext\">Ciphertext (supports letters, spaces, numbers, punctuation):</label>\n");
        html.append("    <textarea id=\"ciphertext\" name=\"ciphertext\" rows=\"5\" placeholder=\"Enter ciphertext here...\">");
        if (rawCiphertext != null) {
            html.append(escapeHtml(rawCiphertext));
        }
        html.append("</textarea>\n");
        html.append("    <div class=\"btn-group\">\n");
        html.append("      <button type=\"submit\" class=\"btn btn-primary\">Analyse</button>\n");
        html.append("      <button type=\"button\" class=\"btn btn-secondary\" onclick=\"loadExample()\">Load example (KBKXEUTK)</button>\n");
        html.append("    </div>\n");
        html.append("  </form>\n");
        html.append("</section>\n");

        boolean hasData = (result != null && result.getTotalLetters() > 0);

        // Section 2: Analyse letter frequencies
        html.append("<section class=\"card\" id=\"section-2\">\n");
        html.append("  <h2>2. Analyse Letter Frequencies</h2>\n");
        if (!hasData) {
            html.append("  <div class=\"placeholder\"><p>Enter ciphertext above to see results.</p></div>\n");
        } else {
            html.append("  <p><strong>Total Alphabetic Characters:</strong> ").append(result.getTotalLetters()).append("</p>\n");
            html.append("  <div class=\"table-container\">\n");
            html.append("    <table>\n");
            html.append("      <thead><tr><th>Letter</th><th>Count</th><th>Frequency (%)</th><th>Distribution Bar</th></tr></thead>\n");
            html.append("      <tbody>\n");
            for (char ch = 'A'; ch <= 'Z'; ch++) {
                int count = result.getCount(ch);
                double pct = result.getPercentage(ch);
                html.append(String.format("        <tr><td><strong>%c</strong></td><td>%d</td><td>%.2f%%</td>", ch, count, pct));
                html.append(String.format("<td><div class=\"bar-track\"><div class=\"bar-fill\" style=\"width: %.2f%%;\"></div></div></td></tr>\n", Math.min(pct * 2.5, 100.0)));
            }
            html.append("      </tbody>\n    </table>\n  </div>\n");
        }
        html.append("</section>\n");

        // Section 3: View frequency ranking
        html.append("<section class=\"card\" id=\"section-3\">\n");
        html.append("  <h2>3. View Frequency Ranking</h2>\n");
        if (!hasData) {
            html.append("  <div class=\"placeholder\"><p>Enter ciphertext above to see results.</p></div>\n");
        } else {
            List<LetterFrequency> ranking = result.getRanking();
            html.append("  <p><strong>Unique Letters Observed:</strong> ").append(ranking.size()).append("</p>\n");
            html.append("  <p><strong>Ranking Order:</strong> ");
            for (int i = 0; i < ranking.size(); i++) {
                html.append("<strong>").append(ranking.get(i).getLetter()).append("</strong>");
                if (i < ranking.size() - 1) html.append(", ");
            }
            html.append("</p>\n");

            html.append("  <div class=\"table-container\">\n");
            html.append("    <table>\n");
            html.append("      <thead><tr><th>Rank</th><th>Letter</th><th>Count</th><th>Percentage</th></tr></thead>\n");
            html.append("      <tbody>\n");
            for (int i = 0; i < ranking.size(); i++) {
                LetterFrequency lf = ranking.get(i);
                html.append(String.format("        <tr><td>#%d</td><td><strong>%c</strong></td><td>%d</td><td>%.2f%%</td></tr>\n",
                        (i + 1), lf.getLetter(), lf.getCount(), lf.getPercentage()));
            }
            html.append("      </tbody>\n    </table>\n  </div>\n");
            html.append("  <p class=\"note\">* <em>Tie-breaking rule:</em> Letters with equal counts are ordered alphabetically.</p>\n");
        }
        html.append("</section>\n");

        // Section 4: Compare with English frequencies
        html.append("<section class=\"card\" id=\"section-4\">\n");
        html.append("  <h2>4. Compare with English Frequencies</h2>\n");
        if (!hasData) {
            html.append("  <div class=\"placeholder\"><p>Enter ciphertext above to see results.</p></div>\n");
        } else {
            html.append("  <h3>Side-by-Side Comparison (A–Z)</h3>\n");
            html.append("  <div class=\"table-container\">\n");
            html.append("    <table>\n");
            html.append("      <thead><tr><th>Letter</th><th>Count</th><th>Observed (%)</th><th>English Ref (%)</th><th>Difference</th></tr></thead>\n");
            html.append("      <tbody>\n");
            for (char ch = 'A'; ch <= 'Z'; ch++) {
                int count = result.getCount(ch);
                double obs = result.getPercentage(ch);
                double ref = FrequencyComparator.getEnglishFrequency(ch);
                double diff = obs - ref;
                String diffClass = (diff > 0) ? "diff-pos" : (diff < 0 ? "diff-neg" : "diff-zero");
                html.append(String.format("        <tr><td><strong>%c</strong></td><td>%d</td><td>%.2f%%</td><td>%.2f%%</td><td class=\"%s\">%+6.2f%%</td></tr>\n",
                        ch, count, obs, ref, diffClass, diff));
            }
            html.append("      </tbody>\n    </table>\n  </div>\n");

            html.append("  <h3>Rank-to-Rank Pairing (Observed vs. Standard English)</h3>\n");
            html.append("  <div class=\"table-container\">\n");
            html.append("    <table>\n");
            html.append("      <thead><tr><th>Rank</th><th>Observed Letter</th><th>English Letter</th><th>Hypothesized Pair</th></tr></thead>\n");
            html.append("      <tbody>\n");
            List<LetterFrequency> ranking = result.getRanking();
            int pairings = Math.min(ranking.size(), FrequencyComparator.ENGLISH_RANKED_LETTERS.length);
            for (int i = 0; i < pairings; i++) {
                char obs = ranking.get(i).getLetter();
                char eng = FrequencyComparator.ENGLISH_RANKED_LETTERS[i];
                html.append(String.format("        <tr><td>#%d</td><td>%c (%.2f%%)</td><td>%c (%.2f%%)</td><td><strong>%c &rarr; %c</strong></td></tr>\n",
                        (i + 1), obs, ranking.get(i).getPercentage(), eng, FrequencyComparator.getEnglishFrequency(eng), obs, eng));
            }
            html.append("      </tbody>\n    </table>\n  </div>\n");

            html.append("  <div class=\"alert info\">\n");
            html.append("    <strong>Cryptanalytic Advisory:</strong> Standard frequency tables represent statistical tendencies over large text corpuses, ");
            html.append("    not guaranteed substitution rules. Short ciphertexts deviate heavily from average letter frequencies.\n");
            html.append("  </div>\n");
        }
        html.append("</section>\n");

        // Section 5: Decryption assistance (Automatic Caesar analysis)
        html.append("<section class=\"card\" id=\"section-5\">\n");
        html.append("  <h2>5. Decryption Assistance (Automatic Caesar Analysis)</h2>\n");
        if (!hasData) {
            html.append("  <div class=\"placeholder\"><p>Enter ciphertext above to see results.</p></div>\n");
        } else {
            DecryptionAssistant.CaesarResult caesar = DecryptionAssistant.solveCaesarAssumingE(result);
            if (caesar == null) {
                html.append("  <p>No alphabetic letters found in ciphertext.</p>\n");
            } else {
                html.append("  <div class=\"caesar-results\">\n");
                html.append("    <p><strong>Most Frequent Ciphertext Letter:</strong> <span class=\"highlight\">").append(caesar.getTopCipherLetter()).append("</span></p>\n");
                html.append("    <p><strong>Assumed Plaintext Letter:</strong> <span class=\"highlight\">").append(caesar.getAssumedPlainLetter()).append("</span></p>\n");
                html.append("    <p><strong>Computed Shift:</strong> <span class=\"highlight\">").append(caesar.getShift()).append("</span> (shifts backward by ").append(caesar.getShift()).append(")</p>\n");

                if (caesar.isTieAmbiguous()) {
                    html.append("    <div class=\"alert warning\">\n");
                    html.append("      <strong>Ambiguity Notice:</strong> Multiple letters tied for the highest frequency count. ");
                    html.append("      '").append(caesar.getTopCipherLetter()).append("' was chosen by the tie-breaking rule (alphabetically first).\n");
                    html.append("    </div>\n");
                }

                html.append("    <div class=\"text-compare\">\n");
                html.append("      <div class=\"text-block\">\n");
                html.append("        <h4>Original Ciphertext</h4>\n");
                html.append("        <pre>").append(escapeHtml(result.getOriginalText())).append("</pre>\n");
                html.append("      </div>\n");
                html.append("      <div class=\"text-block\">\n");
                html.append("        <h4>Candidate Plaintext (Decrypted)</h4>\n");
                html.append("        <pre class=\"plaintext\">").append(escapeHtml(caesar.getCandidatePlaintext())).append("</pre>\n");
                html.append("      </div>\n");
                html.append("    </div>\n");

                html.append("    <div class=\"alert warning\">\n");
                html.append("      <strong>Cryptographic Warning:</strong> This deduction is an educated guess based on statistical frequency tendencies. ");
                html.append("      It only applies if the text was encrypted with a Caesar cipher. Frequency analysis does not guarantee decryption, ");
                html.append("      especially on short texts. Verify that the candidate plaintext forms meaningful words.\n");
                html.append("    </div>\n");
                html.append("  </div>\n");
            }
        }
        html.append("</section>\n");

        html.append("<footer>\n");
        html.append("  <p>Frequency Analysis System &bull; Classical Cryptography Learning Platform</p>\n");
        html.append("</footer>\n");

        html.append("</div>\n");

        // Client-side JavaScript for the 'Load example' button
        html.append("<script>\n");
        html.append("function loadExample() {\n");
        html.append("  document.getElementById('ciphertext').value = 'KBKXEUTK';\n");
        html.append("}\n");
        html.append("</script>\n");

        html.append("</body>\n</html>\n");
        return html.toString();
    }

    private static String getCss() {
        return "* { box-sizing: border-box; margin: 0; padding: 0; }\n" +
               "body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f3f4f6; color: #1f2937; line-height: 1.5; padding: 20px; }\n" +
               ".container { max-width: 960px; margin: 0 auto; }\n" +
               "header { text-align: center; margin-bottom: 25px; padding: 20px 0; border-bottom: 2px solid #e5e7eb; }\n" +
               "header h1 { font-size: 2rem; color: #111827; margin-bottom: 6px; }\n" +
               "header .subtitle { color: #6b7280; font-size: 1rem; }\n" +
               ".card { background: #ffffff; border-radius: 8px; padding: 24px; margin-bottom: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); border: 1px solid #e5e7eb; }\n" +
               ".card h2 { font-size: 1.25rem; color: #1f2937; margin-bottom: 16px; border-bottom: 1px solid #f3f4f6; padding-bottom: 8px; }\n" +
               ".card h3 { font-size: 1rem; color: #374151; margin: 16px 0 8px 0; }\n" +
               "label { display: block; font-weight: 500; margin-bottom: 8px; font-size: 0.95rem; }\n" +
               "textarea { width: 100%; padding: 12px; font-family: monospace; font-size: 1rem; border: 1px solid #d1d5db; border-radius: 6px; resize: vertical; margin-bottom: 12px; }\n" +
               "textarea:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,0.2); }\n" +
               ".btn-group { display: flex; gap: 10px; flex-wrap: wrap; }\n" +
               ".btn { padding: 10px 18px; font-size: 0.95rem; font-weight: 500; border-radius: 6px; cursor: pointer; border: none; transition: background-color 0.15s ease; }\n" +
               ".btn-primary { background-color: #2563eb; color: #ffffff; }\n" +
               ".btn-primary:hover { background-color: #1d4ed8; }\n" +
               ".btn-secondary { background-color: #e5e7eb; color: #374151; }\n" +
               ".btn-secondary:hover { background-color: #d1d5db; }\n" +
               ".table-container { overflow-x: auto; margin-bottom: 12px; }\n" +
               "table { width: 100%; border-collapse: collapse; font-size: 0.9rem; text-align: left; }\n" +
               "th, td { padding: 8px 12px; border-bottom: 1px solid #e5e7eb; }\n" +
               "th { background-color: #f9fafb; font-weight: 600; color: #4b5563; }\n" +
               "tr:hover { background-color: #f9fafb; }\n" +
               ".bar-track { background-color: #e5e7eb; height: 16px; border-radius: 4px; overflow: hidden; width: 100%; min-width: 120px; }\n" +
               ".bar-fill { background-color: #3b82f6; height: 100%; border-radius: 4px; }\n" +
               ".diff-pos { color: #059669; font-weight: 500; }\n" +
               ".diff-neg { color: #dc2626; font-weight: 500; }\n" +
               ".diff-zero { color: #6b7280; }\n" +
               ".alert { padding: 12px 16px; border-radius: 6px; font-size: 0.9rem; margin-top: 14px; margin-bottom: 14px; }\n" +
               ".alert.info { background-color: #eff6ff; color: #1e40af; border: 1px solid #bfdbfe; }\n" +
               ".alert.warning { background-color: #fffbeb; color: #92400e; border: 1px solid #fde68a; }\n" +
               ".alert.error { background-color: #fef2f2; color: #991b1b; border: 1px solid #fecaca; }\n" +
               ".placeholder { padding: 24px; text-align: center; color: #9ca3af; font-style: italic; background-color: #f9fafb; border-radius: 6px; border: 1px dashed #d1d5db; }\n" +
               ".text-compare { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin: 16px 0; }\n" +
               "@media (max-width: 640px) { .text-compare { grid-template-columns: 1fr; } }\n" +
               ".text-block h4 { font-size: 0.9rem; color: #4b5563; margin-bottom: 6px; }\n" +
               ".text-block pre { background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px; padding: 12px; font-family: monospace; font-size: 0.95rem; white-space: pre-wrap; word-break: break-all; min-height: 60px; }\n" +
               ".text-block pre.plaintext { background-color: #ecfdf5; border-color: #a7f3d0; color: #065f46; font-weight: 600; }\n" +
               ".highlight { font-weight: bold; color: #2563eb; }\n" +
               ".note { font-size: 0.85rem; color: #6b7280; margin-top: 6px; }\n" +
               "footer { text-align: center; font-size: 0.85rem; color: #9ca3af; margin: 30px 0 10px 0; }\n";
    }
}

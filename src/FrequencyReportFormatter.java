import java.util.List;

/**
 * Utility class for formatting frequency analysis results into clean,
 * readable reports and visual text-based bar charts.
 */
public class FrequencyReportFormatter {

    /**
     * Formats the complete analysis report including:
     * - Original ciphertext
     * - Total alphabetic count
     * - Full A-Z frequency table (including 0-count letters)
     * - Ranked letters list
     * - Visual frequency bar chart
     */
    public static String formatFullReport(FrequencyResult result) {
        if (result == null || result.getTotalLetters() == 0) {
            return "No alphabetic characters available to analyze.\n" +
                   "Original text: \"" + (result != null ? result.getOriginalText() : "") + "\"\n" +
                   "Total alphabetic characters: 0\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("           FREQUENCY ANALYSIS REPORT                \n");
        sb.append("====================================================\n");
        sb.append("Original Ciphertext:\n");
        sb.append("  \"").append(result.getOriginalText()).append("\"\n\n");
        sb.append("Total Alphabetic Characters: ").append(result.getTotalLetters()).append("\n\n");

        sb.append("--- Full A-Z Letter Frequency Table ---\n");
        sb.append(String.format("%-8s %-8s %-12s\n", "Letter", "Count", "Frequency (%)"));
        sb.append("------------------------------------\n");
        for (char ch = 'A'; ch <= 'Z'; ch++) {
            int count = result.getCount(ch);
            double pct = result.getPercentage(ch);
            sb.append(String.format("  %-6c %-8d %6.2f%%\n", ch, count, pct));
        }

        sb.append("\n--- Frequency Ranking (Observed Letters Only) ---\n");
        List<LetterFrequency> ranking = result.getRanking();
        sb.append(formatRankingSummary(ranking)).append("\n");

        sb.append("\n--- Frequency Distribution Bar Chart ---\n");
        sb.append("(Scale: Each '#' represents approximately 2%)\n");
        sb.append(formatBarChart(ranking));

        sb.append("====================================================\n");
        return sb.toString();
    }

    /**
     * Formats a dedicated ranking report explaining letter orders and tie-breaking.
     */
    public static String formatRankingReport(FrequencyResult result) {
        if (result == null || result.getTotalLetters() == 0) {
            return "No letters to rank (Total alphabetic characters: 0).\n";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("              FREQUENCY RANKING REPORT              \n");
        sb.append("====================================================\n");
        sb.append("Total Alphabetic Characters: ").append(result.getTotalLetters()).append("\n");
        sb.append("Unique Letters Observed: ").append(result.getRanking().size()).append("\n\n");

        sb.append("Summary Order: ");
        sb.append(formatRankingSummary(result.getRanking())).append("\n\n");

        sb.append(String.format("%-6s %-8s %-8s %-12s\n", "Rank", "Letter", "Count", "Percentage"));
        sb.append("----------------------------------------------\n");
        List<LetterFrequency> ranking = result.getRanking();
        for (int i = 0; i < ranking.size(); i++) {
            LetterFrequency lf = ranking.get(i);
            sb.append(String.format(" #%-5d %-8c %-8d %6.2f%%\n",
                    (i + 1), lf.getLetter(), lf.getCount(), lf.getPercentage()));
        }

        sb.append("\n* Note on Tie-Breaking: Letters with identical counts are\n");
        sb.append("  ranked in ascending alphabetical order (e.g., A before B).\n");
        sb.append("====================================================\n");
        return sb.toString();
    }

    private static String formatRankingSummary(List<LetterFrequency> ranking) {
        if (ranking.isEmpty()) {
            return "(none)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ranking.size(); i++) {
            sb.append(ranking.get(i).getLetter());
            if (i < ranking.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Creates an ASCII horizontal bar chart for observed letters.
     * Each '#' represents 2%.
     */
    public static String formatBarChart(List<LetterFrequency> ranking) {
        if (ranking.isEmpty()) {
            return "  (No letters observed)\n";
        }
        StringBuilder sb = new StringBuilder();
        for (LetterFrequency lf : ranking) {
            int barCount = (int) Math.round(lf.getPercentage() / 2.0);
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < barCount; i++) {
                bar.append("#");
            }
            sb.append(String.format("  %c | %-25s %6.2f%%\n", lf.getLetter(), bar.toString(), lf.getPercentage()));
        }
        return sb.toString();
    }
}

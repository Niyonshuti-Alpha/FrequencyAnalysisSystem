import java.util.Scanner;

/**
 * Main console application and menu loop for the Frequency Analysis System.
 * Connects the user interactively to the frequency analysis and Caesar decryption engines.
 * Also provides an option to launch the web interface server.
 */
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final FrequencyAnalyzer analyzer = new FrequencyAnalyzer();
    private static String currentCiphertext = null;
    private static FrequencyResult currentResult = null;
    private static FrequencyHttpServer runningServer = null;

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            displayMenu();
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("\n[!] Please enter a valid menu option (1-7).\n");
                continue;
            }

            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("\n[!] Invalid input: \"" + input + "\". Please enter a number between 1 and 7.\n");
                continue;
            }

            switch (choice) {
                case 1:
                    handleEnterCiphertext();
                    break;
                case 2:
                    handleAnalyseFrequencies();
                    break;
                case 3:
                    handleViewRanking();
                    break;
                case 4:
                    handleCompareEnglish();
                    break;
                case 5:
                    handleDecryptionAssistance();
                    break;
                case 6:
                    handleStartWebPage();
                    break;
                case 7:
                    if (runningServer != null) {
                        System.out.println("\nStopping web server...");
                        runningServer.stop(0);
                    }
                    System.out.println("\nExiting Frequency Analysis System. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid option (" + choice + "). Please select a number from 1 to 7.\n");
            }
        }
    }

    private static void displayMenu() {
        System.out.println("========================================");
        System.out.println("       FREQUENCY ANALYSIS SYSTEM        ");
        System.out.println("========================================");
        System.out.println("1. Enter ciphertext");
        System.out.println("2. Analyse letter frequencies");
        System.out.println("3. View frequency ranking");
        System.out.println("4. Compare with English frequencies");
        System.out.println("5. Decryption assistance (Automatic Caesar)");
        System.out.println("6. Start web page server");
        System.out.println("7. Exit");
        System.out.print("\nSelect an option: ");
    }

    private static void handleEnterCiphertext() {
        System.out.println("\n--- Enter Ciphertext ---");
        System.out.print("Enter text (letters, spaces, punctuation accepted): ");
        String text = scanner.nextLine();

        if (text == null || text.trim().isEmpty()) {
            System.out.println("\n[!] Warning: You entered an empty or whitespace-only string.");
            System.out.println("    Letter counts and percentages will be 0.\n");
        }

        currentCiphertext = text;
        currentResult = analyzer.analyze(currentCiphertext);

        System.out.println("\n[+] Ciphertext stored successfully!");
        System.out.println("    Total alphabetic characters detected: " + currentResult.getTotalLetters() + "\n");
    }

    private static void handleAnalyseFrequencies() {
        if (!ensureCiphertextLoaded()) {
            return;
        }
        System.out.println();
        System.out.print(FrequencyReportFormatter.formatFullReport(currentResult));
        System.out.println();
    }

    private static void handleViewRanking() {
        if (!ensureCiphertextLoaded()) {
            return;
        }
        System.out.println();
        System.out.print(FrequencyReportFormatter.formatRankingReport(currentResult));
        System.out.println();
    }

    private static void handleCompareEnglish() {
        if (!ensureCiphertextLoaded()) {
            return;
        }
        System.out.println();
        System.out.print(FrequencyComparator.generateComparisonReport(currentResult));
        System.out.println();
    }

    private static void handleDecryptionAssistance() {
        if (!ensureCiphertextLoaded()) {
            return;
        }

        System.out.println("\n====================================================");
        System.out.println("       DECRYPTION ASSISTANCE (CAESAR ANALYSIS)       ");
        System.out.println("====================================================");

        if (currentResult.getTotalLetters() == 0) {
            System.out.println("[!] Cannot perform Caesar analysis: No alphabetic letters in ciphertext.\n");
            return;
        }

        DecryptionAssistant.CaesarResult res = DecryptionAssistant.solveCaesarAssumingE(currentResult);
        if (res == null) {
            System.out.println("[!] Analysis could not be performed.\n");
            return;
        }

        System.out.println("Original Ciphertext:");
        System.out.println("  \"" + currentResult.getOriginalText() + "\"");

        System.out.println("\n--- Analysis & Deduction ---");
        System.out.println(res.getReasoning());

        System.out.println("\n--- Summary of Caesar Parameters ---");
        System.out.printf("  Top Ciphertext Letter: '%c'\n", res.getTopCipherLetter());
        System.out.printf("  Assumed Plaintext Letter: '%c'\n", res.getAssumedPlainLetter());
        System.out.printf("  Computed Shift: %d (shifts backward by %d)\n", res.getShift(), res.getShift());

        System.out.println("\n--- Candidate Plaintext ---");
        System.out.println("  \"" + res.getCandidatePlaintext() + "\"");

        System.out.println("\n----------------------------------------------------");
        System.out.println("[!] CRYPTOGRAPHIC WARNING:");
        System.out.println("    1. This deduction is a guess based on a statistical tendency;");
        System.out.println("       it only applies if the text was encrypted with a Caesar cipher.");
        System.out.println("    2. Frequency analysis does NOT automatically decrypt every message.");
        System.out.println("       Short ciphertexts deviate heavily from average letter frequencies.");
        System.out.println("    3. Always verify whether the candidate plaintext forms valid words.");
        System.out.println("====================================================\n");
    }

    private static void handleStartWebPage() {
        if (runningServer != null) {
            System.out.println("\n[i] Web server is already running at: http://localhost:" + runningServer.getPort() + "/\n");
            return;
        }

        int port = 8080;
        System.out.print("\nEnter port for web server [press Enter for 8080]: ");
        String portInput = scanner.nextLine().trim();
        if (!portInput.isEmpty()) {
            try {
                port = Integer.parseInt(portInput);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid port number. Using default port 8080.");
                port = 8080;
            }
        }

        try {
            runningServer = new FrequencyHttpServer(port);
            runningServer.start();
            System.out.println("\n[+] Web server started successfully!");
            System.out.println("    Open your browser to: http://localhost:" + port + "/");
            System.out.println("    (The server will run in the background until you exit the console)\n");
        } catch (java.net.BindException e) {
            System.out.println("\n[!] Error: Port " + port + " is already in use.");
            System.out.println("    Please choose another port (e.g., 8081).\n");
        } catch (Exception e) {
            System.out.println("\n[!] Failed to start web server: " + e.getMessage() + "\n");
        }
    }

    private static boolean ensureCiphertextLoaded() {
        if (currentResult == null) {
            System.out.println("\n[!] No ciphertext has been entered yet.");
            System.out.println("    Please select Option 1 first to enter ciphertext.\n");
            return false;
        }
        return true;
    }
}

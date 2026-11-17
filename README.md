# FrequencyAnalysisSystem

A Java application that performs **frequency analysis** on ciphertext, a classical cryptanalysis technique. It counts how often each letter appears, compares the result with typical English letter frequencies, and uses that to make an automatic guess at a **Caesar cipher** shift.

It has two interfaces that share the same logic classes:

1. A **console menu** application.
2. A **single web page** that displays all results.

This is an academic project built to demonstrate an understanding of cryptography, frequency analysis, and Java programming.

---

## What is frequency analysis?

Frequency analysis is the study of letters (or groups of letters) in a ciphertext in an attempt to partially reveal the message. In English, some letters are far more common than others: **E** is the most common, followed by **T** and **A**, while **J, Q, X, Z** are very rare. If a cipher does not hide these frequencies, parts of the plaintext can be worked out from the ciphertext alone.

**Worked example (Caesar cipher):**

| Step | Detail |
|---|---|
| Ciphertext | `KBKXEUTK` |
| Frequencies | K:3, B:1, X:1, E:1, U:1, T:1 |
| Guess | K is most frequent, so assume K = E |
| Shift | K (10) - E (4) = 6, so shift every letter back by 6 |
| Plaintext | `EVERYONE` |

---

## Features

1. **Text input**: upper/lowercase letters; spaces, digits and punctuation are accepted and ignored in the counts.
2. **Letter frequency counting**: occurrences of each letter A-Z and the total number of letters.
3. **Frequency percentages**: `Frequency (%) = (occurrences / total letters) x 100`, shown to two decimals.
4. **Frequency ranking**: most to least frequent; ties are broken alphabetically.
5. **Analysis report**: full A-Z table plus a bar chart of the distribution.
6. **English comparison**: observed vs. English percentages, the difference, and rank-to-rank pairing.
7. **Decryption assistance (automatic Caesar analysis)**: assumes the most frequent ciphertext letter is `E`, computes the shift, and shows a candidate plaintext. The original ciphertext is never modified.

> **Important:** frequency analysis gives statistical *tendencies*, not guarantees. The Caesar guess only applies if the text really was encrypted with a Caesar cipher, and it can be wrong, especially for short texts.

---

## Project structure

```
FrequencyAnalysisSystem/
├── src/
│   ├── Main.java                     Console entry point and menu
│   ├── WebMain.java                  Web entry point (starts the server)
│   ├── FrequencyHttpServer.java      Built-in HTTP server (GET/POST handling)
│   ├── WebPageRenderer.java          Builds the HTML page
│   ├── FrequencyAnalyzer.java        Counting, percentages, ranking
│   ├── FrequencyResult.java          Holds the analysis results
│   ├── LetterFrequency.java          One letter's count and percentage
│   ├── FrequencyComparator.java      English reference data and comparison
│   ├── DecryptionAssistant.java      Automatic Caesar analysis
│   ├── FrequencyReportFormatter.java Formats the console report
│   └── SystemTestSuite.java          Test cases
├── docs/
│   └── PROJECT_PROMPT.md             Full project specification
├── .gitignore
└── README.md
```

The analysis logic is kept separate from the console and web presentation, so both interfaces produce identical numbers.

---

## Requirements

- **JDK 11 or newer** (check with `java -version`)
- No external libraries or frameworks. The web page uses only the JDK's built-in `com.sun.net.httpserver.HttpServer`.

---

## How to run

### Option A: IntelliJ IDEA

Open the project folder, then right-click `Main.java` (console) or `WebMain.java` (web) and choose **Run**.

### Option B: Command line (PowerShell)

From the project root:

```powershell
# Compile everything into an "out" folder
mkdir out
javac -d out src\*.java

# Console application
java -cp out Main

# Web page
java -cp out WebMain

# Run the tests
java -cp out SystemTestSuite
```

> These commands assume the source files have no `package` declaration. If they do, use the full class name, e.g. `java -cp out yourpackage.Main`.

### Using the web page

1. Run `WebMain`.
2. Open **http://localhost:8080/** in your browser (the URL is also printed in the console).
3. Type or paste ciphertext, or click **Load example (KBKXEUTK)**, then click **Analyse**.

To use a different port, pass it as an argument: `java -cp out WebMain 9090`. Stop the server with `Ctrl + C`.

---

## Console menu

```
========================================
       FREQUENCY ANALYSIS SYSTEM
========================================
1. Enter ciphertext
2. Analyse letter frequencies
3. View frequency ranking
4. Compare with English frequencies
5. Decryption assistance (automatic Caesar analysis)
6. Exit
```

Invalid or non-numeric input is handled with a message, and the menu is shown again.

---

## Web page sections

The single page shows all five options in order:

1. Enter ciphertext
2. Analyse letter frequencies
3. View frequency ranking
4. Compare with English frequencies
5. Decryption assistance (automatic Caesar analysis)

All user text is HTML-escaped before display to prevent script injection. Empty input, oversized input, unknown URLs (404), unsupported methods (405) and a port already in use are all handled with clear messages.

---

## Test cases

| Input | Expected result |
|---|---|
| `HELLO WORLD` | Total 10; L 3 (30.00%); O 2 (20.00%); H, E, W, R, D 1 each (10.00%) |
| `hello world`, `HeLLo WoRLd` | Same as above (case-insensitive) |
| `Hello, World!` | Same as above (punctuation ignored) |
| `HELLO 123 WORLD` | Same as above (digits ignored) |
| Empty or spaces only | Total 0; no crash; clear message |
| `A` | Total 1; A 1 (100.00%) |
| `KBKXEUTK` | K 3 (37.50%); B, X, E, U, T 1 each (12.50%); ranking K, B, E, T, U, X |
| `KBKXEUTK` (Caesar) | Top letter K, assumed E, shift 6, plaintext `EVERYONE` |

---

## English reference frequencies

| Letter | % | Letter | % | Letter | % |
|---|---|---|---|---|---|
| A | 8.2 | J | 0.2 | S | 6.3 |
| B | 1.5 | K | 0.8 | T | 9.1 |
| C | 2.8 | L | 4.0 | U | 2.8 |
| D | 4.3 | M | 2.4 | V | 1.0 |
| E | 12.7 | N | 6.7 | W | 2.4 |
| F | 2.2 | O | 7.5 | X | 0.2 |
| G | 2.0 | P | 1.9 | Y | 2.0 |
| H | 6.1 | Q | 0.1 | Z | 0.1 |
| I | 7.0 | R | 6.0 | | |

Rank order: E T A O I N S H R D L C U M W F G Y P B V K J X Q Z

---

## Limitations

- Short ciphertexts give unreliable statistics.
- Ties between letters are ambiguous.
- The most frequent ciphertext letter is not always `E`.
- Unusual texts (for example, with very few E's) skew the results.
- The method only helps against Caesar-type (monoalphabetic) ciphers. Polyalphabetic ciphers such as Vigenere, and modern encryption, resist it.

## Possible future improvements

- Digram and trigram analysis (TH, HE, THE).
- Manual letter mapping for general substitution ciphers.
- Listing all 25 Caesar shifts for comparison.

---

## Author

NIYONSHUTI Alpha

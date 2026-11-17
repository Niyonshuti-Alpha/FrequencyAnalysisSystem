import java.io.IOException;
import java.net.BindException;

/**
 * Entry point for starting the web interface of the Frequency Analysis System.
 * Accepts an optional port argument (defaults to 8080).
 * Handles port binding errors gracefully and provides clean console feedback.
 */
public class WebMain {
    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                if (port < 1024 || port > 65535) {
                    System.err.println("[!] Port must be between 1024 and 65535. Falling back to default port " + DEFAULT_PORT);
                    port = DEFAULT_PORT;
                }
            } catch (NumberFormatException e) {
                System.err.println("[!] Invalid port argument: '" + args[0] + "'. Falling back to default port " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        try {
            FrequencyHttpServer server = new FrequencyHttpServer(port);
            server.start();

            String url = "http://localhost:" + port + "/";
            System.out.println("====================================================");
            System.out.println("  FREQUENCY ANALYSIS SYSTEM - WEB SERVER STARTED    ");
            System.out.println("====================================================");
            System.out.println("  Web Interface URL: " + url);
            System.out.println("  Serving all 5 frequency analysis sections.");
            System.out.println("  Press Ctrl+C in this console to stop the server.");
            System.out.println("====================================================");

            // Register shutdown hook for clean termination
            final int activePort = port;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[i] Stopping web server on port " + activePort + "...");
                server.stop(1);
                System.out.println("[+] Web server stopped cleanly.");
            }));

        } catch (BindException e) {
            System.err.println("\n[!] Error: Port " + port + " is already in use by another application.");
            System.err.println("    Please free the port or specify an alternative port argument:");
            System.err.println("    Example: java -cp out/production/FrequencyAnalysisSystem WebMain 8081\n");
        } catch (IOException e) {
            System.err.println("\n[!] Failed to start HTTP server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

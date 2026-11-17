import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Embedded HTTP server implementing the Frequency Analysis System web interface.
 * Uses only the standard JDK built-in HttpServer (com.sun.net.httpserver).
 *
 * Capabilities:
 * - Serves the single-page application at root ("/")
 * - Handles GET (renders initial form and placeholders)
 * - Handles POST (processes submitted ciphertext up to 100,000 characters)
 * - Validates input sizes and returns clean error messages
 * - Enforces correct HTTP status codes (404 for unknown URLs, 405 for unsupported methods)
 */
public class FrequencyHttpServer {
    private static final int MAX_INPUT_LENGTH = 100_000;
    private final HttpServer server;
    private final int port;
    private final FrequencyAnalyzer analyzer;

    public FrequencyHttpServer(int port) throws IOException {
        this.port = port;
        this.analyzer = new FrequencyAnalyzer();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/", new RootHandler());
        this.server.setExecutor(null); // default single-threaded/direct executor
    }

    public void start() {
        server.start();
    }

    public void stop(int delaySeconds) {
        server.stop(delaySeconds);
    }

    public int getPort() {
        return port;
    }

    private class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            // 404 for unknown paths (only "/" is supported)
            if (!"/".equals(path)) {
                byte[] notFound = "404 Not Found".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(404, notFound.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound);
                }
                return;
            }

            String method = exchange.getRequestMethod().toUpperCase();
            if ("GET".equals(method)) {
                handleGet(exchange);
            } else if ("POST".equals(method)) {
                handlePost(exchange);
            } else {
                // 405 Method Not Allowed
                exchange.getResponseHeaders().set("Allow", "GET, POST");
                byte[] notAllowed = "405 Method Not Allowed".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
                exchange.sendResponseHeaders(405, notAllowed.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notAllowed);
                }
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            String html = WebPageRenderer.renderPage("", null, null);
            sendHtmlResponse(exchange, 200, html);
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            int totalBytes = 0;
            // Limit reading to prevent out-of-memory from malicious payloads
            boolean exceeded = false;
            while ((read = is.read(buffer)) != -1) {
                totalBytes += read;
                if (totalBytes > (MAX_INPUT_LENGTH * 4)) { // allow URL-encoded expansion
                    exceeded = true;
                    break;
                }
                baos.write(buffer, 0, read);
            }

            if (exceeded) {
                String html = WebPageRenderer.renderPage("", null,
                        "Input rejected: Payload exceeds the 100,000 character limit.");
                sendHtmlResponse(exchange, 400, html);
                return;
            }

            String formData = baos.toString(StandardCharsets.UTF_8);
            String rawCiphertext = extractFormParam(formData, "ciphertext");

            if (rawCiphertext.length() > MAX_INPUT_LENGTH) {
                String html = WebPageRenderer.renderPage(rawCiphertext.substring(0, 1000), null,
                        "Input exceeds the maximum allowed length of 100,000 characters.");
                sendHtmlResponse(exchange, 400, html);
                return;
            }

            FrequencyResult result = null;
            String errorMessage = null;

            if (rawCiphertext.trim().isEmpty()) {
                errorMessage = "Warning: You entered empty or whitespace-only text. Letter counts and percentages are 0.";
                result = analyzer.analyze(rawCiphertext);
            } else {
                result = analyzer.analyze(rawCiphertext);
                if (result.getTotalLetters() == 0) {
                    errorMessage = "Notice: The input contains no alphabetic characters (A-Z). Letter counts are 0.";
                }
            }

            String html = WebPageRenderer.renderPage(rawCiphertext, result, errorMessage);
            sendHtmlResponse(exchange, 200, html);
        }

        private String extractFormParam(String formData, String paramName) {
            if (formData == null || formData.isEmpty()) {
                return "";
            }
            String[] pairs = formData.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    if (paramName.equals(key)) {
                        String value = (idx < pair.length() - 1)
                                ? pair.substring(idx + 1)
                                : "";
                        return URLDecoder.decode(value, StandardCharsets.UTF_8);
                    }
                }
            }
            return "";
        }

        private void sendHtmlResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}

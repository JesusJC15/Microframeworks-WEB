package com.arep.microframeworks_web;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HttpServer {

    private static final Map<String, WebMethod> endPoints = new ConcurrentHashMap<>();
    private static String staticFilesLocation = "webroot/public";
    private static int port = 8080;

    public static void main(String[] args) throws IOException, URISyntaxException {
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                port = 8080;
            }
        }

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("Listening on http://localhost:" + port);
                try (Socket clientSocket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        BufferedOutputStream out = new BufferedOutputStream(clientSocket.getOutputStream())) {

                    String requestLine = in.readLine();
                    if (requestLine == null || requestLine.isBlank()) {
                        continue;
                    }

                    String headerLine;
                    while ((headerLine = in.readLine()) != null && !headerLine.isBlank()) {
                    }

                    HttpExchangeData exchangeData = parseRequestLine(requestLine);
                    if (exchangeData == null) {
                        writeResponse(out, 400, "Bad Request", "text/plain; charset=UTF-8", "Invalid request", Map.of());
                        continue;
                    }

                    HttpRequest request = new HttpRequest(exchangeData.method, exchangeData.path, exchangeData.query);
                    HttpResponse response = new HttpResponse();

                    if ("GET".equalsIgnoreCase(exchangeData.method)) {
                        if (executeEndpoint(out, request, response)) {
                            continue;
                        }
                        if (serveStaticResource(out, request.getPath())) {
                            continue;
                        }
                        writeResponse(out, 404, "Not Found", "text/plain; charset=UTF-8", "Resource not found", Map.of());
                    } else {
                        writeResponse(out, 405, "Method Not Allowed", "text/plain; charset=UTF-8", "Only GET is supported", Map.of());
                    }
                }
            }
        }
    }

    private static HttpExchangeData parseRequestLine(String requestLine) {
        String[] tokens = requestLine.split(" ");
        if (tokens.length < 3) {
            return null;
        }

        String method = tokens[0];
        String uriText = tokens[1];
        try {
            URI uri = new URI(uriText);
            return new HttpExchangeData(method, uri.getPath(), uri.getQuery());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static boolean executeEndpoint(BufferedOutputStream out, HttpRequest request, HttpResponse response) throws IOException {
        WebMethod currentWm = endPoints.get(request.getPath());
        if (currentWm == null) {
            return false;
        }

        String body = currentWm.execute(request, response);
        writeResponse(out, response.getStatusCode(), response.getStatusText(), response.getContentType(), body,
                response.getHeaders());
        return true;
    }

    private static boolean serveStaticResource(BufferedOutputStream out, String requestPath) throws IOException {
        String normalizedRequestPath = requestPath == null || requestPath.isBlank() || "/".equals(requestPath)
                ? "/index.html"
                : requestPath;

        if (normalizedRequestPath.contains("..")) {
            writeResponse(out, 400, "Bad Request", "text/plain; charset=UTF-8", "Invalid path", Map.of());
            return true;
        }

        String resourcePath = normalizePath(staticFilesLocation + normalizedRequestPath);
        byte[] resourceData = readResource(resourcePath);
        if (resourceData == null && !resourcePath.contains("/public/")) {
            String fallbackPath = normalizePath(staticFilesLocation + "/public" + normalizedRequestPath);
            resourceData = readResource(fallbackPath);
            resourcePath = fallbackPath;
        }

        if (resourceData == null) {
            return false;
        }

        String contentType = resolveContentType(resourcePath);
        writeBinaryResponse(out, 200, "OK", contentType, resourceData, Map.of());
        return true;
    }

    private static String normalizePath(String path) {
        String normalized = path.replace("\\", "/");
        while (normalized.contains("//")) {
            normalized = normalized.replace("//", "/");
        }
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static byte[] readResource(String classpathRelativePath) throws IOException {
        try (InputStream inputStream = HttpServer.class.getClassLoader().getResourceAsStream(classpathRelativePath)) {
            if (inputStream != null) {
                return inputStream.readAllBytes();
            }
        }

        File file = new File("target/classes", classpathRelativePath);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        try (InputStream fileInput = new FileInputStream(file)) {
            return fileInput.readAllBytes();
        }
    }

    private static String resolveContentType(String path) {
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".html") || lowerPath.endsWith(".htm")) {
            return "text/html; charset=UTF-8";
        }
        if (lowerPath.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        }
        if (lowerPath.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        }
        if (lowerPath.endsWith(".json")) {
            return "application/json; charset=UTF-8";
        }
        if (lowerPath.endsWith(".png")) {
            return "image/png";
        }
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lowerPath.endsWith(".gif")) {
            return "image/gif";
        }
        if (lowerPath.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lowerPath.endsWith(".ico")) {
            return "image/x-icon";
        }
        return "application/octet-stream";
    }

    private static void writeResponse(BufferedOutputStream out, int statusCode, String statusText, String contentType,
            String body, Map<String, String> headers) throws IOException {
        byte[] bodyBytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        writeBinaryResponse(out, statusCode, statusText, contentType, bodyBytes, headers);
    }

    private static void writeBinaryResponse(BufferedOutputStream out, int statusCode, String statusText,
            String contentType, byte[] bodyBytes, Map<String, String> headers) throws IOException {
        ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
        responseStream.write(("HTTP/1.1 " + statusCode + " " + statusText + "\r\n").getBytes(StandardCharsets.UTF_8));
        responseStream.write(("Content-Type: " + contentType + "\r\n").getBytes(StandardCharsets.UTF_8));
        responseStream.write(("Content-Length: " + bodyBytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        responseStream.write("Connection: close\r\n".getBytes(StandardCharsets.UTF_8));
        for (Map.Entry<String, String> header : headers.entrySet()) {
            responseStream.write((header.getKey() + ": " + header.getValue() + "\r\n")
                    .getBytes(StandardCharsets.UTF_8));
        }
        responseStream.write("\r\n".getBytes(StandardCharsets.UTF_8));
        responseStream.write(bodyBytes);

        out.write(responseStream.toByteArray());
        out.flush();
    }

    public static void staticfiles(String folder) {
        if (folder == null || folder.isBlank()) {
            staticFilesLocation = "webroot/public";
            return;
        }

        String cleaned = folder.trim().replace("\\", "/");
        if (!cleaned.startsWith("/")) {
            cleaned = "/" + cleaned;
        }
        staticFilesLocation = cleaned;
    }

    public static void setPort(int configuredPort) {
        port = configuredPort;
    }

    public static void get(String path, WebMethod wm) {
        endPoints.put(path, wm);
    }

    private record HttpExchangeData(String method, String path, String query) {
    }
}

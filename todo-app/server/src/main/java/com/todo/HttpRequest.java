package com.todo;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String method;
    private String path;
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public HttpRequest(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequest parse(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isBlank()) return null;

        String[] parts = requestLine.split(" ");
        String method = parts[0];
        String path = parts.length > 1 ? parts[1] : "/";

        Map<String, String> headers = new HashMap<>();
        String line;
        int contentLength = 0;

        while ((line = reader.readLine()) != null && !line.isBlank()) {
            int colon = line.indexOf(':');
            if (colon > 0) {
                String key = line.substring(0, colon).trim().toLowerCase();
                String value = line.substring(colon + 1).trim();
                headers.put(key, value);
                if (key.equals("content-length")) {
                    try { contentLength = Integer.parseInt(value); } catch (NumberFormatException ignored) {}
                }
            }
        }

        String body = "";
        if (contentLength > 0) {
            char[] buf = new char[contentLength];
            int read = reader.read(buf, 0, contentLength);
            body = new String(buf, 0, read);
        }

        return new HttpRequest(method, path, headers, body);
    }

    // Simple JSON field extractor — no external libs
    public String getJsonField(String fieldName) {
        if (body == null) return null;
        String search = "\"" + fieldName + "\"";
        int idx = body.indexOf(search);
        if (idx < 0) return null;
        int colon = body.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < body.length() && body.charAt(start) == ' ') start++;
        if (start >= body.length()) return null;
        char first = body.charAt(start);
        if (first == '"') {
            int end = body.indexOf('"', start + 1);
            return end > start ? body.substring(start + 1, end) : null;
        } else {
            int end = start;
            while (end < body.length() && ",}".indexOf(body.charAt(end)) < 0) end++;
            return body.substring(start, end).trim();
        }
    }

    public String getMethod() { return method; }
    public String getPath() { return path; }
    public Map<String, String> getHeaders() { return headers; }
    public String getBody() { return body; }
}

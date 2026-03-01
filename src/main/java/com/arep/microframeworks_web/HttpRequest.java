package com.arep.microframeworks_web;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private final String method;
    private final String path;
    private final String query;
    private final Map<String, String> queryParams;

    public HttpRequest(String method, String path, String query) {
        this.method = method;
        this.path = path;
        this.query = query;
        this.queryParams = parseQuery(query);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public String getValue(String varName) {
        return queryParams.get(varName);
    }

    public String getValues(String varName) {
        return queryParams.get(varName);
    }

    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> values = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return values;
        }

        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            if (pair.isBlank()) {
                continue;
            }
            String[] tokens = pair.split("=", 2);
            String key = decode(tokens[0]);
            String value = tokens.length > 1 ? decode(tokens[1]) : "";
            values.put(key, value);
        }
        return values;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}

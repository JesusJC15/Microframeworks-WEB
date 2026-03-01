package com.arep.microframeworks_web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
	private int statusCode = 200;
	private String statusText = "OK";
	private String contentType = "text/plain; charset=UTF-8";
	private final Map<String, String> headers = new HashMap<>();

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusText() {
		return statusText;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void status(int statusCode, String statusText) {
		this.statusCode = statusCode;
		this.statusText = statusText;
	}

	public void setHeader(String key, String value) {
		headers.put(key, value);
	}

	public Map<String, String> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}

}

package com.obopay.communicationclient;

import java.io.IOException;

public class HttpResponseException extends IOException {

	private static final long serialVersionUID = -3751863640503551128L;

	private int statusCode;

	private String reason;

	public HttpResponseException(int statusCode, String reason) {
		super("Http Status Code :" + statusCode + " - " + reason);
		this.reason = reason;
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

}

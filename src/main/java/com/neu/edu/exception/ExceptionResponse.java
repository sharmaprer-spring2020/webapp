package com.neu.edu.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

public class ExceptionResponse {
	
	private String message;
	private String path;
	private HttpStatus status;
	private LocalDateTime timestamp;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	public void setStatus(HttpStatus status) {
		this.status = status;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	
	
	

}

package com.neu.edu.pojo;

import java.util.List;

public class NotificationMessage {
	
	private String userId;
	private List<String> urls;
	
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public List<String> getUrls() {
		return urls;
	}
	public void setUrls(List<String> urls) {
		this.urls = urls;
	}
}

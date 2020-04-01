package com.neu.edu.pojo;

public class BillDueRequest {
	String userId;
	int days;
	
	public BillDueRequest() {
		// TODO Auto-generated constructor stub
	}
	
	public BillDueRequest(String userId, int days) {
		this.userId = userId;
		this.days = days;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getDays() {
		return days;
	}
	public void setDays(int days) {
		this.days = days;
	}
	

}

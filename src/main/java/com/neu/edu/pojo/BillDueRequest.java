package com.neu.edu.pojo;

/**
 * @author prerna
 *
 */
public class BillDueRequest {
	String userId;
	String emailId;
	int days;
	
	public BillDueRequest() {
		// TODO Auto-generated constructor stub
	}
	
	public BillDueRequest(String userId, String email, int days) {
		this.userId = userId;
		this.emailId = email;
		this.days = days;
	}
	
	
	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
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
